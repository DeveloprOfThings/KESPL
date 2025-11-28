package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.MutableESPFlowController
import io.github.developrofthings.kespl.ORIG_IDX
import io.github.developrofthings.kespl.ORIG_INDENTIFIER_BASE_CONST
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.BaseConnection
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.connection.LE_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_LE_SCANNER_QUALIFER
import io.github.developrofthings.kespl.getValentineOne
import io.github.developrofthings.kespl.packet.isFromV1
import io.github.developrofthings.kespl.packet.isValidFramingData
import io.github.developrofthings.kespl.packet.originIdByte
import io.github.developrofthings.kespl.packet.validateChecksum
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformWhile
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named

@Factory(binds = [IConnection::class])
@Named(LE_CONNECTION_QUALIFIER)
internal class LeConnection(
    flowController: MutableESPFlowController,
    private val bluetoothManager: IBluetoothManager,
    logger: PlatformLogger,
    @InjectedParam connectionScope: CoroutineScope,
    @Named(V1C_LE_SCANNER_QUALIFER) private val scanner: IV1cScanner,
) : BaseConnection(
    flowController = flowController,
    logger = logger,
    connectionScope = connectionScope
) {
    override val connectionType: V1cType
        get() = V1cType.LE

    private var _platformLeClient: PlatformLeClient? = null
    private var _observeConnectionStateJob: Job? = null
    private var _clientOutV1In: PlatformLeCharacteristicWrapper? = null
    private var _v1InJob: Job? = null

    override suspend fun scan(scanMode: ESPScanMode): Flow<V1connectionScanResult> =
        scanner.startScan(scanMode = scanMode)

    override suspend fun performConnection(v1c: V1connection, directConnect: Boolean): Boolean {
        if (v1c !is V1connection.Remote) return false

        return try {
            getPlatformLeClient(
                v1c = v1c,
                directConnect = directConnect,
                bluetoothManager = bluetoothManager,
                logger = logger,
                coroutineScope = connectionScope,
            ).also { espPeripheralClient ->
                _platformLeClient = espPeripheralClient
                // WE MUST call establish connection after assigning the client.
                espPeripheralClient.establishConnection()

                // Discover services
                val espService = espPeripheralClient
                    .discoveryV1CLEServiceAndCharacteristics()

                val clientOut = espService.clientOutV1In
                val v1Out = espService.v1InClientInt

                // Watch for connection loss
                _observeConnectionStateJob =
                    connectionScope.watchForDisconnection(espPeripheralClient)
                _v1InJob = connectionScope.watchForInput(v1OutClientIn = v1Out)

                _clientOutV1In = clientOut
            }
            true
        } catch (e: Exception) {
            if (e is LeConnectionFailed) {
                // Aborted connection still throw exceptions so we want to consume them here and log
                // an appropriate error
                if (_connectionStatus.value == ESPConnectionStatus.Disconnecting) {
                    logger.info(
                        "LeConnection", """Attempted connection with ${v1c.name} was aborted!""".trimMargin()
                    )
                } else {
                    logger.error(
                        "LeConnection", """
                |Caught an exception while attempting to establish at connection with ${v1c.name}.
                |Exception: ${e.message}""".trimMargin()
                    )
                }
            }
            // Make sure we don't have an errant connection waiting to completeF
            _platformLeClient?.cancelConnection()
            false
        }
    }

    override suspend fun performDisconnect() {
        _platformLeClient?.disconnectWaitForConfirmation()
    }

    private suspend fun CoroutineScope.watchForInput(
        v1OutClientIn: PlatformLeCharacteristicWrapper,
    ): Job {
        var lastV1Type: ESPDevice.ValentineOne = ESPDevice.ValentineOne.Unknown
        return v1OutClientIn
            .notifications()
            .onEach { bytes ->
                // Make sure we got good eps data
                if (bytes.isValidESPPacket(lastV1Type)) {
                    // Store the last V1 type
                    if (bytes.isFromV1()) {
                        lastV1Type = getValentineOne(bytes.originIdByte)
                    }

                    processESPData(espData = bytes)
                }
            }
            .launchIn(this)
    }

    private fun CoroutineScope.watchForDisconnection(client: PlatformLeClient): Job = client
        .connectionStatus
        // We only want to emit once we determine we're disconnected
        .transformWhile {
            // we want to collect until we get a Disconnected status
            (it != ESPConnectionStatus.Disconnected).also { notDisconnected ->
                if (!notDisconnected) emit(it)
            }
        }
        // Only fires when `ESPConnectionStatus.Disconnected` is emitted
        .onEach {
            client.close()
            // If we didn't enter the disconnecting state we are experiencing an
            // unexpected disconnect ie connection loss
            if (_connectionStatus.value != ESPConnectionStatus.Disconnecting) onConnectionLost()
            else onDisconnected()
        }
        .launchIn(this)

    override suspend fun cleanupForDisconnection() {
        super.cleanupForDisconnection()
        _v1InJob?.cancel()
        _platformLeClient = null
        _clientOutV1In = null
    }

    override suspend fun writeBytes(bytes: ByteArray): Boolean = _clientOutV1In?.let {
        try {
            it.write(data = bytes)
            true
        } catch (e: ESPLeException) {
            logger.error(
                "LeConnection", """
                |Caught an exception while attempting to write to the client out characteristic.
                |Exception: ${e.message}""".trimMargin()
            )
            false
        }
    } ?: false
}

private fun ByteArray.isValidESPPacket(lastV1Type: ESPDevice.ValentineOne): Boolean {
    if (!isValidFramingData()) return false

    val rawOrgId = (this[ORIG_IDX] - ORIG_INDENTIFIER_BASE_CONST).toByte()

    val useChecksum =
        rawOrgId == ESPDevice.ValentineOne.Checksum.id || lastV1Type == ESPDevice.ValentineOne.Checksum
    /*
        ___________________________________
        |   A   |   B   |   !(A & !(B))   |
        ___________________________________
        |   T   |   T   |        T        |
        |   T   |   F   |        F        |
        |   F   |   T   |        T        |
        |   F   |   F   |        T        |
        ___________________________________

     */
    return !(useChecksum && !this.validateChecksum())
}