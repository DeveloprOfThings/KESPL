@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Signal Strength Bar Byte definition
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  | |
 * |  |  |  |  |  |  | \- b0(left)
 * |  |  |  |  |  |  \--- b1
 * |  |  |  |  |  \- ---- b2
 * |  |  |  |  \- -- ---- b3
 * |  |  |  \- -- -- ---- b4
 * |  |  \- -- -- -- ---- b5
 * |  \------------------ b6
 * \--------------------- b7(right)
 *
 * Reference: InfDisplayData packet description ESP Specification v. 3.012
 *
 * Note 1: This feature is only available on V4.1018 and higher
 */
@JvmInline
value class SignalStrengthBarGraph(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the signal strength bar graph data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)
    /**
     * Indicates if the Valentine One's bar graph 1 indicator is lit.
     */
    val b0: Boolean get() = get(0)
    /**
     * Indicates if the Valentine One's bar graph 2 indicator is lit.
     */
    val b1: Boolean get() = get(1)
    /**
     * Indicates if the Valentine One's bar graph 3 indicator is lit.
     */
    val b2: Boolean get() = get(2)
    /**
     * Indicates if the Valentine One's bar graph 4 indicator is lit.
     */
    val b3: Boolean get() = get(3)
    /**
     * Indicates if the Valentine One's bar graph 5 indicator is lit.
     */
    val b4: Boolean get() = get(4)
    /**
     * Indicates if the Valentine One's bar graph 6 indicator is lit.
     */
    val b5: Boolean get() = get(5)
    /**
     * Indicates if the Valentine One's bar graph 7 indicator is lit.
     */
    val b6: Boolean get() = get(6)
    /**
     * Indicates if the Valentine One's bar graph 8 indicator is lit.
     */
    val b7: Boolean get() = get(7)
}