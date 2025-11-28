@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused")

package io.github.developrofthings.kespl.packet.data.user

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import io.github.developrofthings.kespl.utilities.extensions.primitive.shr
import kotlin.experimental.and
import kotlin.jvm.JvmInline

@JvmInline
/***
 * Represents user configuration settings inside of a Valentine One Gen2.
 */
value class V19UserSettings(override val userBytes: ByteArray) : UserSettings {
    /**
     * Returns the byte at the specified index in the user settings.
     */
    operator fun get(index: Int): Byte = userBytes[index]

    val userByte0: UserByte0 get() = UserByte0(get(0))

    val userByte1: UserByte1 get() = UserByte1(get(1))

    val userByte2: UserByte2 get() = UserByte2(get(2))

    val userByte3: UserByte3 get() = UserByte3(get(3))

    val userByte4: UserByte4 get() = UserByte4(get(4))

    val userByte5: UserByte5 get() = UserByte5(get(5))
}

@JvmInline
value class UserByte0(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the user byte.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Indicates if X band coverage is on.
     */
    val isXBandOn: Boolean get() = get(0)

    /**
     * Indicates if K band coverage is on.
     */
    val isKBandOn: Boolean get() = get(1)

    /**
     * Indicates if Ka band coverage is on.
     */
    val isKaBandOn: Boolean get() = get(2)

    /**
     * Indicates if Laser coverage is on.
     */
    val isLaserOn: Boolean get() = get(3)

    /**
     * Indicates if mute to muted volume is on.
     */
    val isMuteToMutedVolume: Boolean get() = get(4)

    /**
     * Indicates if the bogey lock tone is loud after muting is on.
     */
    val isBogeyLockLoudAfterMutingOn: Boolean get() = get(5)

    /**
     * Indicates if X and K Band muting is in Logic or Advanced Logic mode is off.
     */
    val isXandKRearMuteInLogicModeOff: Boolean get() = get(6)

    /**
     * Indicates if Ku band coverage is on.
     */
    val isKuBandOn: Boolean get() = get(7)
}

@JvmInline
value class UserByte1(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the user byte.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Indicates if Euro mode is on.
     */
    val isEuroOn: Boolean get() = get(0)

    /**
     * Indicates if K-Verifier is on.
     */
    val isKVerifierOn: Boolean get() = get(1)

    /**
     * Indicates if rear laser is on.
     */
    val isRearLaserOn: Boolean get() = get(2)

    /**
     * Indicates if custom frequencies are disabled.
     */
    val isCustomFrequenciesDisabled: Boolean get() = get(3)

    /**
     * Indicates if Ka always radar priority is off.
     */
    val isKaAlwaysRadarPriorityOff: Boolean get() = get(4)

    /**
     * Indicates if fast laser detection is on.
     */
    val isFastLaserDetectionOn: Boolean get() = get(5)

    /**
     * Ka sensitivity level.
     */
    val kaSensitivity: BandSensitivity
        get() = BandSensitivity.kaSensitivityFromByte(
            kaSensitivityByte = ((data and KA_SENSITIVITY_BIT_MASK) shr KA_SENSITIVITY_SHIFT)
        )
}

@JvmInline
value class UserByte2(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the user byte.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Indicates if the Valentine One's startup/sign-on sequence is on.
     */
    val isStartupSequenceOn: Boolean get() = get(0)

    /**
     * Indicates if Resting display is enabled
     */
    val isRestingDisplayOn: Boolean get() = get(1)

    /**
     * Indicates if BSM (Blind Spot Monitoring) Plus is off.
     */
    val isBSMPlusOff: Boolean get() = get(2)

    val autoMute: AutoMute
        get() = AutoMute.fromByte(data)

    /**
     * K sensitivity level.
     */
    val kSensitivity: BandSensitivity
        get() = BandSensitivity.kAndXSensitivityFromByte(
            kAndXSensitivityByte = ((data and K_SENSITIVITY_BIT_MASK) shr K_SENSITIVITY_SHIFT)
        )

    val isMRCTPhotoRadarDisabled: Boolean get() = get(7)
}

