package io.github.developrofthings.kespl

/*
 * Constants that represents the Seven Segment display possible values.
 */
/**Constant value that represents the Valentine One's Seven Segment displaying the character '0' */
const val SEVEN_SEG_VALUE_0: Byte = 0x3f

/**Constant value that represents the Valentine One's Seven Segment displaying the character '1' */
const val SEVEN_SEG_VALUE_1: Byte = 0x06

/**Constant value that represents the Valentine One's Seven Segment displaying the character '2' */
const val SEVEN_SEG_VALUE_2: Byte = 0x5B

/**Constant value that represents the Valentine One's Seven Segment displaying the character '3' */
const val SEVEN_SEG_VALUE_3: Byte = 0x4F

/**Constant value that represents the Valentine One's Seven Segment displaying the character '4' */
const val SEVEN_SEG_VALUE_4: Byte = 0x66

/**Constant value that represents the Valentine One's Seven Segment displaying the character '5' */
const val SEVEN_SEG_VALUE_5: Byte = 0x6D

/**Constant value that represents the Valentine One's Seven Segment displaying the character '6' */
const val SEVEN_SEG_VALUE_6: Byte = 0x7D

/**Constant value that represents the Valentine One's Seven Segment displaying the character '7' */
const val SEVEN_SEG_VALUE_7: Byte = 0x07

/**Constant value that represents the Valentine One's Seven Segment displaying the character '8' */
const val SEVEN_SEG_VALUE_8: Byte = 0x7F

/**Constant value that represents the Valentine One's Seven Segment displaying the character '9' */
const val SEVEN_SEG_VALUE_9: Byte = 0x6F

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'A' */
const val SEVEN_SEG_VALUE_A: Byte = 0x77

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'B' */
const val SEVEN_SEG_VALUE_b: Byte = 0x7C

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'C' */
const val SEVEN_SEG_VALUE_C: Byte = 0x39

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'D' */
const val SEVEN_SEG_VALUE_d: Byte = 0x5E

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'E' */
const val SEVEN_SEG_VALUE_E: Byte = 0x79

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'F' */
const val SEVEN_SEG_VALUE_F: Byte = 0x71

/**Constant value that represents the Valentine One's Seven Segment displaying the character '#' */
const val SEVEN_SEG_VALUE_POUND: Byte = 0x49

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'l' */
const val SEVEN_SEG_VALUE_l: Byte = 0x18

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'L' */
const val SEVEN_SEG_VALUE_L: Byte = 0x38

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'J' */
const val SEVEN_SEG_VALUE_J: Byte = 0x1E

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'c' */

const val SEVEN_SEG_VALUE_c: Byte = 0x58

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'U' */
const val SEVEN_SEG_VALUE_U: Byte = 0x3E

/**Constant value that represents the Valentine One's Seven Segment displaying the character 'u' */
const val SEVEN_SEG_VALUE_u: Byte = 0x1C

internal const val NO_CHECKSUM_FRAMING_BYTES: Int =  6

internal const val CHECKSUM_FRAMING_BYTES: Int =  7

const val ESP_PACKET_SOF = 0xAA.toByte()
const val ESP_PACKET_EOF = 0xAB.toByte()
const val DEST_INDENTIFIER_BASE_CONST = 0xD0.toByte()
const val ORIG_INDENTIFIER_BASE_CONST = 0xE0.toByte()
const val PACKET_DELIMITER_BYTE: Byte = 0x7F
const val DATA_LINK_ESCAPE_BYTE_7D: Byte = 0X7D
const val DATA_LINK_ESCAPE_BYTE_5F: Byte = 0X5F
const val DATA_LINK_ESCAPE_BYTE_5D: Byte = 0X5D

const val LEGACY_FRAMING_BYTES: Int = 4

const val SOF_IDX = 0
const val DEST_IDX = 1
const val ORIG_IDX = 2
const val PACK_ID_IDX = 3
const val PAYLOAD_LEN_IDX = 4
const val PAYLOAD_START_IDX = 5

internal const val emptyByte: Byte = 0b0000_0000

/**
 * Bit mask where the first bit is set.
 */
internal const val bit1Mask: Byte = 0b0000_0001
/**
 * Bit mask where the second bit is set.
 */
internal const val bit2Mask: Byte = 0b0000_0010
/**
 * Bit mask where the third bit is set.
 */
internal const val bit3Mask: Byte = 0b0000_0100
/**
 * Bit mask where the fourth bit is set.
 */
internal const val bit4Mask: Byte = 0b0000_1000
/**
 * Bit mask where the fifth bit is set.
 */
internal const val bit5Mask: Byte = 0b0001_0000
/**
 * Bit mask where the sixth bit is set.
 */
internal const val bit6Mask: Byte = 0b0010_0000
/**
 * Bit mask where the seventh bit is set.
 */
internal const val bit7Mask: Byte = 0b0100_0000
/**
 * Bit mask where the eighth bit is set.
 */
internal const val bit8Mask: Byte = (0b1000_0000).toByte()

internal const val fullByte: Byte = 0b1111_1111.toByte()