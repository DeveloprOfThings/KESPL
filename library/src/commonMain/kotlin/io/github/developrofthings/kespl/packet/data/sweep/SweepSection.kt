package io.github.developrofthings.kespl.packet.data.sweep

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.payloadLength

typealias ResponseSweepSection = ESPPacket

/**
 * Count of [SweepSection] contained in this [ESPPacket]. Possible values are (0-3).
 */
private val ByteArray.sweepSectionCount: Int
    get() {
        val pLength = payloadLength
        return when {
            15 <= pLength -> 3
            10 <= pLength -> 2
            5 <= pLength -> 1
            else -> 0
        }
    }

/**
 * Count of [SweepSection] contained in this [ESPPacket]. Possible values are (0-3).
 */
@Suppress("unused")
private val ResponseSweepSection.sweepSectionCount: Int
    get() = this.bytes.sweepSectionCount

fun ResponseSweepSection.sweepSections(): List<SweepSection> = this.bytes.sweepSections()

fun ByteArray.sweepSections(): List<SweepSection> = buildList {
    repeat(sweepSectionCount) {
        add(
            (it * SWEEP_SECTIONS_BYTE_COUNT).let { offset ->
                val lowerEdgeMSB =
                    this@sweepSections[PAYLOAD_START_IDX + (offset + 1)].toInt() and 0xFF
                val lowerEdgeLSB =
                    this@sweepSections[PAYLOAD_START_IDX + (offset + 2)].toInt() and 0xFF
                val upperEdgeMSB =
                    this@sweepSections[PAYLOAD_START_IDX + (offset + 3)].toInt() and 0xFF
                val upperEdgeLSB =
                    this@sweepSections[PAYLOAD_START_IDX + (offset + 4)].toInt() and 0xFF

                SweepSection(
                    indexCount = this@sweepSections[PAYLOAD_START_IDX + offset],
                    lowerEdge = ((upperEdgeMSB shl 8) + upperEdgeLSB),
                    upperEdge = ((lowerEdgeMSB shl 8) + lowerEdgeLSB),
                )
            }
        )
    }
}

/**
 * Range of frequencies that a Valentine One will sweep through in which user defined
 * [SweepDefinition] can be defined.
 *
 * @property index The index of this sweep section.
 * @property count The total number of sweeps sections.
 * @property lowerEdge The lower frequency edge for this sweep section in MHz.
 * @property upperEdge The upper frequency edge for this sweep section in MHz.
 *
 * @see SweepDefinition
 */
data class SweepSection(
    private val indexCount: Byte,
    val lowerEdge: Int,
    val upperEdge: Int,
) {
    val index: Int = (indexCount.toInt() and INDEX_MASK) shr 4

    val count: Int = (indexCount.toInt() and COUNT_MASK)
}

fun SweepSection(
    index: Int,
    count: Int,
    lowerEdge: Int,
    upperEdge: Int,
): SweepSection = SweepSection(
    indexCount = (((index and INDEX_MASK) shl 4) or (count and COUNT_MASK)).toByte(),
    lowerEdge = lowerEdge,
    upperEdge = upperEdge,
)

internal fun List<SweepSection>.toPayload(): ByteArray {
    val length = when {
        size == 1 -> 5
        size == 2 -> 10
        size >= 3 -> 15
        else -> 0
    }
    return ByteArray(length).apply {
        var destIdx = 0
        this@toPayload.forEachIndexed { i, swp ->
            this[(destIdx * SWEEP_SECTIONS_BYTE_COUNT) + 0] =
                (((swp.index shl 4) and INDEX_MASK) or (swp.count and COUNT_MASK)).toByte()
            this[(destIdx * SWEEP_SECTIONS_BYTE_COUNT) + 1] =
                ((swp.upperEdge and EDGE_MSB_MASK) shr 8).toByte()
            this[(destIdx * SWEEP_SECTIONS_BYTE_COUNT) + 2] =
                (swp.upperEdge and EDGE_LSB_MASK).toByte()
            this[(destIdx * SWEEP_SECTIONS_BYTE_COUNT) + 3] =
                ((swp.lowerEdge and EDGE_MSB_MASK) shr 8).toByte()
            this[(destIdx * SWEEP_SECTIONS_BYTE_COUNT) + 4] =
                (swp.lowerEdge and EDGE_LSB_MASK).toByte()
            destIdx++
        }
    }
}

private const val SWEEP_SECTIONS_BYTE_COUNT: Int = 5
private const val COUNT_MASK: Int = 0x0F
private const val INDEX_MASK: Int = 0xF0