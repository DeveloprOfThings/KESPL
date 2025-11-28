package io.github.developrofthings.kespl.packet.data.alert

import io.github.developrofthings.kespl.fullByte
import kotlin.experimental.and

enum class AlertBand(internal val value: Byte) {
    Laser(LASER),
    Ka(KA_BAND),
    K(K_BAND),
    X(X_BAND),
    Ku(KU_BAND),
    Photo(fullByte),
    None(0x00);

    companion object {
        fun fromByte(b: Byte): AlertBand = when (b and BAND_MASK) {
            LASER -> Laser
            KA_BAND -> Ka
            K_BAND -> K
            X_BAND -> X
            KU_BAND -> Ku
            else -> None
        }
    }
}

private const val BAND_MASK: Byte = 0x1F.toByte()
private const val LASER: Byte = 0x01
private const val KA_BAND: Byte = 0x02
private const val K_BAND: Byte = 0x04
private const val X_BAND: Byte = 0x08
private const val KU_BAND: Byte = 0x16