@JvmInline
value class UserByte3(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the user byte.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)


    /**
     * X sensitivity level.
     */
    val xSensitivity: BandSensitivity
        get() = BandSensitivity.kAndXSensitivityFromByte(
            kAndXSensitivityByte = (data and X_SENSITIVITY_BIT_MASK)
        )

    /**
     * Indicates if DriveSafe 3D Photo Radar detection is off.
     */
    val isDriveSafe3DPhotoRadarOff: Boolean get() = get(2)

    /**
     * Indicates if DriveSafe 3DHD Photo Radar detection is off.
     */
    val isDriveSafe3DHDPhotoRadarOff: Boolean get() = get(3)

    /**
     * Indicates if Redflex Halo Photo Radar detection is off.
     */
    val isRedflexHaloPhotoRadarOff: Boolean get() = get(4)

    /**
     * Indicates if Redflex NK7 Photo Radar detection is off.
     */
    val isRedflexNK7PhotoRadarOff: Boolean get() = get(5)

    /**
     * Indicates if Ekin Photo Radar detection is off.
     */
    val isEkinPhotoRadarOff: Boolean get() = get(6)

    /**
     * Indicates if the Photo Radar verifier is off.
     */
    val isPhotoVerifierOff: Boolean get() = get(7)
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

enum class AutoMute(internal val value: Byte) {
    /**
     * Off (default): Auto Mute is disabled
     */
    Off(0x03),

    /**
     * Auto Mute: Mute all X, K & Ku signals after 3 seconds.
     */
    MuteAllSignals(0x02),

    /**
     * Auto Mute: with unmute – Mute all X, K & Ku signals after 3 seconds. Unmute
     * auto muted alerts if strength reaches five LEDs on the Signal Strength
     * Meter.
     */
    MuteAllSignalsWithUnmute(0x01),

    /**
     * Invalid value: forces setting to Off
     */
    Invalid(0x00);

    companion object {
        fun fromByte(value: Byte): AutoMute = when (value and AUTO_MUTE_MASK) {
            AUTO_MUTE_OFF -> Off
            AUTO_MUTE_ALL_SIGNALS -> MuteAllSignals
            AUTO_MUTE_ON_WITH_UNMUTE -> MuteAllSignalsWithUnmute
            AUTO_MUTE_INVALID -> Invalid
            else -> Invalid
        }
    }
}


enum class BandSensitivity {
    /**
     * Maximum range(default)
     */
    Full,

    /**
     * Same as V1 Gen2 at its 2020 introduction
     */
    Original,

    /**
     * Still great range but fewer alerts from fantastic distances
     */
    Relaxed,

    /**
     * Forces setting to Full Sensitivity
     */
    Invalid;

    companion object {
        fun kaSensitivityFromByte(kaSensitivityByte: Byte): BandSensitivity =
            when (kaSensitivityByte) {
                kaSensitivityFull -> Full
                kaSensitivityOriginal -> Original
                kaSensitivityRelaxed -> Relaxed
                kaSensitivityInvalid -> Invalid
                else -> Invalid
            }

        fun kAndXSensitivityFromByte(kAndXSensitivityByte: Byte): BandSensitivity =
            when (kAndXSensitivityByte) {
                kAndXSensitivityFull -> Full
                kAndXSensitivityOriginal -> Original
                kAndXSensitivityRelaxed -> Relaxed
                kAndXSensitivityInvalid -> Invalid
                else -> Invalid
            }
    }
}

private const val KA_SENSITIVITY_SHIFT: Int = 6
private const val KA_SENSITIVITY_BIT_MASK: Byte = (0xC0).toByte()
private const val kaSensitivityFull: Byte = 3
private const val kaSensitivityOriginal: Byte = 2
private const val kaSensitivityRelaxed: Byte = 1
private const val kaSensitivityInvalid: Byte = 0
private const val K_SENSITIVITY_SHIFT: Int = 5
private const val K_SENSITIVITY_BIT_MASK: Byte = (0x60).toByte()
private const val X_SENSITIVITY_BIT_MASK: Byte = (0x03)
private const val kAndXSensitivityOriginal: Byte = 3
private const val kAndXSensitivityFull: Byte = 2
private const val kAndXSensitivityRelaxed: Byte = 1
private const val kAndXSensitivityInvalid: Byte = 0
private const val AUTO_MUTE_MASK: Byte = 0x18
private const val AUTO_MUTE_OFF: Byte = 0x03
private const val AUTO_MUTE_ALL_SIGNALS: Byte = 0x02
private const val AUTO_MUTE_ON_WITH_UNMUTE: Byte = 0x01
private const val AUTO_MUTE_INVALID: Byte = 0x00