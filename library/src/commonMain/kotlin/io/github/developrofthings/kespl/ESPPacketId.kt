package io.github.developrofthings.kespl

sealed class ESPPacketId(val id: Byte) {
    /**Packet Identifier for Version Request */
    data object ReqVersion: ESPPacketId(0x01)

    /**Packet Identifier for Version Response */
    data object RespVersion: ESPPacketId(0x02)

    /**Packet Identifier for Serial Number Request */
    data object ReqSerialNumber: ESPPacketId(0x03)

    /**Packet Identifier for Serial Number Response */
    data object RespSerialNumber: ESPPacketId(0x04)

    /**Packet Identifier for UserBytes Request */
    data object ReqUserBytes: ESPPacketId(0x11)

    /**Packet Identifier for UserBytes Response */
    data object RespUserBytes: ESPPacketId(0x12)

    /**Packet Identifier for Write UserBytes Request */
    data object ReqWriteUserBytes: ESPPacketId(0x13)

    /**Packet Identifier for Factory Default Request */
    data object ReqFactoryDefault: ESPPacketId(0x14)

    /**Packet Identifier for writing a Sweep Definition Request */
    data object ReqWriteSweepDefinition: ESPPacketId(0x15)

    /**Packet Identifier for all Sweep Definiton Request */
    data object ReqAllSweepDefinitions: ESPPacketId(0x16)

    /**Packet Identifier for Sweep Definition Response */
    data object RespSweepDefinition: ESPPacketId(0x17)

    /**Packet Identifier for Default Sweep Request */
    data object ReqDefaultSweeps: ESPPacketId(0x18)

    /**Packet Identifier for Max Sweep Index Request */
    data object ReqMaxSweepIndex: ESPPacketId(0x19)

    /**Packet Identifier for Max Sweep Index Response */
    data object RespMaxSweepIndex: ESPPacketId(0x20)

    /**Packet Identifier for Sweep Write Result Response */
    data object RespSweepWriteResult: ESPPacketId(0x21)

    /**Packet Identifier for Sweep Sections Request */
    data object ReqSweepSections: ESPPacketId(0x22)

    /**Packet Identifier for Sweep Sections Response */
    data object RespSweepSections: ESPPacketId(0x23)

    /**Packet Identifier for Default Sweep Definitions Request */
    data object ReqDefaultSweepDefinitions: ESPPacketId(0x24)

    /**Packet Identifier for Default Sweep Definitions  Response */
    data object RespDefaultSweepDefinitions: ESPPacketId(0x25)

    /**Packet Identifier for InfDisplayData */
    data object InfDisplayData: ESPPacketId(0x31)

    /**Packet Identifier for Turn Off Main Display Request */
    data object ReqTurnOffMainDisplay: ESPPacketId(0x32)

    /**Packet Identifier for Turn On Main Display Request */
    data object ReqTurnOnMainDisplay: ESPPacketId(0x33)

    /**Packet Identifier for Mute On Request */
    data object ReqMuteOn: ESPPacketId(0x34)

    /**Packet Identifier for Mute Off Request */
    data object ReqMuteOff: ESPPacketId(0x35)

    /**Packet Identifier for Change Mode Request */
    data object ReqChangeMode: ESPPacketId(0x36)

    /**Packet Identifier for the current volume settings Request */
    data object ReqCurrentVolume: ESPPacketId(0x37)

    /**Packet Identifier for the current volume setting Response */
    data object RespCurrentVolume: ESPPacketId(0x38)

    /**Packet Identifier for updating the current volume settings Request */
    data object ReqWriteVolume: ESPPacketId(0x39)

    /**
     * Packet Identifier used to request that the Valentine One stops waiting for the silent period
     * described in Table 9.4, which causes the Valentine One to play the audio for the primary
     * alert sooner than it normally would.
     *
     * @since V4.1035
     */
    data object ReqAbortAudioDelay: ESPPacketId(0x3A)

    /**
     * Packet Identifier used to request that the Valentine One displays the current volume. This is
     * equivalent to tapping one of the volume buttons on the V1 Gen2.
     *
     * @since V4.1036
     */
    data object ReqDisplayCurrentVolume: ESPPacketId(0x3B)

    /**
     * Packet Identifier used to request both the current and saved volume settings in the Valentine
     * One
     *
     * @since V4.1037
     */
    data object ReqAllVolume: ESPPacketId(0x3C)

    /**
     * Packet Identifier used by the Valentine One to respond to a [ReqAllVolume] request.
     *
     * @since V4.1037
     */
    data object RespAllVolume: ESPPacketId(0x3D)

    /**Packet Identifier for Start Alert Data Request */
    data object ReqStartAlertData: ESPPacketId(0x41)

    /**Packet Identifier for Stop Alert Data Request */
    data object ReqStopAlertData: ESPPacketId(0x42)

    /**Packet Identifier for Alert Data Response */
    data object RespAlertData: ESPPacketId(0x43)

    /**Packet Identifier for Response Data */
    data object RespDataReceived: ESPPacketId(0x61)

    /**Packet Identifier for Battery Voltage Request */
    data object ReqBatteryVoltage: ESPPacketId(0x62)

    /**Packet Identifier for Battery Voltage Response */
    data object RespBatteryVoltage: ESPPacketId(0x63)

    /**Packet Identifier for Unsupported Packet Response */
    data object RespUnsupportedPacket: ESPPacketId(0x64)

    /**Packet Identifier for Request Not Processed Response */
    data object RespRequestNotProcessed: ESPPacketId(0x65)

    /**Packet Identifier for InfV1Busy */
    data object InfV1Busy: ESPPacketId(0x66)

    /**Packet Identifier for Data Error Response */
    data object RespDataError: ESPPacketId(0x67)

