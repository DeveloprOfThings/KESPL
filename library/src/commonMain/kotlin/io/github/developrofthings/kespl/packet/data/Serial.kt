package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.espPayloadToString

typealias ResponseSerialNumber = ESPPacket

typealias SerialNumber = String

fun ESPPacket.isSerialNumberResponse(): Boolean = packetIdentifier == ESPPacketId.RespSerialNumber

fun ResponseSerialNumber.serialNumber(): SerialNumber = this.bytes.serialNumber()

fun ByteArray.serialNumber(): SerialNumber = espPayloadToString(SERIAL_LENGTH)

internal const val SERIAL_LENGTH: Int = 10