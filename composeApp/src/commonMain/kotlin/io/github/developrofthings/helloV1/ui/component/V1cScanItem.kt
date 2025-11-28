package io.github.developrofthings.helloV1.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun V1cScanResultItem(
    result: V1connectionScanResult,
    colors: Pair<Color, Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RSSIIndicator(
            rssi = result.rssi,
            colors = colors,
            modifier = Modifier
                .size(60.dp)
        )

        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = result.device.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = result.id,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RSSIIndicator(
    rssi: Int,
    colors: Pair<Color, Color>,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(colors) { colors.first.copy(alpha = .72F) }

    val progress by animateFloatAsState(
        targetValue = rssi.translateRssiToProgress(),
        label = "progress",
    )

    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            .drawBehind {
                drawRect(color = backgroundColor)
                val progressSize = Size(
                    width = size.width,
                    height = progress * size.height
                )
                drawRect(
                    color = colors.first,
                    topLeft = Offset(x = 0F, y = (size.height - progressSize.height)),
                    size = size,
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rssi",
            color = colors.second,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

fun translateValue(
    value: Int,
    sourceMin: Int,
    sourceMax: Int,
    destinationMin: Int,
    destinationMax: Int,
): Int = (destinationMax - destinationMin).let { targetRange ->
    val sourceRange = sourceMax - sourceMin
    val clamped = value.coerceIn(minimumValue = sourceMin, maximumValue = sourceMax)
    val offset = abs(clamped - sourceMin) / sourceRange.toFloat()
    (destinationMin + (targetRange * offset)).toInt()
}

fun Int.translateRssiToProgress(): Float = translateValue(
    value = this,
    sourceMin = -127,
    sourceMax = 128,
    destinationMin = 0,
    destinationMax = 255,
) / 255F


@Preview
@Composable
private fun RSSIIndicatorPreview() {
    RSSIIndicator(
        rssi = -10,
        colors = Color(0XFFfb8500) to Color.Black,
        modifier = Modifier.size(150.dp),
    )
}