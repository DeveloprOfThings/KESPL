package io.github.developrofthings.kespl.utilities.extensions.primitive

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ORIG_INDENTIFIER_BASE_CONST
import kotlin.experimental.or
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.text.get
import kotlin.text.set
import kotlin.text.toBoolean

class ByteExtTest {

    @Test
    fun `When Byte value equal to V1 with Checksum Device Identifier, then Byte_isFromV1 == true`() {
        assertTrue(actual = (ORIG_INDENTIFIER_BASE_CONST or 0x0A).isFromV1)
    }

    @Test
    fun `When Byte value equal to V1 without Checksum Device Identifier, then Byte_isFromV1 == true`() {
        assertTrue(actual = (ORIG_INDENTIFIER_BASE_CONST or 0x09).isFromV1)
    }

    @Test
    fun `When Byte value equal to Legacy V1 Device Identifier, then Byte_isFromV1 == true`() {
        assertTrue(actual = (ORIG_INDENTIFIER_BASE_CONST or 0x98.toByte()).isFromV1)
    }

    @Test
    fun `When Byte value does not equal V1 Device Identifier, then Byte_isFromV1 == false`() {
        (0..255).forEach { value ->
            val bValue = (ORIG_INDENTIFIER_BASE_CONST or value.toByte())
            if(
                (bValue == ESPDevice.ValentineOne.Checksum.originatorIdentifier) or
                (bValue == ESPDevice.ValentineOne.NoChecksum.originatorIdentifier) or
                (bValue == ESPDevice.ValentineOne.Legacy.originatorIdentifier)
            ) return@forEach
            assertFalse(actual = bValue.isFromV1)
        }
    }

    @Test
    fun `When left shifting byte value == 0x00 then result value == 0`() {
        val bValue = 0x00.toByte()
        (0..7).forEach { shl  ->
            assertEquals(
                expected = 0x00.toByte(),
                actual = bValue.shl(shl)
            )
        }
    }

    @Test
    fun `When left shifting byte value == 0x01, then result value equals the expected value`() {
        val bValue = 0x01.toByte()
        val expectedValues = byteArrayOf(
            0x01.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            0x10.toByte(),
            0x20.toByte(),
            0x40.toByte(),
            0x80.toByte(),
        )
        (0..7).forEachIndexed { index, shl  ->
            assertEquals(
                expected = expectedValues[index],
                actual = bValue.shl(shl),
            )
        }
    }

    @Test
    fun `When right shifting byte value == 0x00 then result value == 0`() {
        val bValue = 0x00.toByte()
        (0..7).forEach { shl  ->
            assertEquals(
                expected = 0x00.toByte(),
                actual = bValue.shr(shl)
            )
        }
    }

    @Test
    fun `When right shifting byte value == 0x01, then result value equals the expected value`() {
        val bValue = 0x80.toByte()
        val expectedValues = byteArrayOf(
            0x80.toByte(),
            0xC0.toByte(),
            0xE0.toByte(),
            0xF0.toByte(),
            0xF8.toByte(),
            0xFC.toByte(),
            0xFE.toByte(),
            0xFF.toByte(),
        )
        (0..7).forEachIndexed { index, shl  ->
            val actual = bValue.shr(shl)
            assertEquals(
                expected = expectedValues[index],
                actual = actual,
            )
        }
    }

    @Test
    fun `When byte value == 0x00, then Byte_isBitSet(Index) returns false for all bit indices`() {
        val bValue = 0x00.toByte()
        (0..7).forEach {
            assertFalse(actual = bValue.isBitSet(index = it))
        }
    }

    @Test
    fun `When bit is set in a byte, then Byte_isBitSet(Index) returns true for the corresponding index`() {
        val expectedValues = byteArrayOf(
            0x01.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            0x10.toByte(),
            0x20.toByte(),
            0x40.toByte(),
            0x80.toByte(),
        )

        (0..7).forEach {
            assertTrue(actual = expectedValues[it].isBitSet(index = it))
        }
    }

