@file:JvmName("DemoConnectionKt")

package io.github.developrofthings.kespl.bluetooth.connection.demo

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.MutableESPFlowController
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.BaseConnection
import io.github.developrofthings.kespl.bluetooth.connection.DEMO_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.collection.MutableByteList
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.ResponseBatteryVoltage
import io.github.developrofthings.kespl.packet.data.ResponseSavvyStatus
import io.github.developrofthings.kespl.packet.data.ResponseSerialNumber
import io.github.developrofthings.kespl.packet.data.ResponseVehicleSpeed
import io.github.developrofthings.kespl.packet.data.ResponseVersion
import io.github.developrofthings.kespl.packet.data.sweep.ResponseMaxSweepIndex
import io.github.developrofthings.kespl.packet.data.sweep.ResponseSweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.ResponseSweepSection
import io.github.developrofthings.kespl.packet.data.user.ResponseUserBytes
import io.github.developrofthings.kespl.packet.destinationId
import io.github.developrofthings.kespl.packet.packetId
import io.github.developrofthings.kespl.packet.tryAssemblyESPByteArrayFromLegacyDataBuffer
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@Factory(binds = [IConnection::class])
@Named(DEMO_CONNECTION_QUALIFIER)
internal class DemoConnection(
    flowController: MutableESPFlowController,
    logger: PlatformLogger,
    @InjectedParam connectionScope: CoroutineScope,
) : BaseConnection(
    flowController = flowController,
    logger = logger,
    connectionScope = connectionScope
) {

    private var _demoESPJob: Job? = null

    override val connectionType: V1cType get() = V1cType.Demo

    private val _deviceVersions = mutableMapOf<ESPDevice, ResponseVersion>()
    private val _deviceSerials = mutableMapOf<ESPDevice, ResponseSerialNumber>()
    private var _userBytes: ResponseUserBytes? = null
    private var _savvyStatus: ResponseSavvyStatus? = null
    private var _vehicleSpeed: ResponseVehicleSpeed? = null
    private var _maxSweepIndex: ResponseMaxSweepIndex? = null
    private var _batteryVoltage: ResponseBatteryVoltage? = null
    private var _sweepSections: MutableList<ResponseSweepSection> = mutableListOf()
    private var _sweepDefinitions: MutableList<ResponseSweepDefinition> = mutableListOf()

    init {
        // Demo mode cannot use echoing
        canEchoQueue = false
    }

    override suspend fun scan(scanMode: ESPScanMode): Flow<V1connectionScanResult> = emptyFlow()

    override suspend fun performConnection(v1c: V1connection, directConnect: Boolean): Boolean {
        if (v1c !is V1connection.Demo) return false
        _demoESPJob = connectionScope.demoMode(v1c.demoESP)
        return true
    }

    override suspend fun performDisconnect() {
        _demoESPJob?.cancel()
        _deviceVersions.clear()
        _deviceSerials.clear()
        onDisconnected()
    }

    override suspend fun writeBytes(bytes: ByteArray): Boolean {
        when (bytes.packetId) {
            ESPPacketId.ReqVersion -> {
                val destination = bytes.destinationId
                _deviceVersions[destination]?.let {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqSerialNumber -> {
                val destination = bytes.destinationId
                _deviceSerials[destination]?.let {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqUserBytes -> {
                _userBytes?.let {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqSavvyStatus -> {
                _savvyStatus?.let {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqVehicleSpeed -> {
                _vehicleSpeed?.let {
                    _espData.emit(it.bytes)
                }

            }

            ESPPacketId.ReqSweepSections -> {
                _sweepSections.forEach {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqAllSweepDefinitions -> {
                _sweepDefinitions.forEach {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqMaxSweepIndex -> {
                _maxSweepIndex?.let {
                    _espData.emit(it.bytes)
                }
            }

            ESPPacketId.ReqBatteryVoltage -> {
                _batteryVoltage?.let {
                    _espData.emit(it.bytes)
                }
            }
            /*
                All Packet Id's are defined here instead of using an 'else' case so that if a new
                PacketId is added the code will not compile until a new case is added.
            */

            ESPPacketId.RespVersion,
            ESPPacketId.RespSerialNumber,
            ESPPacketId.RespUserBytes,
            ESPPacketId.ReqWriteUserBytes,
            ESPPacketId.ReqFactoryDefault,
            ESPPacketId.ReqWriteSweepDefinition,
            ESPPacketId.RespSweepDefinition,
            ESPPacketId.ReqDefaultSweeps,
            ESPPacketId.RespMaxSweepIndex,
            ESPPacketId.RespSweepWriteResult,
            ESPPacketId.RespSweepSections,
            ESPPacketId.ReqDefaultSweepDefinitions,
            ESPPacketId.RespDefaultSweepDefinitions,
            ESPPacketId.InfDisplayData,
            ESPPacketId.ReqTurnOffMainDisplay,
            ESPPacketId.ReqTurnOnMainDisplay,
            ESPPacketId.ReqMuteOn,
            ESPPacketId.ReqMuteOff,
            ESPPacketId.ReqChangeMode,
            ESPPacketId.ReqCurrentVolume,
            ESPPacketId.RespCurrentVolume,
            ESPPacketId.ReqWriteVolume,
            ESPPacketId.ReqAbortAudioDelay,
            ESPPacketId.ReqDisplayCurrentVolume,
            ESPPacketId.ReqAllVolume,
            ESPPacketId.RespAllVolume,
            ESPPacketId.ReqStartAlertData,
            ESPPacketId.ReqStopAlertData,
            ESPPacketId.RespAlertData,
            ESPPacketId.RespDataReceived,
            ESPPacketId.RespBatteryVoltage,
            ESPPacketId.RespUnsupportedPacket,
            ESPPacketId.RespRequestNotProcessed,
            ESPPacketId.InfV1Busy,
            ESPPacketId.RespDataError,
            ESPPacketId.RespSavvyStatus,
            ESPPacketId.RespVehicleSpeed,
            ESPPacketId.ReqOverrideThumbwheel,
            ESPPacketId.ReqSetSavvyUnmuteEnable,
            ESPPacketId.UnknownPacketType -> { /* NO-OP */
            }
        }
        return true
    }

    private fun CoroutineScope.demoMode(
        demoESPData: String
    ) = launch(Dispatchers.IO) {
        val demoData = preProcessESPData(demoESPData)
            // For the time being drop all comments
            .filterNot { it is DemoData.Comment }

        // Cache esp response
        preProcessESPResponses(
            espData = demoData
                .filterIsInstance<DemoData.ESPData>()
        )
        // Pre process the esp data
        while (isActive) {
            demoData.forEach { datum ->
                when (datum) {
                    is DemoData.Comment -> { /* NO-OP */
                    }

                    is DemoData.ESPData -> processESPData(datum.packet.bytes)
                    is DemoData.Notification -> _notificationData.emit(datum.message)
                }
                delay(68L)
            }
        }
    }

    private fun preProcessESPResponses(espData: List<DemoData.ESPData>) {
        espData.map { it.packet }.forEach { packet ->
            when (packet.packetIdentifier) {
                ESPPacketId.RespVersion -> {
                    val origin = packet.originatorIdentifier
                    _deviceVersions[origin] = packet
                    return
                }

                ESPPacketId.RespSerialNumber -> {
                    val origin = packet.originatorIdentifier
                    _deviceSerials[origin] = packet
                    return
                }

                ESPPacketId.RespSavvyStatus -> {
                    _savvyStatus = packet
                    return
                }

                ESPPacketId.RespSweepSections -> {
                    if (_sweepSections.size < 2) _sweepSections.add(packet)
                    return
                }

                ESPPacketId.RespSweepDefinition -> {
                    if (_sweepDefinitions.size < 6) _sweepDefinitions.add(packet)
                    return
                }

                ESPPacketId.RespMaxSweepIndex -> {
                    _maxSweepIndex = packet
                    return
                }

                ESPPacketId.ReqBatteryVoltage -> {
                    _batteryVoltage = packet
                    return
                }

                ESPPacketId.RespVehicleSpeed -> {
                    _vehicleSpeed = packet
                    return
                }

                ESPPacketId.RespUserBytes -> {
                    _userBytes = packet
                    return
                }

                ESPPacketId.ReqVersion,
                ESPPacketId.ReqSerialNumber,
                ESPPacketId.ReqUserBytes,
                ESPPacketId.ReqWriteUserBytes,
                ESPPacketId.ReqFactoryDefault,
                ESPPacketId.ReqWriteSweepDefinition,
                ESPPacketId.ReqAllSweepDefinitions,
                ESPPacketId.ReqDefaultSweeps,
                ESPPacketId.ReqMaxSweepIndex,
                ESPPacketId.RespSweepWriteResult,
                ESPPacketId.ReqSweepSections,
                ESPPacketId.ReqDefaultSweepDefinitions,
                ESPPacketId.RespDefaultSweepDefinitions,
                ESPPacketId.InfDisplayData,
                ESPPacketId.ReqTurnOffMainDisplay,
                ESPPacketId.ReqTurnOnMainDisplay,
                ESPPacketId.ReqMuteOn,
                ESPPacketId.ReqMuteOff,
                ESPPacketId.ReqChangeMode,
                ESPPacketId.ReqCurrentVolume,
                ESPPacketId.RespCurrentVolume,
                ESPPacketId.ReqWriteVolume,
                ESPPacketId.ReqAbortAudioDelay,
                ESPPacketId.ReqDisplayCurrentVolume,
                ESPPacketId.ReqAllVolume,
                ESPPacketId.RespAllVolume,
                ESPPacketId.ReqStartAlertData,
                ESPPacketId.ReqStopAlertData,
                ESPPacketId.RespAlertData,
                ESPPacketId.RespDataReceived,
                ESPPacketId.RespBatteryVoltage,
                ESPPacketId.RespUnsupportedPacket,
                ESPPacketId.RespRequestNotProcessed,
                ESPPacketId.InfV1Busy,
                ESPPacketId.RespDataError,
                ESPPacketId.ReqSavvyStatus,
                ESPPacketId.ReqVehicleSpeed,
                ESPPacketId.ReqOverrideThumbwheel,
                ESPPacketId.ReqSetSavvyUnmuteEnable,
                ESPPacketId.UnknownPacketType -> { /* NO-OP */
                }
            }
        }
    }

    private fun preProcessESPData(demoESPData: String): List<DemoData> = buildList<DemoData> {
        val mByteBuffer = MutableByteList(initialCapacity = MutableByteList.DEFAULT_CAPACITY)
        var step = 0
        while (step < demoESPData.length) {
            val endOfLineLoc = demoESPData.indexOf("\n", step)
            // Get the whole line of byte data and store it into a string.
            val currentLine = demoESPData.substring(step, endOfLineLoc)

            if (currentLine.startsWith(doubleSlashesSymbol)) {
                add(DemoData.Comment(currentLine.substringAfter(doubleSlashesSymbol)))
            } else if (currentLine[0] == lessThanSymbol) {
                val startLoc = currentLine.indexOf(colonSymbol)
                val endLoc = currentLine.indexOf(greaterThanSymbol)
                add(DemoData.Notification(message = currentLine.substring(startLoc + 1, endLoc)))
            } else {
                val bytes = convertStringToByteArray(currentLine)
                mByteBuffer.addBytes(bytes)

                tryAssemblyESPByteArrayFromLegacyDataBuffer(
                    buffer = mByteBuffer,
                    v1Type = ESPDevice.ValentineOne.Checksum
                )?.let {
                    add(DemoData.ESPData(packet = ESPPacket(it)))
                }
            }
            // Offset the step variable by location of the first new line character.
            step = endOfLineLoc + 1
        }
    }
}

internal sealed interface DemoData {

    data class Comment(val comment: String) : DemoData

    data class Notification(val message: String) : DemoData

    @JvmInline
    value class ESPData(val packet: ESPPacket) : DemoData
}

private fun convertStringToByteArray(hexData: String): ByteArray {
    val splitData = hexData.split(DEMO_FILE_SPACE_CHARACTER)
    // Create a byte array the size of the split demo data array.
    val espData = ByteArray(splitData.size)
    var i = 0
    for (c in splitData) {
        // Convert the string byte value to an actual byte.
        espData[i++] = ((c[0].digitToInt(16) shl 4) + c[1].digitToInt(16)).toByte()
    }
    return espData
}

private const val DEMO_FILE_SPACE_CHARACTER = " "
private const val doubleSlashesSymbol = "//"
private const val lessThanSymbol = '<'
private const val colonSymbol = ":"
private const val greaterThanSymbol = ">"