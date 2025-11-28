package io.github.developrofthings.kespl

import kotlin.experimental.or

sealed class ESPDevice(open val id: Byte) {

    val destinationIdentifier: Byte get() = id or DEST_INDENTIFIER_BASE_CONST

    val originatorIdentifier: Byte get() =  id or ORIG_INDENTIFIER_BASE_CONST

    /** Represents either a Concealed Display or Tech Display ESP**/
    data object RemoteDisplay: ESPDevice(0x00)

    /**Remote Audio ESP device */
    data object RemoteAudio : ESPDevice(0x01)

    /**Savvy ESP device */
    data object SAVVY : ESPDevice(0x02)

    /**Third party assigned ESP device #1 */
    data object ThirdParty1 : ESPDevice(0x03)

    /**Third party assigned ESP device #2 */
    data object ThirdParty2 : ESPDevice(0x04)

    /** Third party assigned ESP device #3 */
    data object ThirdParty3 : ESPDevice(0x05)

    /** V1connection ESP device */
    data object V1connection : ESPDevice(0x06)

    /** Reserved ESP device ID */
    data object Reserved : ESPDevice(0x07)

    /** General ESP device. Packets with this destination ID are suitable for anyone */
    data object GeneralBroadcast : ESPDevice(0x08)

    sealed class ValentineOne(override val id: Byte) : ESPDevice(id) {
        /**Valentine One W/o Checksum */
        data object NoChecksum : ValentineOne(0x09)

        /**Valentine One W/ Checksum */
        data object Checksum : ValentineOne(0x0A)

        /** Legacy Valentine One */
        data object Legacy : ValentineOne(0x98.toByte())

        data object Unknown : ValentineOne(0x9F.toByte())
    }

    class Custom(customDeviceId: Byte) : ESPDevice(customDeviceId)

    /**Unknown ESP Device */
    data object UnknownDevice : ESPDevice(0x99.toByte())
}

fun getValentineOne(origin: Byte): ESPDevice.ValentineOne = when (origin) {
    ESPDevice.ValentineOne.NoChecksum.originatorIdentifier -> ESPDevice.ValentineOne.NoChecksum
    ESPDevice.ValentineOne.Checksum.originatorIdentifier -> ESPDevice.ValentineOne.Checksum
    ESPDevice.ValentineOne.Legacy.originatorIdentifier -> ESPDevice.ValentineOne.Legacy
    else -> ESPDevice.ValentineOne.Unknown
}

val ESPDevice.isV1: Boolean
    get() = this is ESPDevice.ValentineOne