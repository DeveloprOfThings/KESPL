@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import io.github.developrofthings.kespl.utilities.extensions.primitive.last
import kotlin.experimental.and
import kotlin.jvm.JvmInline

/**
 * Bogey Counter 7 Segment Byte definition
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  |  |
 * |  |  |  |  |  |  |  \- Seg a
 * |  |  |  |  |  |  \---- Seg b
 * |  |  |  |  |  \------- Seg c
 * |  |  |  |  \---------- Seg d
 * |  |  |  \------------- Seg e
 * |  |  \---------------- Seg f
 * |  \------------------- Seg g
 * \---------------------- dp
 *
 * Reference: InfDisplayData packet description ESP Specification v. 3.012
 *
 * Note 1: This feature is only available on V4.1018 and higher
 */
@JvmInline
value class BogeyCounter7Segment(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the bogey counter data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)
    /**
     * Indicates if the Valentine One's seven segment 'a' indicator is lit.
     */
    val segmentA: Boolean get() = get(0)
    /**
     * Indicates if the Valentine One's seven segment 'b' indicator is lit.
     */
    val segmentB: Boolean get() = get(1)
    /**
     * Indicates if the Valentine One's seven segment 'c' indicator is lit.
     */
    val segmentC: Boolean get() = get(2)
    /**
     * Indicates if the Valentine One's seven segment 'd' indicator is lit.
     */
    val segmentD: Boolean get() = get(3)
    /**
     * Indicates if the Valentine One's seven segment 'e' indicator is lit.
     */
    val segmentE: Boolean get() = get(4)
    /**
     * Indicates if the Valentine One's seven segment 'f' indicator is lit.
     */
    val segmentF: Boolean get() = get(5)
    /**
     * Indicates if the Valentine One's seven segment 'g' indicator is lit.
     */
    val segmentG: Boolean get() = get(6)
    /**
     * Indicates if the Valentine One's seven segment 'decimal point' indicator is lit.
     */
    val decimalPoint: Boolean get() = get(7)

    /**
     * Subtracts the decimal point display bit, and return the remaining bits that can be used to
     * reconstruct a [V1Mode].
     */
    internal val modeBits: Byte get() = data and BG_COUNTER_MINUS_DP_MASK

    val raw: Byte get() = data
}

val BogeyCounter7Segment.mode: V1Mode get() = V1Mode.fromBogeyCounter(bogeyCounter = this.modeBits)

val Byte.decimalPoint: Boolean get() = last