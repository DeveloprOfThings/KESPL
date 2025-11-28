@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.alert

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * This class represents the band arrow data for a given alert within an alert table.
 *
 * It provides a convenient way to access the individual bits of the band arrow data,
 * which indicate the signal band (Laser, Ka, K, X, Ku) and the direction of detection
 * (front, side, rear) relative to the Valentine One device.
 *
 * @property data The raw byte data for the band arrow.
 */
@JvmInline
value class BandArrow(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the band arrow data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Indicates if the alert this [BandArrow] corresponds to a laser signal.
     */
    val laser: Boolean get() = get(0)

    /**
     * Indicates if the alert this [BandArrow] corresponds to a Ka band signal.
     */
    val kaBand: Boolean get() = get(1)

    /**
     * Indicates if the alert this [BandArrow] corresponds to a K band signal.
     */
    val kBand: Boolean get() = get(2)

    /**
     * Indicates if the alert this [BandArrow] corresponds to a X band signal.
     */
    val xBand: Boolean get() = get(3)

    /**
     * Indicates if the alert this [BandArrow] corresponds to a Ka band signal.
     */
    val kuBand: Boolean get() = get(4)

    /**
     * Indicates if the alert this [BandArrow] corresponds to is detected at the front of the
     * Valentine One.
     */
    val front: Boolean get() = get(5)

    /**
     * Indicates if the alert this [BandArrow] corresponds to is detected at the side of the
     * Valentine One.
     */
    val side: Boolean get() = get(6)

    /**
     * Indicates if the alert this [BandArrow] corresponds to is detected at the rear of the
     * Valentine One.
     */
    val rear: Boolean get() = get(7)

    /**
     * The alert band in which this alert was detected
     */
    val band: AlertBand get() = AlertBand.fromByte(data)

    /**
     * The direction, in respect to the Valentine One's rear antenna, this alert was detected.
     */
    val arrow: AlertArrow get() = AlertArrow.fromByte(data)
}

private const val LASER: Byte = 0x01
private const val KA_BAND: Byte = 0x02
private const val K_BAND: Byte = 0x04
private const val X_BAND: Byte = 0x16
private const val KU_BAND: Byte = 0x16

private const val FRONT: Byte = 0x20
private const val SIDE: Byte = 0x40
private const val REAR: Byte = 0x80.toByte()
