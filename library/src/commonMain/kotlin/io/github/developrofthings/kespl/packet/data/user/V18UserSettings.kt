@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.user

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.experimental.and
import kotlin.jvm.JvmInline

@JvmInline
/**
 * Represents user configuration settings inside of a Valentine One.
 */
value class V18UserSettings(override val userBytes: ByteArray) : UserSettings {
    /**
     * Returns the byte at the specified index in the user settings.
     */
    operator fun get(index: Int): Byte = userBytes[index]

    /**
     * Returns the [UserByte0] of the user settings.
     */
    val userByte0: UserByte0 get() = UserByte0(get(0))

    /**
     * Returns the [UserByte1] of the user settings.
     */
    val userByte1: UserByte1 get() = UserByte1(get(1))

    /**
     * Returns the [UserByte2] of the user settings.
     */
    val userByte2: UserByte2 get() = UserByte2(get(2))

    /**
     * Returns the [UserByte3] of the user settings.
     */
    val userByte3: UserByte3 get() = UserByte3(get(3))

    /**
     * Returns the [UserByte4] of the user settings.
     */
    val userByte4: UserByte4 get() = UserByte4(get(4))

    /**
     * Returns the [UserByte5] of the user settings.
     */
    val userByte5: UserByte5 get() = UserByte5(get(5))

    @JvmInline
    value class UserByte0(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

        /**
         * Indicates if X band coverage is enabled.
         */
        val xBand: Boolean get() = get(0)

        /**
         * Indicates if K band coverage is enabled.
         */
        val kBand: Boolean get() = get(1)

        /**
         * Indicates if Ka band coverage is enabled.
         */
        val kaBand: Boolean get() = get(2)

        /**
         * Indicates if Laser coverage is enabled.
         */
        val laser: Boolean get() = get(3)

        /**
         * Indicates if the bar graph (signal strength meter) is "more responsive" for Ka band
         * alerts only.
         */
        val barGraphBoolean: Boolean get() = get(4)

        /**
         * Indicates if the Ka false guard is enabled.
         */
        val kaFalseGuard: Boolean get() = get(5)

        /**
         * Indicates if initial K Band alerts are muted under certain circumstances.
         */
        val kMuting: Boolean get() = get(6)

        /**
         * Indicates if the muted volume is controlled by the control lever.
         */
        val muteVolumeBoolean: Boolean get() = get(7)

        /**
         * Sensitivity of the Valentine One's bar graph.
         */
        val barGraph: BarGraphSensitive
            get() = when (barGraphBoolean) {
                true -> BarGraphSensitive.Normal
                false -> BarGraphSensitive.Responsive
            }

        /**
         * Control for muted audio volume.
         */
        val muteVolume: MuteVolumeControl
            get() = when (muteVolumeBoolean) {
                true -> MuteVolumeControl.Lever
                false -> MuteVolumeControl.Zero
            }
    }

    @JvmInline
    value class UserByte1(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

        /**
         * Indicates if the volume of the bogey lock tone after your press mute is controlled by the
         * control knob.
         */
        val postMuteBogeyLockVolume: Boolean get() = get(0)

        /**
         * K timer bits
         */
        private val kTimerByte: Byte get() = (data and kTimerBitMask)

        /**
         * The time period of automatic muting at the onset of K Band alerts, in seconds.
         */
        val kTimer: KTimer
            get() = when (kTimerByte) {
                kTimerMute10 -> KTimer.`10`
                kTimerMute30 -> KTimer.`30`
                kTimerMute20 -> KTimer.`20`
                kTimerMute15 -> KTimer.`15`
                kTimerMute07 -> KTimer.`7`
                kTimerMute05 -> KTimer.`5`
                kTimerMute04 -> KTimer.`4`
                else -> KTimer.`3`
            }

        /**
         * Indicates if muted K Band alerts are unmuted at four lights.
         */
        val kInitialUnmute4Lights: Boolean get() = get(4)

        /**
         * Indicates if muted K Band alerts slowly rise to six lights are unmuted.
         */
        val kPersistentUnmute6Lights: Boolean get() = get(5)

        /**
         * Indicates if rear K Band alerts are unmuted.
         */
        val kRearUnmuted: Boolean get() = get(6)

        /**
         * Indicates if Ku band coverage is enabled.
         */
        val kuBand: Boolean get() = get(7)
    }

    @JvmInline
    value class UserByte2(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

        /**
         * Indicates if POP is enabled.
         */
        val pop: Boolean get() = get(0)

        /**
         * Indicates if Euro mode is enabled.
         */
        val euroDisabled: Boolean get() = get(1)

        /**
         * Indicates if Euro X coverage is enabled.  
         */
        val euroX: Boolean get() = get(2)

        /**
         * Indicates if Traffic Monitor Filter (TMF) and Junk-K fighter is disabled.
         */
        val tmfJunkFilterDisabled: Boolean get() = get(3)

        /**
         * Indicates if the forcing Legacy display is disabled.
         */
        val fForceLegacyDisplayDisabled: Boolean get() = get(4)

        /**
         * Unused; always 1.
         */
        val bit5: Boolean get() = get(5)

        /**
         * Unused; always 1.
         */
        val bit6: Boolean get() = get(6)

        /**
         * Unused; always 1.
         */
        val bit7: Boolean get() = get(7)
    }

    @JvmInline
    value class UserByte3(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

        /**
         * Unused; always 1.
         */
        val bit0: Boolean get() = get(0)

        /**
         * Unused; always 1.
         */
        val bit1: Boolean get() = get(1)

        /**
         * Unused; always 1.
         */
        val bit2: Boolean get() = get(2)

        /**
         * Unused; always 1.
         */
        val bit3: Boolean get() = get(3)

        /**
         * Unused; always 1.
         */
        val bit4: Boolean get() = get(4)

        /**
         * Unused; always 1.
         */
        val bit5: Boolean get() = get(5)

        /**
         * Unused; always 1.
         */
        val bit6: Boolean get() = get(6)

        /**
         * Unused; always 1.
         */
        val bit7: Boolean get() = get(7)
    }

    @JvmInline
    value class UserByte4(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)
        /**
         * Unused; always 1.
         */
        val bit0: Boolean get() = get(0)
        /**
         * Unused; always 1.
         */
        val bit1: Boolean get() = get(1)
        /**
         * Unused; always 1.
         */
        val bit2: Boolean get() = get(2)
        /**
         * Unused; always 1.
         */
        val bit3: Boolean get() = get(3)
        /**
         * Unused; always 1.
         */
        val bit4: Boolean get() = get(4)
        /**
         * Unused; always 1.
         */
        val bit5: Boolean get() = get(5)
        /**
         * Unused; always 1.
         */
        val bit6: Boolean get() = get(6)
        /**
         * Unused; always 1.
         */
        val bit7: Boolean get() = get(7)
    }

    @JvmInline
    value class UserByte5(private val data: Byte) {
        /**
         * Returns the bit at the specified index in the user byte.
         */
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)
        /**
         * Unused; always 1.
         */
        val bit0: Boolean get() = get(0)
        /**
         * Unused; always 1.
         */
        val bit1: Boolean get() = get(1)
        /**
         * Unused; always 1.
         */
        val bit2: Boolean get() = get(2)
        /**
         * Unused; always 1.
         */
        val bit3: Boolean get() = get(3)
        /**
         * Unused; always 1.
         */
        val bit4: Boolean get() = get(4)
        /**
         * Unused; always 1.
         */
        val bit5: Boolean get() = get(5)
        /**
         * Unused; always 1.
         */
        val bit6: Boolean get() = get(6)
        /**
         * Unused; always 1.
         */
        val bit7: Boolean get() = get(7)
    }
}

enum class BarGraphSensitive(val value: Boolean) {
    Normal(true),
    Responsive(false)
}

enum class MuteVolumeControl(val value: Boolean) {
    /**
     * Muted volume is controlled by the control lever.
     */
    Lever(true),

    /**
     * Muted volume is always zero.
     */
    Zero(false)
}

enum class BogeyLockVolumeControl(val value: Boolean) {
    Lever(true),
    Knob(false)
}

enum class KTimer {
    `10`,
    `30`,
    `20`,
    `15`,
    `7`,
    `5`,
    `4`,
    `3`,
}

private const val kTimerMute10: Byte = 0x0E
private const val kTimerMute30: Byte = 0x0C
private const val kTimerMute20: Byte = 0x0A
private const val kTimerMute15: Byte = 8
private const val kTimerMute07: Byte = 6
private const val kTimerMute05: Byte = 4
private const val kTimerMute04: Byte = 2
private const val kTimerMute03: Byte = 0
private const val kTimerBitMask: Byte = 0x0E
