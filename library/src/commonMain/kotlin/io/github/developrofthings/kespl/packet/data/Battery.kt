package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket

typealias ResponseBatteryVoltage = ESPPacket

fun ByteArray.batteryVoltage(): String =
    "${this[PAYLOAD_START_IDX + 0].toInt()}.${this[PAYLOAD_START_IDX + 1].toInt()}"

fun ResponseBatteryVoltage.batteryVoltage(): String =
    this.bytes.batteryVoltage()