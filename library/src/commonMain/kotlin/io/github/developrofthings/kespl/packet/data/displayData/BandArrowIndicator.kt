@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.get
import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import io.github.developrofthings.kespl.utilities.extensions.primitive.last
import kotlin.jvm.JvmInline

/**
 * Band and Arrow Indicator Byte definition
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  |  |
 * |  |  |  |  |  |  |  \- LASER
 * |  |  |  |  |  |  \---- Ka BAND
 * |  |  |  |  |  \------- K BAND
 * |  |  |  |  \---------- X BAND
 * |  |  |  \------------- Mute Indicator Note 1
 * |  |  \---------------- FRONT
 * |  \------------------- SIDE
 * \---------------------- REAR
 *
 * Reference: InfDisplayData packet description ESP Specification v. 3.012
 *
 * Note 1: This feature is only available on V4.1018 and higher
 */
@JvmInline
value class BandArrowIndicator(val data: Byte) {

    /**
     * Returns the bit at the specified index in the band arrow indicator data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)
    /**
     * Indicates if the Laser alert indicator is lit on the Valentine One's display.
     */
    val laser: Boolean get() = get(0)
    /**
     * Indicates if the Ka alert indicator is lit on the Valentine One's display.
     */
    val kaBand: Boolean get() = get(1)
    /**
     * Indicates if the K alert indicator is lit on the Valentine One's display.
     */
    val kBand: Boolean get() = get(2)
    /**
     * Indicates if the X alert indicator is lit on the Valentine One's display.
     */
    val xBand: Boolean get() = get(3)
    /**
     * Indicates if the mute/soft indicator is lit on the Valentine One's display.
     *
     * @since V4.1018
     */
    val mute: Boolean get() = get(4)
    /**
     * Indicates if the Front arrow indicator is lit on the Valentine One's display.
     */
    val front: Boolean get() = get(5)
    /**
     * Indicates if the Side arrow indicator is lit on the Valentine One's display.
     */
    val side: Boolean get() = get(6)
    /**
     * Indicates if the Rear arrow indicator is lit on the Valentine One's display.
     */
    val rear: Boolean get() = get(7)
}

val Byte.front: Boolean get() = this[5]

val Byte.side: Boolean get() = this[6]

val Byte.rear: Boolean get() = last