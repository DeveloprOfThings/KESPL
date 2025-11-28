package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Aux2 – Current Volume
 * 07:04 03:00
 * |       |
 * |       \- Mute Volume B3, B2, B1, B0
 * \--------- Main Volume B3, B2, B1, B0
 *
 * Reference: InfDisplayData packet description ESP Specification v. 3.012
 * @since V4.1028
 */
@JvmInline
value class Aux2(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the aux2 data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Muted volume level of the Valentine One.
     */
    val muteVolume: Int get() = (data.toInt() and MUTE_VOL_MASK)
    /**
     * Main (unmuted) volume level of the Valentine One.
     */
    val mainVolume: Int get() = ((data.toInt() and MAIN_VOL_MASK) shr 4)
}

internal const val MUTE_VOL_MASK: Int = 0x0F

internal const val MAIN_VOL_MASK: Int = 0xF0