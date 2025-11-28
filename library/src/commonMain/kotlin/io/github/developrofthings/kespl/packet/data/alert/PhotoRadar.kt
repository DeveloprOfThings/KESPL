package io.github.developrofthings.kespl.packet.data.alert

import kotlin.experimental.and

/**
 * Represents possible Photo Radar types that the Valentine One Gen2 is capable of detecting.
 *  Reference: Table 9.7 ESP Specification v. 3.013
 *
 * @since V4.1037
 */
enum class PhotoRadar(internal val value: Byte) {
    MRCT(0x01),
    DriveSafeType1(0x02),
    DriveSafeType2(0x03),
    RedflexHalo(0x04),
    RedflexNK7(0x05),
    Ekin(0x06),
    None(0x00);

    companion object {
        fun fromByte(b: Byte): PhotoRadar = when(b and PHOTO_RADAR_MASK) {
            _MRCT -> MRCT
           DRIVESAFETYPE1 -> DriveSafeType1
           DRIVESAFETYPE2 -> DriveSafeType2
           REDFLEXHALO -> RedflexHalo
           REDFLEXNK7 -> RedflexNK7
           EKIN -> Ekin
           else -> None
        }
    }
}

private const val PHOTO_RADAR_MASK: Byte = 0x0F.toByte()
private const val _MRCT: Byte = 0x01
private const val DRIVESAFETYPE1: Byte = 0x02
private const val DRIVESAFETYPE2: Byte = 0x03
private const val REDFLEXHALO: Byte = 0x04
private const val REDFLEXNK7: Byte = 0x05
private const val EKIN: Byte = 0x06
