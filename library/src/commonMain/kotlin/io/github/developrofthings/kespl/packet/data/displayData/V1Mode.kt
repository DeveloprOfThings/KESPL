package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.ESPPacketId.InfDisplayData
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_A
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_C
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_L
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_U
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_c
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_l
import io.github.developrofthings.kespl.SEVEN_SEG_VALUE_u
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode.AdvancedLogic
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode.AllBogeysKKa
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode.Invalid
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode.LogicKa
import io.github.developrofthings.kespl.utilities.extensions.primitive.shr
import kotlin.experimental.and

/**
 * Represents the operating mode of the Valentine One. The mode determines how the Valentine One
 * will alert to radar and laser signals.
 *
 * The Valentine One has three primary operating modes:
 * - **All Bogeys/K/Ka**: In this mode, all detected radar and laser signals (bogeys) are reported
 *   at their initial (unmuted) volume as soon as they are detected. This mode provides the most
 *   comprehensive and immediate alerting.
 * - **Logic/Ka**: In this mode, X and K-band bogeys that are assessed to be too weak to be threats
 *   are initially reported at a muted volume. If the Valentine One later determines these signals
 *   to be threats, the audio will increase to the initial (unmuted) volume. Ka-band signals are
 *   always reported at their initial volume. This mode helps to reduce false alerts from weaker
 *   X and K-band signals while still providing timely alerts for Ka-band threats.
 * - **Advanced Logic**: In this mode, X and K-band bogeys that the Valentine One determines not to
 *   be threats are not reported at all. Threats are reported at their initial (unmuted) volume.
 *   As a failsafe, the Valentine One will always alert to extremely strong signals, regardless of
 *   the mode. This mode provides the quietest operation by filtering out most non-threatening
 *   signals.
 *
 * @property byteValue The byte value representing the mode.
 *
 * @see AllBogeysKKa
 * @see LogicKa
 * @see AdvancedLogic
 * @see Invalid
 * @see InfDisplayData
 * @see DisplayData.aux1
 */
sealed class V1Mode(val byteValue: Byte) {

    /**
     * All bogeys will be reported at the Initial (unmuted) Volume as soon as they are detected.
     */
    data object AllBogeysKKa : V1Mode(ONE)

    /**
     * X and K-band bogeys assessed to be too weak to be threads will be reported at the Muted
     * volume. If and when the Valentine One determines them to be threats, the audio will increase
     * to the Initial (unmuted) Volume.
     */
    data object LogicKa : V1Mode(TWO)

    /**
     * X and K-band bogeys the Valentine One has determines to **not** be a threat will not be
     * reported at all. Threats will be reported at Initial (unmuted) Volume.
     *
     * **Note:** As a fail safe the Valentine One will always alert for extremely strong alerts.
     */
    data object AdvancedLogic : V1Mode(THREE)

    /**
     * A valid mode could not be determined using the
     * [InfDisplayData].
     * More than likely this will happen when the Valentine One is either alerting or displaying a
     * non-"mode" symbol on the bogey counter and the Valentine One doesn't support mode bits in
     * [DisplayData.aux1].
     *
     * @see InfDisplayData
     * @see DisplayData.aux1
     */
    data object Invalid : V1Mode(0x00)

    companion object {
        fun from(aux1: Byte): V1Mode {
            val modeBits = (aux1 and MODE_MASK) shr 2
            return when (modeBits) {
                ONE -> AllBogeysKKa
                TWO -> LogicKa
                THREE -> AdvancedLogic
                else -> Invalid
            }
        }

        fun fromBogeyCounter(bogeyCounter: Byte): V1Mode =
            when (bogeyCounter and BG_COUNTER_MINUS_DP_MASK) {
                SEVEN_SEG_VALUE_A,
                SEVEN_SEG_VALUE_C,
                SEVEN_SEG_VALUE_U,
                    -> AllBogeysKKa


                SEVEN_SEG_VALUE_l,
                SEVEN_SEG_VALUE_u,
                SEVEN_SEG_VALUE_c,
                    -> LogicKa

                SEVEN_SEG_VALUE_L -> AdvancedLogic

                else -> Invalid
            }
    }
}

val modes: List<V1Mode> = listOf(
    AllBogeysKKa,
    LogicKa,
    AdvancedLogic,
    Invalid,
)

internal const val BG_COUNTER_MINUS_DP_MASK: Byte = 0x7F
private const val MODE_MASK: Byte = 0b0000_1100
internal const val ONE: Byte = 0x01.toByte()
internal const val TWO: Byte = 0x02.toByte()
internal const val THREE: Byte = 0x03.toByte()