package io.github.developrofthings.kespl.packet.data.alert

import kotlin.experimental.and

enum class AlertArrow(internal val value: Byte) {
    Front(FRONT),
    Side(SIDE),
    Rear(REAR),
    None(0x00);

    companion object {
        fun fromByte(b: Byte): AlertArrow = when (b and ARROW_MASK) {
            FRONT -> Front
            SIDE -> Side
            REAR -> Rear
            else -> None
        }
    }
}

internal const val ARROW_MASK: Byte = 0xE0.toByte()
private const val FRONT: Byte = 0x20
private const val SIDE: Byte = 0x40
private const val REAR: Byte = 0x80.toByte()