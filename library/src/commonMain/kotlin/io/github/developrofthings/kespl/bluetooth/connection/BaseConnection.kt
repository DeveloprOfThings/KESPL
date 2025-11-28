@file:OptIn(ExperimentalTime::class)

package io.github.developrofthings.kespl.bluetooth.connection

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.EmptySuccessESPResponse
import io.github.developrofthings.kespl.MutableESPFlowController
import io.github.developrofthings.kespl.asFailure
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.isV1
import io.github.developrofthings.kespl.packet.ESPRequest
import io.github.developrofthings.kespl.packet.data.versionAsDouble
import io.github.developrofthings.kespl.packet.destinationIdByte
import io.github.developrofthings.kespl.packet.isForMe
import io.github.developrofthings.kespl.packet.isForV1c
import io.github.developrofthings.kespl.packet.isInfDisplayData
import io.github.developrofthings.kespl.packet.isLegacy
import io.github.developrofthings.kespl.packet.isTimeSlicing
import io.github.developrofthings.kespl.packet.isV1Version
import io.github.developrofthings.kespl.packet.originId
import io.github.developrofthings.kespl.packet.originIdByte
import io.github.developrofthings.kespl.packet.packetIdByte
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Abstract class representing a base connection for ESP communication.
 * This class provides common functionality and properties for different connection types.
 *
 * @property connectionScope The [CoroutineScope] in which connection-related operations are performed.
 */
