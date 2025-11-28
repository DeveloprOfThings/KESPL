package io.github.developrofthings.helloV1

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode

sealed interface ESPDataRequest {

    data class Version(val targetDevice: ESPDevice) : ESPDataRequest

    data class Serial(val targetDevice: ESPDevice) : ESPDataRequest

    data class ReadUserBytes(val targetDevice: ESPDevice) : ESPDataRequest

    class WriteUserBytes(val targetDevice: ESPDevice, val userBytes: ByteArray) : ESPDataRequest

    data class RestoreDefaults(val targetDevice: ESPDevice) : ESPDataRequest

    sealed interface V1 : ESPDataRequest {

        data class DisplayOn(val on: Boolean) : V1

        data class Mute(val muted: Boolean) : V1

        data class ChangeMode(val v1Mode: V1Mode): V1

        class AlertTable(val on: Boolean) : V1

        data object BatteryVoltage : V1
    }

    sealed interface SAVVY : ESPDataRequest {

        data object SAVVYStatus: SAVVY

        data object VehicleSpeed: SAVVY

        data class OverrideThumbwheel(val speed: Int): SAVVY

        data class Unmute(val enableUnmuting: Boolean): SAVVY
    }
}