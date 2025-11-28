package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.espPayloadToString

typealias ResponseVersion = ESPPacket

typealias Version = String

fun ByteArray.version(): Version = espPayloadToString(VERSION_LENGTH)

fun ByteArray.versionAsDouble(): Double = espPayloadToString(VERSION_LENGTH).asDouble()

fun ResponseVersion.version(): Version = this.bytes.version()

fun ResponseVersion.versionDouble(): Double = version().asDouble()

fun Version.asDouble(): Double = this.substring(startIndex = 1).toDouble()

internal const val VERSION_LENGTH: Int = 7