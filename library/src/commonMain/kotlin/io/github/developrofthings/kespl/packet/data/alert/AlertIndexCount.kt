package io.github.developrofthings.kespl.packet.data.alert

import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.jvm.JvmInline

/**
 * Represents the alert index count data.
 *
 * This class provides access to the individual bits, count, and index of the alert index count.
 *
 * @property data The raw byte data representing the alert index count.
 */
@JvmInline
value class AlertIndexCount(private val data: Byte) {
    /**
     * Returns the bit at the specified index in the alert index count data.
     */
    operator fun get(index: Int): Boolean = data.isBitSet(index = index)

    /**
     * Number of alerts present in the alert table this [AlertIndexCount] corresponds to.
     */
    val count: Int get() = (data.toInt() and ALERT_COUNT_MASK)

    /**
     * The index of the alert this [AlertIndexCount] corresponds to inside of the alert table.
     */
    val index: Int get() = ((data.toInt() and ALERT_INDEX_MASK) shr 4)
}

internal const val ALERT_INDEX_MASK: Int = 0xF0

internal const val ALERT_COUNT_MASK: Int = 0x0F
