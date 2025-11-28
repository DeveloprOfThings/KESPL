package io.github.developrofthings.kespl.utilities.extensions.primitive

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.bit1Mask
import io.github.developrofthings.kespl.emptyByte
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

val Byte.isFromV1: Boolean get() {
    val value = this@isFromV1
    return (value == ESPDevice.ValentineOne.Checksum.originatorIdentifier) ||
            (value == ESPDevice.ValentineOne.NoChecksum.originatorIdentifier) ||
            (value == ESPDevice.ValentineOne.Legacy.originatorIdentifier)
}

infix fun Byte.shl(bitCount: Int): Byte = (this.toInt() shl bitCount).toByte()

infix fun Byte.shr(bitCount: Int): Byte = (this.toInt() shr bitCount).toByte()

fun Byte.isBitSet(index: Int): Boolean = (this and (bit1Mask shl index)) != emptyByte

operator fun Byte.get(index: Int): Boolean = this.isBitSet(index = index)

val Byte.first: Boolean get() = this[0]

val Byte.last: Boolean get() = this[7]

operator fun Byte.set(index: Int, value: Boolean): Byte = if (value) {
    (this or (true.toByte() shl index))
} else (this and (true.toByte() shl index).inv())

fun Boolean.toByte(): Byte = if (this) 0x01 else 0x00

fun Byte.toBoolean(): Boolean = (this != emptyByte)