    /**Packet Identifier for SAVVYStatus Request */
    data object ReqSavvyStatus: ESPPacketId(0x71)

    /**Packet Identifier for Savvy Status Response */
    data object RespSavvyStatus: ESPPacketId(0x72)

    /**Packet Identifier for Vehicle Speed Request */
    data object ReqVehicleSpeed: ESPPacketId(0x73)

    /**Packet Identifier for Vehicle Speed Response */
    data object RespVehicleSpeed: ESPPacketId(0x74)

    /**Packet Identifier for Override Thumbwheel Request */
    data object ReqOverrideThumbwheel: ESPPacketId(0x75)

    /**Packet Identifier for Savvy Unmute Enable Request */
    data object ReqSetSavvyUnmuteEnable: ESPPacketId(0x76)

    /**Packet Identifier for Unknown Packet */
    data object UnknownPacketType: ESPPacketId((0x100).toByte())
}

fun getPacketId(packetIdByte: Byte): ESPPacketId = when (packetIdByte) {
    ESPPacketId.ReqVersion.id -> ESPPacketId.ReqVersion
    ESPPacketId.RespVersion.id -> ESPPacketId.RespVersion
    ESPPacketId.ReqSerialNumber.id -> ESPPacketId.ReqSerialNumber
    ESPPacketId.RespSerialNumber.id -> ESPPacketId.RespSerialNumber
    ESPPacketId.ReqUserBytes.id -> ESPPacketId.ReqUserBytes
    ESPPacketId.RespUserBytes.id -> ESPPacketId.RespUserBytes
    ESPPacketId.ReqWriteUserBytes.id -> ESPPacketId.ReqWriteUserBytes
    ESPPacketId.ReqFactoryDefault.id -> ESPPacketId.ReqFactoryDefault
    ESPPacketId.ReqWriteSweepDefinition.id -> ESPPacketId.ReqWriteSweepDefinition
    ESPPacketId.ReqAllSweepDefinitions.id -> ESPPacketId.ReqAllSweepDefinitions
    ESPPacketId.RespSweepDefinition.id -> ESPPacketId.RespSweepDefinition
    ESPPacketId.ReqDefaultSweeps.id -> ESPPacketId.ReqDefaultSweeps
    ESPPacketId.ReqMaxSweepIndex.id -> ESPPacketId.ReqMaxSweepIndex
    ESPPacketId.RespMaxSweepIndex.id -> ESPPacketId.RespMaxSweepIndex
    ESPPacketId.RespSweepWriteResult.id -> ESPPacketId.RespSweepWriteResult
    ESPPacketId.ReqSweepSections.id -> ESPPacketId.ReqSweepSections
    ESPPacketId.RespSweepSections.id -> ESPPacketId.RespSweepSections
    ESPPacketId.ReqDefaultSweepDefinitions.id -> ESPPacketId.ReqDefaultSweepDefinitions
    ESPPacketId.RespDefaultSweepDefinitions.id -> ESPPacketId.RespDefaultSweepDefinitions
    ESPPacketId.InfDisplayData.id -> ESPPacketId.InfDisplayData
    ESPPacketId.ReqTurnOffMainDisplay.id -> ESPPacketId.ReqTurnOffMainDisplay
    ESPPacketId.ReqTurnOnMainDisplay.id -> ESPPacketId.ReqTurnOnMainDisplay
    ESPPacketId.ReqMuteOn.id -> ESPPacketId.ReqMuteOn
    ESPPacketId.ReqMuteOff.id -> ESPPacketId.ReqMuteOff
    ESPPacketId.ReqChangeMode.id -> ESPPacketId.ReqChangeMode
    ESPPacketId.ReqCurrentVolume.id -> ESPPacketId.ReqCurrentVolume
    ESPPacketId.RespCurrentVolume.id -> ESPPacketId.RespCurrentVolume
    ESPPacketId.ReqWriteVolume.id -> ESPPacketId.ReqWriteVolume
    ESPPacketId.ReqStartAlertData.id -> ESPPacketId.ReqStartAlertData
    ESPPacketId.ReqStopAlertData.id -> ESPPacketId.ReqStopAlertData
    ESPPacketId.RespAlertData.id -> ESPPacketId.RespAlertData
    ESPPacketId.RespDataReceived.id -> ESPPacketId.RespDataReceived
    ESPPacketId.ReqBatteryVoltage.id -> ESPPacketId.ReqBatteryVoltage
    ESPPacketId.RespBatteryVoltage.id -> ESPPacketId.RespBatteryVoltage
    ESPPacketId.RespUnsupportedPacket.id -> ESPPacketId.RespUnsupportedPacket
    ESPPacketId.RespRequestNotProcessed.id -> ESPPacketId.RespRequestNotProcessed
    ESPPacketId.InfV1Busy.id -> ESPPacketId.InfV1Busy
    ESPPacketId.RespDataError.id -> ESPPacketId.RespDataError
    ESPPacketId.ReqSavvyStatus.id -> ESPPacketId.ReqSavvyStatus
    ESPPacketId.RespSavvyStatus.id -> ESPPacketId.RespSavvyStatus
    ESPPacketId.ReqVehicleSpeed.id -> ESPPacketId.ReqVehicleSpeed
    ESPPacketId.RespVehicleSpeed.id -> ESPPacketId.RespVehicleSpeed
    ESPPacketId.ReqOverrideThumbwheel.id -> ESPPacketId.ReqOverrideThumbwheel
    ESPPacketId.ReqSetSavvyUnmuteEnable.id -> ESPPacketId.ReqSetSavvyUnmuteEnable
    ESPPacketId.UnknownPacketType.id -> ESPPacketId.UnknownPacketType
    else -> ESPPacketId.UnknownPacketType
}