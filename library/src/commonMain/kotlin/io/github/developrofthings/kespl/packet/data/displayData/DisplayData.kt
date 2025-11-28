package io.github.developrofthings.kespl.packet.data.displayData

import kotlin.jvm.JvmInline

/**
 * Display information needed to rebuild the front panel display of a Valentine One.
 */
@JvmInline
value class DisplayData(internal val bytes: ByteArray) {

    /**
     * Returns the byte at the specified index in the display data.
     */
    operator fun get(index: Int): Byte = bytes[index]

    /**
     * Bogey Counter 7 Segment "Image 2" Byte definition
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
     * Reference: InfDisplayData packet description ESP Specification v. 3.013
     */
    val bogeyCounter7SegmentImage1: BogeyCounter7Segment
        get() = BogeyCounter7Segment(bytes[BOGEY_COUNTER_IMG_1_INDEX])

    /**
     * Bogey Counter 7 Segment "Image 2" Byte definition
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
     * Reference: InfDisplayData packet description ESP Specification v. 3.013
     */
    val bogeyCounter7SegmentImage2: BogeyCounter7Segment
        get() = BogeyCounter7Segment(bytes[BOGEY_COUNTER_IMG_2_INDEX])

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
     * Reference: InfDisplayData packet description ESP Specification v. 3.013
     */
    val signalStrengthBarGraphImage: SignalStrengthBarGraph
        get() = SignalStrengthBarGraph(bytes[SIGNAL_STRENGTH_BARGRAPH_INDEX])

    /**
     * Band and Arrow Indicator "Image 1" Byte definition
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
     * Reference: InfDisplayData packet description ESP Specification v. 3.013
     */
    val bandArrowIndicatorImage1: BandArrowIndicator
        get() = BandArrowIndicator(bytes[BAND_ARROW_IMG_1_INDEX])

    /**
     * Band and Arrow Indicator "Image 2" Byte definition
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
     * Reference: InfDisplayData packet description ESP Specification v. 3.013
     */
    val bandArrowIndicatorImage2: BandArrowIndicator
        get() = BandArrowIndicator(bytes[BAND_ARROW_IMG_2_INDEX])

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
     */
    val aux0: Aux0
        get() = Aux0(bytes[AUX_0_INDEX])

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
     * Reference: Table 8.4 of the ESP Specification v. 3.013
     */
    val aux1: Aux1
        get() = Aux1(bytes[AUX_1_INDEX])

    /**
     * Aux2 – Current Volume
     * 07:04 03:00
     * |       |
     * |       \- Mute Volume B3, B2, B1, B0
     * \--------- Main Volume B3, B2, B1, B0
     */
    val aux2: Aux2
        get() = Aux2(bytes[AUX_2_INDEX])


    /**
     * Indicates if the Valentine One's audio is muted.
     */
    val isSoft: Boolean get() = aux0.soft

    /**
     * Indicates if the Valentine One is "time slicing" (accessories will be given a "time slice" to communicate on the ESP bus).
     */
    val isTimeSlicing: Boolean get() = !aux0.tsHoldOff

    /**
     * Indicates if the Valentine One has successfully signed on and is actively searching for alerts.
     */
    val isSearchingForAlerts: Boolean get() = aux0.systemStatus

    /**
     * Indicates if the Valentine One's is turned on.
     */
    val isDisplayOn: Boolean get() = aux0.displayOn

    /**
     * Indicates if the Valentine One is operating in Euro Mode.
     */
    val isEuro: Boolean get() = aux0.euroMode

    /**
     * Indicates if the Valentine One's currently has custom sweeps defined. `false` if custom
     * sweeps have not been defined. `true` if Valentine One has custom sweeps defined and custom
     * modes will be used if operating in Euro Mode.
     */
    val isCustomSweep: Boolean get() = aux0.customSweep

    /**
     * Indicates if the Valentine One is operating in Legacy mode.
     */
    val isLegacy: Boolean get() = aux0.espLegacy

    /**
     * Indicates if the Valentine One's display status. `false` if the Valentine One's display is
     * showing a mode or the resting display indicator. `true` if the display is actively showing an
     * alert, volume or other  important information.
     *
     * @since V4.1037
     */
    val isDisplayActive: Boolean get() = aux0.displayActive

    /**
     * Current [V1Mode] the Valentine One is operating in
     *
     * @since V4.1028
     */
    val mode: V1Mode get() = aux1.mode

    /**
     * State of the Valentine One's Bluetooth Indicator.
     *
     * @since V4.1018
     */
    val btIndicatorImage1: Boolean get() = aux1.btIndicatorImage1

    /**
     * Indicates if the mute/soft indicator is lit on the Valentine One's display.
     *
     * @since V4.1018
     */
    val muteIndicatorImage1: Boolean get() = bandArrowIndicatorImage1.mute

    /**
     * Indicates if the mute/soft indicator is lit on the Valentine One's display.
     *
     * @since V4.1018
     */
    val muteIndicatorImage2: Boolean get() = bandArrowIndicatorImage2.mute

    /**
     * Alternate Valentine One's Bluetooth Indicator used to determine if the indicator is blinking.
     *
     * @since V4.1018
     */
    val btIndicatorImage2: Boolean get() = aux1.btIndicatorImage2

    @Suppress("unused")
    val bogeyCounterMode: V1Mode get() = bogeyCounter7SegmentImage1.mode
}

const val BOGEY_COUNTER_IMG_1_INDEX: Int = 0
const val BOGEY_COUNTER_IMG_2_INDEX: Int = 1
const val SIGNAL_STRENGTH_BARGRAPH_INDEX: Int = 2
const val BAND_ARROW_IMG_1_INDEX: Int = 3
const val BAND_ARROW_IMG_2_INDEX: Int = 4
const val AUX_0_INDEX: Int = 5
const val AUX_1_INDEX: Int = 6
const val AUX_2_INDEX: Int = 7