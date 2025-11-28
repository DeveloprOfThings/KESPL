package io.github.developrofthings.helloV1.data.repository

import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.helloV1.ui.toMPH
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.allVolumes
import io.github.developrofthings.kespl.packet.data.batteryVoltage
import io.github.developrofthings.kespl.packet.data.currentVolume
import io.github.developrofthings.kespl.packet.data.serialNumber
import io.github.developrofthings.kespl.packet.data.speed
import io.github.developrofthings.kespl.packet.data.status
import io.github.developrofthings.kespl.packet.data.version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ESPDataLogRepositoryImpl(
    val espService: IESPService,
    val coroutineScope: CoroutineScope,
) : ESPDataLogRepository {

    private val _filterAlertData = MutableStateFlow(true)
    private val _filterDisplayData = MutableStateFlow(true)
    private val _log = MutableStateFlow<List<String>>(emptyList())
    override val filterAlertData: StateFlow<Boolean>
        get() = _filterAlertData.asStateFlow()
    override val filterDisplayData: StateFlow<Boolean>
        get() = _filterDisplayData.asStateFlow()

    override val log: StateFlow<List<String>>
        get() = _log.asStateFlow()

    override fun setDisplayDataFiltering(enabled: Boolean) {
        _filterDisplayData.value = enabled
    }

    override fun setAlertDataFiltering(enabled: Boolean) {
        _filterAlertData.value = enabled
    }

    init {
        espService
            .espData
            .onEach(::handleESPData)
            .launchIn(coroutineScope)
    }

    private fun handleESPData(packet: ESPPacket) {
        when (packet.packetIdentifier) {
            ESPPacketId.InfDisplayData -> {
                if (filterDisplayData.value) addLog(
                    """
                            Display Data:
                            $packet
                        """.trimIndent()
                )
            }

            ESPPacketId.RespAlertData -> {
                if (filterAlertData.value) addLog(
                    """
                    Alert Data:
                    $packet
                """.trimIndent()
                )
            }

            ESPPacketId.RespUserBytes -> {
                addLog("V1 user bytes: ${packet.toPayloadHexString(6)}")
            }

            ESPPacketId.RespVersion -> {
                addLog("${packet.originatorIdentifier} Version: ${packet.version()}")
            }

            ESPPacketId.RespSerialNumber -> {
                addLog("${packet.originatorIdentifier} Serial Number: ${packet.serialNumber()}")
            }

            ESPPacketId.RespCurrentVolume -> {
                packet.currentVolume().also {
                    addLog("V1 Volume: main = ${it.mainVolume} mute = ${it.mutedVolume}")
                }
            }

            ESPPacketId.RespAllVolume -> {
                packet.allVolumes().also {
                    addLog(
                        """
                            V1 Volume:
                                Current Main Vol. = ${it.currentVolume.mainVolume}
                                Current Muted Vol. = ${it.currentVolume.mutedVolume}
                                Saved Main Vol. = ${it.savedVolume.mainVolume}
                                Saved Muted Vol. = ${it.savedVolume.mutedVolume}
                        """.trimIndent()
                    )
                }
            }

            ESPPacketId.RespVehicleSpeed -> {
                addLog("Vehicle Speed: ${packet.speed} KPH")
            }

            ESPPacketId.RespSavvyStatus -> {
                packet.status().also {
                    addLog(
                        """
                            SAVVY Status: {
                                Speed threshold = ${it.currentSpeedThresholdKPH} KPH (${it.currentSpeedThresholdKPH.toMPH()} MPH)
                                Threshold overridden = ${it.isThresholdUserOverride}
                                Unmute enabled = ${it.isUnmuteEnabled}
                            }
                        """.trimIndent()
                    )
                }
            }


            ESPPacketId.RespBatteryVoltage -> {
                addLog("Battery Voltage: ${packet.batteryVoltage()}")
            }

            ESPPacketId.ReqVersion,
            ESPPacketId.ReqSerialNumber,
            ESPPacketId.ReqUserBytes,
            ESPPacketId.ReqWriteUserBytes,
            ESPPacketId.ReqFactoryDefault,
            ESPPacketId.ReqWriteSweepDefinition,
            ESPPacketId.ReqAllSweepDefinitions,
            ESPPacketId.RespSweepDefinition,
            ESPPacketId.ReqDefaultSweeps,
            ESPPacketId.ReqMaxSweepIndex,
            ESPPacketId.RespMaxSweepIndex,
            ESPPacketId.RespSweepWriteResult,
            ESPPacketId.ReqSweepSections,
            ESPPacketId.RespSweepSections,
            ESPPacketId.ReqDefaultSweepDefinitions,
            ESPPacketId.RespDefaultSweepDefinitions,
            ESPPacketId.ReqTurnOffMainDisplay,
            ESPPacketId.ReqTurnOnMainDisplay,
            ESPPacketId.ReqMuteOn,
            ESPPacketId.ReqMuteOff,
            ESPPacketId.ReqChangeMode,
            ESPPacketId.ReqCurrentVolume,
            ESPPacketId.ReqWriteVolume,
            ESPPacketId.ReqAbortAudioDelay,
            ESPPacketId.ReqDisplayCurrentVolume,
            ESPPacketId.ReqAllVolume,
            ESPPacketId.ReqStartAlertData,
            ESPPacketId.ReqStopAlertData,
            ESPPacketId.RespDataReceived,
            ESPPacketId.ReqBatteryVoltage,
            ESPPacketId.RespUnsupportedPacket,
            ESPPacketId.RespRequestNotProcessed,
            ESPPacketId.InfV1Busy,
            ESPPacketId.RespDataError,
            ESPPacketId.ReqSavvyStatus,
            ESPPacketId.ReqVehicleSpeed,
            ESPPacketId.ReqOverrideThumbwheel,
            ESPPacketId.ReqSetSavvyUnmuteEnable,
            ESPPacketId.UnknownPacketType,
                -> addLog(packet.toString())
        }
    }

    override fun addLog(log: String) {
        _log.update {
            it.toMutableList().apply { add(log) }
        }
    }

    override fun clearLog() {
        _log.update {
            it.toMutableList().apply {
                clear()
            }
        }
    }
}

fun ESPPacket.toPayloadHexString(length: Int): String = this.bytes.toHexString(
    startIndex = PAYLOAD_START_IDX,
    endIndex = PAYLOAD_START_IDX + length,
    format = payloadHexFormat
)

private val payloadHexFormat = HexFormat {
    bytes {
        byteSeparator = " " // Defines the separator between individual byte representations
        upperCase = true // Optional: Use uppercase hex digits (A-F)
    }
}