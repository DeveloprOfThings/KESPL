@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Aux0 Byte definition
 *
 * 07 06 05 04 03 02 01 00
 * |  |  |  |  |  |  |  |
 * |  |  |  |  |  |  |  \- Soft
 * |  |  |  |  |  |  \---- TS Holdoff
 * |  |  |  |  |  \------- Sys. Status
 * |  |  |  |  \---------- Display On
 * |  |  |  \------------- Euro Mode
 * |  |  \---------------- Custom Sweep
 * |  \------------------- ESP/Legacy
 * \---------------------- Display Active
 *
 * Reference: Table 8.3 of the ESP Specification v. 3.013
 *
 */
@JvmInline
value class Aux0(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the aux0 data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Indicates if the Valentine One's audio is muted.
     */
    val soft: Boolean get() = get(SOFT_BIT_INDEX)

    /**
     * Indicates if the Valentine One is 'time slicing' (accessories will be given a "time slice" to
     * communicate on the ESP bus).
     */
    val tsHoldOff: Boolean get() = get(TS_HOLDOFF_BIT_INDEX)

    /**
     * Indicates if the Valentine One has successfully signed on and is actively searching for alerts.
     */
    val systemStatus: Boolean get() = get(SYSTEM_STATUS_BIT_INDEX)

    /**
     * Indicates if the Valentine One's is turned on.
     */
    val displayOn: Boolean get() = get(DISPLAY_MODE_BIT_INDEX)

    /**
     * Indicates if the Valentine One is operating in Euro Mode.
     */
    val euroMode: Boolean get() = get(EURO_MODE_BIT_INDEX)

    /**
     * Indicates if the Valentine One's currently has custom sweeps defined. `false` if custom
     * sweeps have not been defined. `true` if Valentine One has custom sweeps defined and custom
     * modes will be used if operating in Euro Mode.
     */
    val customSweep: Boolean get() = get(CUSTOM_SWEEP_BIT_INDEX)

    /**
     * Indicates if the Valentine One is operating in Legacy mode.
     */
    val espLegacy: Boolean get() = get(ESP_LEGACY_BIT_INDEX)

    /**
     * Indicates if the Valentine One's display status. `false` if the Valentine One's display is
     * showing a mode or the resting display indicator. `true` if the display is actively showing an
     * alert, volume or other  important information.
     *
     * @since V4.1037
     */
    val displayActive: Boolean get() = get(DISPLAY_ACTIVE_BIT_INDEX)
}

internal const val SOFT_BIT_INDEX: Int = 0
internal const val TS_HOLDOFF_BIT_INDEX: Int = 1
internal const val SYSTEM_STATUS_BIT_INDEX: Int = 2
internal const val DISPLAY_MODE_BIT_INDEX: Int = 3
internal const val EURO_MODE_BIT_INDEX: Int = 4
internal const val CUSTOM_SWEEP_BIT_INDEX: Int = 5
internal const val ESP_LEGACY_BIT_INDEX: Int = 6
internal const val DISPLAY_ACTIVE_BIT_INDEX: Int = 7