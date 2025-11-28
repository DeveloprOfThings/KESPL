package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.emptyByte
import io.github.developrofthings.kespl.fullByte
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.math.roundToInt

typealias ResponseSavvyStatus = ESPPacket

fun ByteArray.status(): SAVVYStatus = SAVVYStatus(
    currentSpeedThresholdKPH = this[PAYLOAD_START_IDX + 0].toInt(),
    status = this[PAYLOAD_START_IDX + 1],
)

fun ResponseSavvyStatus.status(): SAVVYStatus = this.bytes.status()


/**
 * Represents the current status of a SAVVY.
 *
 * This class encapsulates the current speed threshold and status information related to SAVVY.
 *
 * @property currentSpeedThresholdKPH The current speed threshold in kilometers per hour (KPH).
 * @property status A byte representing the status of SAVVY, where individual bits correspond to
 * different flags.
 *
 * @property isThresholdUserOverride Indicates whether the speed threshold has been overridden by
 * the user.
 * This is determined by checking the 0th bit of the [status] byte.
 * @property currentSpeedThresholdMPH The current speed threshold in miles per hour (MPH),
 * calculated from [currentSpeedThresholdKPH].
 * @property isUnmuteEnabled Indicates whether unmuting is enabled.
 * This is determined by checking the 1st bit of the [status] byte.
 */
data class SAVVYStatus(
    val currentSpeedThresholdKPH: Int,
    private val status: Byte,
) {
    val isThresholdUserOverride: Boolean get() = status.isBitSet(0)

    val isUnmuteEnabled: Boolean get() = status.isBitSet(1)

    val currentSpeedThresholdMPH: Int
        get() = (currentSpeedThresholdKPH * KPH_TO_MPH_CONVERSION_SCALER).roundToInt()
}

/**
 * Represents the different ways to override the SAVVY's thumbwheel setting.
 *
 * This sealed class defines the possible overrides for the SAVVY's thumbwheel, which controls
 * the speed at which the Valentine One is muted.
 *
 * @property speed The internal speed value associated with the override.
 */
sealed class SAVVYThumbwheelOverride(internal val speed: Byte) {
    /**
     * The SAVVY should not mute the Valentine One.
     */
    data object None : SAVVYThumbwheelOverride(speed = emptyByte)

    /**
     * The SAVVY should mute the Valentine One at all speeds.
     */
    data object Auto : SAVVYThumbwheelOverride(speed = fullByte)

    /**
     * The SAVVY should mute the Valentine One at the specified speed (in KPH) overriding the
     * current thumbwheel value.
     *
     * This value is not persisted across reboots and will be overwritten if the SAVVY's thumbwheel
     * is changed.
     *
     * @property speedKPH The speed to mute at, in KPH.
     */
    class Custom(speedKPH: Byte) : SAVVYThumbwheelOverride(speed = speedKPH)
}

private const val KPH_TO_MPH_CONVERSION_SCALER: Float = 1 / 1.60934F

private const val MPH_TO_KPH_CONVERSION_SCALER: Float = 1.60934F

fun Int.toKPH(): Int = (this * MPH_TO_KPH_CONVERSION_SCALER).toInt()

fun Int.toMPH(): Int = (this * KPH_TO_MPH_CONVERSION_SCALER).toInt()

internal fun Int.toKPHByte(): Byte = (this * MPH_TO_KPH_CONVERSION_SCALER).toInt().toByte()

internal fun Int.toMPHByte(): Byte = (this * KPH_TO_MPH_CONVERSION_SCALER).toInt().toByte()