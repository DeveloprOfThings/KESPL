package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket

typealias ResponseVehicleSpeed = ESPPacket

val ByteArray.speed: Int get() = this[PAYLOAD_START_IDX + 0].toInt()

val ResponseVehicleSpeed.speed: Int get() = this.bytes.speed