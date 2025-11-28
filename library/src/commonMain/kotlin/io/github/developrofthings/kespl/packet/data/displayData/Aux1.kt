@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Aux1 Byte definition
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  |  |
 * |  |  |  |  |  |  |  \- Reserved
 * |  |  |  |  |  |  \---- Reserved
 * |  |  |  |  |  \------- Mode B0 Note 2
 * |  |  |  |  \---------- Mode B1 Note 2
 * |  |  |  \------------- Auto Muted Note 2
 * |  |  \---------------- Double Tap Active Note 2
 * |  \------------------- Bluetooth Indicator Image 1 Note 1
 * \---------------------- Bluetooth Indicator Image 2 Note 1
 *
 * Reference: Table 8.4 of the ESP Specification v. 3.012
 *
 * Note 1: This feature is only available on V4.1018 and higher
 * Note 2: This feature is only available on V4.1028 and higher
 */
@JvmInline
value class Aux1(internal val data: Byte) {

    /**
     * Returns the bit at the specified index in the aux1 data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Bit 0 of the V1 mode.
     *
     * @since V4.1028
     */
    val modeBit0: Boolean get() = get(2)
    /**
     * Bit 1 of the V1 mode.
     *
     * @since V4.1028
     */
    val modeBit2: Boolean get() = get(3)

    /**
     * Indicates the reason why the Valentine One's audio is muted. `True` - indicates that audio was
     * muted by the user or the app. `False` - indicates that audio was internally muted using the
     * Valentine One's logic.
     *
     * @since V4.1028
     */
    val autoMuted: Boolean get() = get(4)

    /**
     * Indicates whether or note the Valentine One double tap features is active.
     *
     * @since V4.1028
     */
    val doubleTapActive: Boolean get() = get(5)

    /**
     * State of the Valentine One's Bluetooth Indicator.
     *
     * @since V4.1018
     */
    val btIndicatorImage1: Boolean get() = get(6)
    /**
     * Alternate Valentine One's Bluetooth Indicator used to determine if the indicator is blinking.
     *
     * @since V4.1018
     */
    val btIndicatorImage2: Boolean get() = get(7)
}

/**
 * Current [V1Mode] the Valentine One is operating in
 *
 * @since V4.1028
 */
val Aux1.mode: V1Mode get() = V1Mode.from(aux1 = data)