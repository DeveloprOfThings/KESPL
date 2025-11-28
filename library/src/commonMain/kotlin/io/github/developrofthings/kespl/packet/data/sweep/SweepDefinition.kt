package io.github.developrofthings.kespl.packet.data.sweep

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket

typealias ResponseSweepDefinition = ESPPacket
typealias ResponseMaxSweepIndex = ESPPacket
typealias ResponseSweepWriteResult = ESPPacket
// Type alias for users of the library that prefer this terminology.
typealias CustomFrequency = SweepDefinition

fun ResponseSweepDefinition.sweepDefinition(): SweepDefinition = this.bytes.sweepDefinition()

fun ByteArray.sweepDefinition(): SweepDefinition {
    val upperEdgeMSB = this[PAYLOAD_START_IDX + 1].toInt() and 0xFF
    val upperEdgeLSB = this[PAYLOAD_START_IDX + 2].toInt() and 0xFF
    val lowerEdgeMSB = this[PAYLOAD_START_IDX + 3].toInt() and 0xFF
    val lowerEdgeLSB = this[PAYLOAD_START_IDX + 4].toInt() and 0xFF
    return SweepDefinition(
        index = this[PAYLOAD_START_IDX + 0].toInt() and INDEX_MASK,
        lowerEdge = ((lowerEdgeMSB shl 8) or lowerEdgeLSB),
        upperEdge = ((upperEdgeMSB shl 8) or upperEdgeLSB),
    )
}

/**
 * Maximum number of [SweepDefinition] the current Valentine One supports.
 */
val ResponseMaxSweepIndex.maxSweepIndex: Int get() = this.bytes.maxSweepIndex


val ByteArray.maxSweepIndex: Int get() = this[PAYLOAD_START_IDX].toInt()

/**
 * Result of the [SweepDefinition] write operation.
 *
 * 0 = Sweep Write Successful
 * Any Other Value = The number of the first sweep with invalid parameters.
 * The error number returned will be the sweep index + 1, where sweep index is the index from the
 * reqWriteSweepDefinition packet
 *
 */
@Suppress("unused")
val ResponseSweepWriteResult.writeResult: Int get() = this.bytes.writeResult

val ByteArray.writeResult: Int get() = this[PAYLOAD_START_IDX].toInt()

/**
 * A user configurable range of frequencies that a Valentine One will report detected alerts.
 *
 * These frequency ranges **must** responds the bounds of the [SweepSection].
 *
 * The **maximum** number of sweeps is defined by [ResponseMaxSweepIndex.maxSweepIndex].
 *
 * @property index The index for this sweep definition. Max value determined by
 * [ResponseMaxSweepIndex.maxSweepIndex].
 * @property lowerEdge The lower frequency edge for this sweep in MHz.
 * @property upperEdge The upper frequency edge for this sweep in MHz.
 *
 * @see ResponseMaxSweepIndex.maxSweepIndex
 * @see ESPPacketId.ReqMaxSweepIndex
 * @see ESPPacketId.ReqAllSweepDefinitions
 * @see ESPPacketId.ReqDefaultSweepDefinitions
 * @see SweepSection
 */
data class SweepDefinition(
    val index: Int,
    val lowerEdge: Int,
    val upperEdge: Int,
)

fun SweepDefinition.toPayload(
    commit: Boolean,
): ByteArray = ByteArray(5).apply {
    val commitBit = if (commit) COMMIT_BIT_MASK else 0x00
    this[0] = ((index and INDEX_MASK) or commitBit).toByte()
    this[1] = ((upperEdge and EDGE_MSB_MASK) shr 8).toByte()
    this[2] = (upperEdge and EDGE_LSB_MASK).toByte()
    this[3] = ((lowerEdge and EDGE_MSB_MASK) shr 8).toByte()
    this[4] = (lowerEdge and EDGE_LSB_MASK).toByte()
}

/**
 * A collection of sweep related information read from the connected Valentine One.
 *
 * @property maxSweepIndex Maximum supported index of a collection of sweep definitions stored
 * inside the Valentine One.
 * @property sweepSections List of current [SweepSection]s.
 * @property defaultSweepsDefinitions List of default [SweepDefinition]s. This may be an empty list
 * if the connected Valentine One does not support default sweep definitions.
 * @property customSweepsDefinitions List of custom [SweepDefinition]s.
 */
data class SweepData(
    val maxSweepIndex: Int,
    val sweepSections: List<SweepSection>,
    val defaultSweepsDefinitions: List<SweepDefinition>,
    val customSweepsDefinitions: List<SweepDefinition>,
)

internal const val EDGE_MSB_MASK: Int = 0x00_00_FF_00
internal const val EDGE_LSB_MASK: Int = 0x00_00_00_FF
private const val COMMIT_BIT_MASK: Int = 0b0100_0000
private const val INDEX_MASK: Int = 0b0011_1111