abstract class BaseConnection internal constructor(
    flowController: MutableESPFlowController,
    val logger: PlatformLogger,
    override val connectionScope: CoroutineScope,
) : IConnection {

    private val _useEchoQueue = atomic(false)

    override var canEchoQueue: Boolean by _useEchoQueue

    protected val _connectionStatus = flowController.connectionStatus

    override val connectionStatus: StateFlow<ESPConnectionStatus> = _connectionStatus.asStateFlow()

    private val _noData = flowController.noData

    override val noData: SharedFlow<Unit> = _noData.asSharedFlow()

    protected open val _espData = flowController.espData

    override val espData: Flow<ByteArray> = _espData.asSharedFlow()

    protected open val _notificationData = flowController.notificationData

    override val notificationData: Flow<String> get() = _notificationData.asSharedFlow()

    private var _noDataWDChannel: Channel<Unit>? = null

    private val _v1Version: MutableStateFlow<Double> = flowController.v1Version

    private val _valentineOneType: MutableStateFlow<ESPDevice.ValentineOne> =
        flowController.valentineOneType

    private val _tempValentineOneType: AtomicRef<ESPDevice.ValentineOne> =
        atomic(ESPDevice.ValentineOne.Unknown)

    private val _valentineOneTypeCounter: AtomicInt = atomic(0)

    override val v1Version: StateFlow<Double> get() = _v1Version.asStateFlow()

    private val _isTimeSlicing: MutableStateFlow<Boolean> = flowController.isTimeSlicing

    override val isTimeSlicing: StateFlow<Boolean>
        get() = _isTimeSlicing.asStateFlow()

    private val echoQueueMutex = Mutex()
    private val _echoQueue: MutableList<Echo> = ArrayDeque(12)

    private var _outputWriterJob: Job? = null

    private var _noDataWatchDogJob: Job? = null

    private var initialVersionRequestJob: Job? = null

    override val hasDeterminedV1Type: Boolean get() = _valentineOneType.value.isV1

    override val hasV1Version: Boolean get() = _v1Version.value != 0.0

    override val v1Type: StateFlow<ESPDevice.ValentineOne> get() = _valentineOneType.asStateFlow()

    init {
        flowController.forceStatefulDefaults()
    }

    override suspend fun connect(v1c: V1connection, directConnect: Boolean): Boolean {
        if (_connectionStatus.value == ESPConnectionStatus.Connected) return true
        else if (
            (_connectionStatus.value != ESPConnectionStatus.Disconnected) and
            (_connectionStatus.value != ESPConnectionStatus.ConnectionFailed) and
            (_connectionStatus.value != ESPConnectionStatus.ConnectionLost)
        ) return false
        _connectionStatus.emit(ESPConnectionStatus.Connecting)
        return performConnection(v1c = v1c, directConnect = directConnect).also {
            if (it) onConnectionEstablished()
            else {
                _connectionStatus.emit(ESPConnectionStatus.ConnectionFailed)
                // We want to quickly transition into the disconnected state
                _connectionStatus.emit(ESPConnectionStatus.Disconnected)
            }
        }
    }

    protected abstract suspend fun performConnection(v1c: V1connection, directConnect: Boolean): Boolean

    override suspend fun disconnect() {
        if (
            (_connectionStatus.value != ESPConnectionStatus.Disconnected) and
            (_connectionStatus.value != ESPConnectionStatus.ConnectionLost)
        ) {
            _connectionStatus.emit(ESPConnectionStatus.Disconnecting)
            performDisconnect()
        }
    }

    protected abstract suspend fun performDisconnect()

    @OptIn(FlowPreview::class)
    override suspend fun writeRequest(
        request: ESPRequest,
        waitForV1TypeDuration: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if(connectionStatus.value != ESPConnectionStatus.Connected) {
            return ESPFailure.NotConnected.asFailure()
        }
        // Block until we determine the V1 type
        val v1Type = _valentineOneType
            .timeout(waitForV1TypeDuration)
            .catch { exception ->
                if (exception is TimeoutCancellationException) {
                    // Catch the TimeoutCancellationException emitted above.
                    // Emit desired item on timeout.
                    emit(ESPDevice.ValentineOne.Unknown)
                } else {
                    // Throw other exceptions.
                    throw exception
                }
            }
            .firstOrNull { it != ESPDevice.ValentineOne.Unknown }
            ?: return ESPFailure.V1NotDetermined.asFailure()

        // Prevent known errors... we can't send any device besides the V1connection while in Legacy
        if (!request.isForV1c() && v1Type == ESPDevice.ValentineOne.Legacy) {
            return ESPFailure.LegacyMode.asFailure()
        }

        return try {
            if (!_isTimeSlicing.value && !request.isForV1c()) {
                return ESPFailure.NotTimeSlicing.asFailure()
            }

            val isChecksum = v1Type == ESPDevice.ValentineOne.Checksum
            request.toWritablePayload(useChecksum = isChecksum).let { payload ->
                if(!writeBytes(payload)) ESPFailure.ESPOperationFailed.asFailure()
                else {
                    addPayloadToEchoQueue(payload = payload)
                    EmptySuccessESPResponse
                }
            }

        } catch (e: Exception) {
            ESPFailure.Unknown(e).asFailure()
        }
    }

    protected abstract suspend fun writeBytes(bytes: ByteArray): Boolean

    private fun ByteArray.shouldEcho(): Boolean =
    // Packets with the same dest and orig (going to V1) will not be placed on the ESP Bus so
        // they won't get echoed
        !((this.destinationIdByte == this.originIdByte) ||
                // The V1connection will consume mute request and manually pull-up the mute line so this
                // request will not be placed on the ESP Bus so they won't get echoed
                this.packetIdByte == ESPPacketId.ReqMuteOn.id)

    private suspend fun addPayloadToEchoQueue(payload: ByteArray) {
        if (!canEchoQueue || !payload.shouldEcho()) return

        echoQueueMutex.withLock {
            _echoQueue.add(
                Echo(
                    packetPayload = payload,
                    txTime = Clock.System.now(),
                )
            )
        }
    }

    private suspend fun checkEchoQueue(espData: ByteArray): Boolean {
        if (!canEchoQueue) return false
        // Local function so that we don't have to perform an explicit synchronization and we can
        // guarantee that access only happens after checking the echo queue
        fun purgeExpiredEchos(now: Instant) {
            for (i in _echoQueue.lastIndex downTo 0) {
                val echo = _echoQueue[i]
                if (echo.hasExpired(now)) {
                    // If we aren't time-slicing reset the tx time and
                    if (!_isTimeSlicing.value) {
                        _echoQueue[i] = echo.copy(txTime = now)
                        continue
                    }
                    _echoQueue.removeAt(i)
                }
            }
        }

        var retVal = false

        echoQueueMutex.withLock {
            for (i in _echoQueue.lastIndex downTo 0) {
                val echo = _echoQueue[i]
                if (espData.contentEquals(echo.packetPayload)) {
                    retVal = true
                }
            }
            purgeExpiredEchos(Clock.System.now())
        }

        return retVal
    }

    private suspend fun clearEchoQueue() {
        echoQueueMutex.withLock {
            _echoQueue.clear()
        }
    }

    protected open suspend fun cleanupForDisconnection() {
        _v1Version.value = 0.0

        _tempValentineOneType.value = ESPDevice.ValentineOne.Unknown
        _valentineOneType.value = ESPDevice.ValentineOne.Unknown

        _isTimeSlicing.value = false

        val disconnectionException = CancellationException("Disconnecting")
        _noDataWatchDogJob?.cancel(disconnectionException)
        _outputWriterJob?.cancel(disconnectionException)
        initialVersionRequestJob?.cancel(disconnectionException)

        _noDataWatchDogJob = null
        _outputWriterJob = null
        initialVersionRequestJob = null

        // Cancel any in-flight request
        _noDataWDChannel?.close(disconnectionException)
        clearEchoQueue()
    }

    @OptIn(FlowPreview::class)
    private fun requestV1Version() {
        initialVersionRequestJob = connectionScope.launch {
            withTimeoutOrNull(3.seconds) {
                if(!(_isTimeSlicing.firstOrNull { it } ?: false)) return@withTimeoutOrNull
                val v1Type = _valentineOneType.firstOrNull {
                    it != ESPDevice.ValentineOne.Unknown &&
                            it != ESPDevice.ValentineOne.Legacy
                } ?: return@withTimeoutOrNull

                writeRequest(
                    request = ESPRequest(
                        destination = v1Type,
                        requestId = ESPPacketId.ReqVersion,
                    )
                )
            }
        }
    }

    private suspend fun onConnectionEstablished() {
        // Detect the V1 version on start up
        requestV1Version()
        // Emit to the rest of the library we've connected
        _connectionStatus.emit(ESPConnectionStatus.Connected)
    }

    protected suspend fun onConnectionLost() {
        _connectionStatus.emit(ESPConnectionStatus.ConnectionLost)
        cleanupForDisconnection()
    }

    protected suspend fun onDisconnected() {
        _connectionStatus.emit(ESPConnectionStatus.Disconnected)
        cleanupForDisconnection()
    }

    private fun ByteArray.v1Type(): ESPDevice.ValentineOne =
        when {
            isLegacy || originIdByte == ESPDevice.ValentineOne.Legacy.originatorIdentifier -> ESPDevice.ValentineOne.Legacy
            originId == ESPDevice.ValentineOne.NoChecksum -> ESPDevice.ValentineOne.NoChecksum
            else -> ESPDevice.ValentineOne.Checksum
        }

    private suspend inline fun processInfDisplayData(infDisplayData: ByteArray) {
        val currentV1Type = infDisplayData.v1Type()
        val lastKnownV1Type = _valentineOneType.value

        if (currentV1Type != lastKnownV1Type) {
            if (_tempValentineOneType.value == lastKnownV1Type) {
                _valentineOneTypeCounter.incrementAndGet()
            } else {
                _valentineOneTypeCounter.getAndSet(1)
                _tempValentineOneType.getAndSet(currentV1Type)
            }

            if (_valentineOneTypeCounter.compareAndSet(expect = V1_TYPE_SWITCH_THRESHOLD, 0)) {
                _valentineOneType.emit(currentV1Type)
            }
        }

        _isTimeSlicing.emit(infDisplayData.isTimeSlicing)
    }

    protected suspend fun processESPData(espData: ByteArray) {
        // Pet dog
        handleNoDataWatchDog()
        // Check if this packet is an echo
        if (checkEchoQueue(espData)) return

        if (!espData.isForMe) return
        // Perform in-app processing before we emit the ESPPacket
        else if (espData.isInfDisplayData) processInfDisplayData(espData)

        if (espData.isV1Version) _v1Version.emit(value = espData.versionAsDouble())

        // We MUST perform all app processing on ESP data before emitting to rest of the world.
        _espData.emit(value = espData)
    }

    private suspend inline fun handleNoDataWatchDog() {
        // Make sure the watch dog is started
        connectionScope.initNoDataWatchDog()
        _noDataWDChannel?.send(Unit)
    }

    private fun CoroutineScope.initNoDataWatchDog(timeout: Duration = staleDataWatchDogTimeout) {
        // If already running bail
        if (_noDataWatchDogJob?.isActive == true) return

        _noDataWDChannel = Channel(Channel.RENDEZVOUS)
        _noDataWatchDogJob = launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    withTimeout(timeout) {
                        // This call will block until we've received data
                        _noDataWDChannel?.receive()
                    }
                } catch (_: TimeoutCancellationException) {
                    _noData.emit(Unit)
                }
            }
        }
    }
}

data class Echo(
    val packetPayload: ByteArray,
    val txTime: Instant = Clock.System.now(),
) {
    fun hasExpired(
        now: Instant,
        expiration: Duration = echoDataExpiration,
    ): Boolean = (now - txTime) > expiration

    override fun hashCode(): Int {
        var result = txTime.hashCode()
        result = 31 * result + packetPayload.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Echo

        if (!packetPayload.contentEquals(other.packetPayload)) return false
        if (txTime != other.txTime) return false

        return true
    }
}

private val echoDataExpiration: Duration = 1000.milliseconds
internal const val V1_TYPE_SWITCH_THRESHOLD = 5