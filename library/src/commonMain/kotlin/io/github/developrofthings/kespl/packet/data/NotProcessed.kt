package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.getPacketId
import io.github.developrofthings.kespl.packet.ESPPacket

typealias ResponseRequestNotProcessed = ESPPacket

fun ResponseRequestNotProcessed.notProcessedPacketId(): ESPPacketId = getPacketId(this[PAYLOAD_START_IDX])