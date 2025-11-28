package io.github.developrofthings.helloV1.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun EdgeToEdge(
    lightIcons: Boolean = isSystemInDarkTheme(),
    statusBarColor: Color = Color.Transparent,
    navBarColor: Color = Color.Transparent,
)

@OptIn(ExperimentalStdlibApi::class)
fun CharSequence.byteValue(): Byte = this.toString().hexToByte()

@OptIn(ExperimentalStdlibApi::class)
fun CharSequence.intValue(): Int = this.toString().toInt()

fun CharSequence.intValueSafe(): Int = if(isEmpty()) 0 else this.toString().toInt()

fun Int.toKPH(): Int = (this * MPH_TO_KPH_CONVERSION_SCALER).toInt()

fun Int.toMPH(): Int = (this * KPH_TO_MPH_CONVERSION_SCALER).toInt()

fun CharSequence.isHex(): Boolean {
    if (isEmpty() ||
        (this[0] != '-' && (this[0].digitToIntOrNull(16) ?: -1) == -1)
    ) return false

    if (this.length == 1 && this[0] == '-') return false

    for (i in 1..<this.length) if ((this[i].digitToIntOrNull(16) ?: -1) == -1) return false
    return true
}

fun CharSequence.isDigitsOnly(): Boolean = this.all { it.isDigit() }

private const val MPH_TO_KPH_CONVERSION_SCALER: Float = 1.60934F
private const val KPH_TO_MPH_CONVERSION_SCALER: Float = 1 / MPH_TO_KPH_CONVERSION_SCALER