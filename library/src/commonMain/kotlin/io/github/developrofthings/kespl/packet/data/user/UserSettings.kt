package io.github.developrofthings.kespl.packet.data.user

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.fullByte
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.utilities.V1_GEN_2_PLATFORM_BASELINE_VERSION

typealias ResponseUserBytes = ESPPacket

@OptIn(ExperimentalUnsignedTypes::class)
fun ResponseUserBytes.valentineOneUserBytes(
    version: Double,
): UserSettings = ByteArray(USER_BYTE_COUNT).apply {
    this@valentineOneUserBytes.copyInto(
        destination = this,
        destinationOffset = 0,
        length = this@apply.size,
    )
}.run {
    if (V1_GEN_2_PLATFORM_BASELINE_VERSION <= version) V19UserSettings(this)
    else V18UserSettings(this)
}

fun ByteArray.valentineOneUserBytes(
    version: Double,
): UserSettings = ByteArray(USER_BYTE_COUNT).apply {
    this@valentineOneUserBytes.copyInto(
        destination = this,
        destinationOffset = 0,
        startIndex = PAYLOAD_START_IDX,
        endIndex = PAYLOAD_START_IDX + this@apply.size
    )
}.run {
    if (V1_GEN_2_PLATFORM_BASELINE_VERSION <= version) V19UserSettings(this)
    else V18UserSettings(this)
}

@Suppress("unused")
@OptIn(ExperimentalUnsignedTypes::class)
fun ResponseUserBytes.techDisplayUserBytes(version: Double): UserSettings =
    ByteArray(USER_BYTE_COUNT).apply {
        this@techDisplayUserBytes.copyInto(
            destination = this,
            destinationOffset = 0,
            length = this@apply.size,
        )
    }.let(::TechDisplayUserSettings)

@Suppress("unused")
@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.techDisplayUserBytes(version: Double): UserSettings =
    ByteArray(USER_BYTE_COUNT).apply {
        this@techDisplayUserBytes.copyInto(
            destination = this,
            destinationOffset = 0,
            startIndex = PAYLOAD_START_IDX,
            endIndex = PAYLOAD_START_IDX + this@apply.size
        )
    }.let(::TechDisplayUserSettings)

/**
 * Represents user configuration settings inside of a Valentine One.
 *
 * Refer to Appendix 12.1 of the ESP Specification for a description of the [userBytes] based on the
 * firmware version of a Valentine One.s
 */
sealed interface UserSettings {
    /**
     * Valentine One user Configuration settings
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    val userBytes: ByteArray
}

val defaultUserBytes: ByteArray = ByteArray(USER_BYTE_COUNT)
    .apply { repeat(6) { this[it] = fullByte } }

internal const val USER_BYTE_COUNT: Int = 6