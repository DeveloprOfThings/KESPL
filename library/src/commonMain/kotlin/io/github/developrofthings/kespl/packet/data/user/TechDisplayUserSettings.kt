@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.user

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

@JvmInline
/**
 * Represents user configuration settings inside of a Tech display.
 */
value class TechDisplayUserSettings(override val userBytes: ByteArray) : UserSettings {

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

        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

        /**
         * Indicates if the Valentine One should be turned off.
         */
        val isV1DisplayOff: Boolean get() = get(0)

        /**
         * Indicates if the tech should be turned on.
         */
        val isTechDisplayOn: Boolean get() = get(1)

        /**
         * Indicates if extended recall mode is off.
         */
        val isExtendedRecallModeTimeoutOff: Boolean get() = get(2)

        /**
         * Indicates if display "resting" is on.
         */
        val isRestingDisplayOn: Boolean get() = get(3)

        /**
         * Indicates if the extended frequency display is off.
         */
        val isExtendedFrequencyDisplayOff: Boolean get() = get(4)

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
    value class UserByte1(private val data: Byte) {
        operator fun get(index: Int): Boolean = data.isBitSet(index = index)

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
    value class UserByte2(private val data: Byte) {
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
    value class UserByte3(private val data: Byte) {
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