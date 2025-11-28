package io.github.developrofthings.helloV1

import androidx.compose.runtime.Composable
import io.github.developrofthings.helloV1.ui.byteValue
import io.github.developrofthings.kespl.ESPDevice
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.custom
import hellov1.composeapp.generated.resources.general_broadcast
import hellov1.composeapp.generated.resources.remote_audio
import hellov1.composeapp.generated.resources.remote_display
import hellov1.composeapp.generated.resources.reserved
import hellov1.composeapp.generated.resources.savvy
import hellov1.composeapp.generated.resources.third_party_1
import hellov1.composeapp.generated.resources.third_party_2
import hellov1.composeapp.generated.resources.third_party_3
import hellov1.composeapp.generated.resources.unknown
import hellov1.composeapp.generated.resources.v1connection
import hellov1.composeapp.generated.resources.valentine_one_check
import hellov1.composeapp.generated.resources.valentine_one_legacy
import hellov1.composeapp.generated.resources.valentine_one_no_check
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed class TargetESPDevice(open val nameRes: StringResource) {

    @Composable
    fun label() = stringResource(nameRes)

    /** Represents either a Concealed Display or Tech Display ESP**/
    data object RemoteDisplay : TargetESPDevice(Res.string.remote_display)

    /**Remote Audio ESP device */
    data object RemoteAudio : TargetESPDevice(Res.string.remote_audio)

    /**Savvy ESP device */
    data object SAVVY : TargetESPDevice(Res.string.savvy)

    /**Third party assigned ESP device #1 */
    data object ThirdParty1 : TargetESPDevice(Res.string.third_party_1)

    /**Third party assigned ESP device #2 */
    data object ThirdParty2 : TargetESPDevice(Res.string.third_party_2)

    /** Third party assigned ESP device #3 */
    data object ThirdParty3 : TargetESPDevice(Res.string.third_party_3)

    /** V1connection ESP device */
    data object V1connection : TargetESPDevice(Res.string.v1connection)

    /** Reserved ESP device ID */
    data object Reserved : TargetESPDevice(Res.string.reserved)

    /** General ESP device. Packets with this destination ID are suitable for anyone */
    data object GeneralBroadcast : TargetESPDevice(Res.string.general_broadcast)

    sealed class ValentineOne(override val nameRes: StringResource) : TargetESPDevice(nameRes) {
        /**Valentine One W/o Checksum */
        data object NoChecksum : ValentineOne(Res.string.valentine_one_no_check)

        /**Valentine One W/ Checksum */
        data object Checksum : ValentineOne(Res.string.valentine_one_check)

        /** Legacy Valentine One */
        data object Legacy : ValentineOne(Res.string.valentine_one_legacy)
    }

    data object Custom : TargetESPDevice(Res.string.custom)

    /**Unknown ESP Device */
    data object UnknownDevice : TargetESPDevice(Res.string.unknown)
}

fun TargetESPDevice.resolveESPDevice(customId: String): ESPDevice =
    when (this) {
        TargetESPDevice.Custom -> ESPDevice.Custom(
            customDeviceId = if (customId.isEmpty()) 0x00 else customId.byteValue()
        )

        TargetESPDevice.GeneralBroadcast -> ESPDevice.GeneralBroadcast
        TargetESPDevice.RemoteAudio -> ESPDevice.RemoteAudio
        TargetESPDevice.RemoteDisplay -> ESPDevice.RemoteDisplay
        TargetESPDevice.Reserved -> ESPDevice.Reserved
        TargetESPDevice.SAVVY -> ESPDevice.SAVVY
        TargetESPDevice.ThirdParty1 -> ESPDevice.ThirdParty1
        TargetESPDevice.ThirdParty2 -> ESPDevice.ThirdParty2
        TargetESPDevice.ThirdParty3 -> ESPDevice.ThirdParty3
        TargetESPDevice.UnknownDevice -> ESPDevice.UnknownDevice
        TargetESPDevice.V1connection -> ESPDevice.V1connection
        TargetESPDevice.ValentineOne.Checksum -> ESPDevice.ValentineOne.Checksum
        TargetESPDevice.ValentineOne.Legacy -> ESPDevice.ValentineOne.Legacy
        TargetESPDevice.ValentineOne.NoChecksum -> ESPDevice.ValentineOne.NoChecksum
    }