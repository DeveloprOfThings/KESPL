@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.alert

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Auxiliary data for an alert.
 *
 * This class provides access to the individual bits within the aux0 byte of an alert data packet.
 * Each bit represents a specific flag or status related to the alert.
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  |  |
 * |  |  |  |  |  |  |  \- Photo Radar Type B0
 * |  |  |  |  |  |  \---- Photo Radar Type B1
 * |  |  |  |  |  \------- Photo Radar Type B2
 * |  |  |  |  \---------- Photo Radar Type B3
 * |  |  |  \------------- Reserved
 * |  |  \---------------- Reserved
 * |  \------------------- Junk Alert
 * \---------------------- Priority Alert
 *
 * Reference: ESP Specification v. 3.013
 *
 * @property data The raw byte value containing the aux0 data.
 */
@JvmInline
value class Aux0(private val data: Byte) {

    /**
     * Returns the bit at the specified index in the aux0 data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Photo Radar type bit 0.
     *
     * @since V4.1037
     */
    val photoRadarBit0: Boolean get() = get(PHOTO_RADAR_BIT_0_INDEX)

    /**
     * Photo Radar type bit 1.
     *
     * @since V4.1037
     */
    val photoRadarBit1: Boolean get() = get(PHOTO_RADAR_BIT_1_INDEX)

    /**
     * Photo Radar type bit 2.
     *
     * @since V4.1037
     */
    val photoRadarBit2: Boolean get() = get(PHOTO_RADAR_BIT_2_INDEX)

    /**
     * Photo Radar type bit 3.
     *
     * @since V4.1037
     */
    val photoRadarBit3: Boolean get() = get(PHOTO_RADAR_BIT_3_INDEX)

    /**
     * Reserved for future use.
     */
    val reserved5: Boolean get() = get(4)

    /**
     * Reserved for future use.
     */
    val reserved6: Boolean get() = get(5)

    /**
     * Indicates if the alert has been determined to be false alert and will be removed from
     * subsequent alert tables.
     *
     * @since V4.1032
     */
    val junkAlert: Boolean get() = get(JUNK_ALERT_BIT_INDEX)

    /**
     * Indicates if the alert has the highest priority in the alert table.
     */
    val priorityAlert: Boolean get() = get(PRIORITY_ALERT_BIT_INDEX)

    /**
     * Photo Radar type of the current alert
     *
     * @since V4.1037
     */
    val photoRadarType: PhotoRadar get() = PhotoRadar.fromByte(data)
}

private const val PHOTO_RADAR_BIT_0_INDEX: Int = 0
private const val PHOTO_RADAR_BIT_1_INDEX: Int = 1
private const val PHOTO_RADAR_BIT_2_INDEX: Int = 2
private const val PHOTO_RADAR_BIT_3_INDEX: Int = 3
private const val JUNK_ALERT_BIT_INDEX: Int = 6
private const val PRIORITY_ALERT_BIT_INDEX: Int = 7