    @Test
    fun `When byte value == 0xFF, then Byte_isBitSet(Index) returns true for or all bit indices`() {
        val bValue = 0xFF.toByte()
        (0..7).forEach {
            assertTrue(actual = bValue.isBitSet(index = it))
        }
    }

    @Test
    fun `When byte value == 0x00, then Byte_get(Index) returns false for all bit indices`() {
        val bValue = 0x00.toByte()
        (0..7).forEach {
            assertFalse(actual = bValue.get(index = it))
        }
    }

    @Test
    fun `When bit is set in a byte, then Byte_get(Index) returns true for the corresponding index`() {
        val expectedValues = byteArrayOf(
            0x01.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            0x10.toByte(),
            0x20.toByte(),
            0x40.toByte(),
            0x80.toByte(),
        )
        (0..7).forEach {
            assertTrue(actual = expectedValues[it].get(index = it))
        }
    }

    @Test
    fun `When byte value == 0xFF, then Byte_get(Index) returns true for or all bit indices`() {
        val bValue = 0xFF.toByte()
        (0..7).forEach {
            assertTrue(actual = bValue.get(index = it))
        }
    }

    @Test
    fun `When byte value == 0x00, then Byte_first returns false`() {
        val bValue = 0x00.toByte()
        assertFalse(actual = bValue.first)
    }

    @Test
    fun `When byte value == 0x01, then Byte_first returns false`() {
        val bValue = 0x01.toByte()
        assertTrue(actual = bValue.first)
    }

    @Test
    fun `When byte value == 0xFF, then Byte_first returns false`() {
        val bValue = 0xFF.toByte()
        assertTrue(actual = bValue.first)
    }

    @Test
    fun `When byte value == 0x00, then Byte_last returns false`() {
        val bValue = 0x00.toByte()
        assertFalse(actual = bValue.last)
    }

    @Test
    fun `When byte value == 0x01, then Byte_last returns false`() {
        val bValue = 0x01.toByte()
        assertFalse(actual = bValue.last)
    }

    @Test
    fun `When byte value == 0xFF, then Byte_last returns false`() {
        val bValue = 0xFF.toByte()
        assertTrue(actual = bValue.last)
    }

    @Test
    fun `When setting a bit at index to true, then the result value equals expected value`() {
        val expected = byteArrayOf(
            0x01.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            0x10.toByte(),
            0x20.toByte(),
            0x40.toByte(),
            0x80.toByte(),
        )

        val bValue = 0x00.toByte()
        (0..7).forEach {
            assertEquals(
                expected = expected[it],
                actual = bValue.set(it, true)
            )
        }
    }

    @Test
    fun `When clearing a bit at index to true, then the result value equals expected value`() {
        val expected = byteArrayOf(
            (0b1111_1110).toByte(),
            (0b1111_1101).toByte(),
            (0b1111_1011).toByte(),
            (0b1111_0111).toByte(),
            (0b1110_1111).toByte(),
            (0b1101_1111).toByte(),
            (0b1011_1111).toByte(),
            (0b0111_1111).toByte(),
        )

        val bValue = 0xFF.toByte()
        (0..7).forEach {
            val actual = bValue.set(it, false)
            assertEquals(
                expected = expected[it],
                actual = actual
            )
        }
    }

    @Test
    fun `When boolean value == false, the Boolean_toByte == 0x00`() {
        assertEquals(
            expected = 0x00.toByte(),
            actual = false.toByte()
        )
    }

    @Test
    fun `When boolean value == true, the Boolean_toByte == 0x01`() {
        assertEquals(
            expected = 0x01.toByte(),
            actual = true.toByte()
        )
    }

    @Test
    fun `When byte value == 0x00, then Byte_toBoolean = false`() {
        val bValue = 0x00.toByte()
        assertFalse(actual = bValue.toBoolean())
    }

    @Test
    fun `When byte values != 0x00, then Byte_toBoolean = true`() {
        (1..254).forEach {
            assertTrue(actual = (it).toByte().toBoolean())
        }
    }
}