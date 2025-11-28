package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.kespl.packet.data.displayData.BandArrowIndicator
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.k_band
import hellov1.composeapp.generated.resources.ka_band
import hellov1.composeapp.generated.resources.ku_band
import hellov1.composeapp.generated.resources.laser_abrv
import hellov1.composeapp.generated.resources.x_band
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Bands {
    Laser,
    Ka,
    K,
    X,
    Ku;


    @get:Composable
    val indicatorLabel: String
        get() = stringResource(
            when (this) {
                Laser -> Res.string.laser_abrv
                Ka -> Res.string.ka_band
                K -> Res.string.k_band
                X -> Res.string.x_band
                Ku -> Res.string.ku_band
            }
        )
}

private val indicatorBands = Bands.entries.filterNot { it == Bands.Ku }

@Composable
fun BandsIndicator(
    bands: BandArrowIndicator,
    modifier: Modifier = Modifier,
    textWidth: Dp = 15.dp,
    barSize: Dp = 30.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    textStyle: TextStyle = LocalTextStyle.current,
) = Column(
    modifier = modifier,
    verticalArrangement = verticalArrangement,
    horizontalAlignment = Alignment.Start,
) {
    indicatorBands.forEachIndexed { i, band ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = band.indicatorLabel,
                modifier = Modifier.width(textWidth),
                color = Color.White,
                textAlign = TextAlign.Start,
                style = textStyle,
                maxLines = 1,
            )
            Bar(
                lit = bands[i],
                modifier = Modifier,
                size = barSize,
            )
        }
    }
}

@Preview
@Composable
private fun NoBandsBandIndicatorPreview() {
    BandsIndicator(
        bands = BandArrowIndicator(0x00),
        modifier = Modifier.wrapContentWidth(),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}

@Preview
@Composable
private fun LaserBandIndicatorPreview() {
    BandsIndicator(
        bands = BandArrowIndicator(0x01),
        modifier = Modifier.wrapContentWidth(),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}

@Preview
@Composable
private fun KaBandIndicatorPreview() {
    BandsIndicator(
        bands = BandArrowIndicator(0x02),
        modifier = Modifier.wrapContentWidth(),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}

@Preview
@Composable
private fun KBandIndicatorPreview() {
    BandsIndicator(
        bands = BandArrowIndicator(0x04),
        modifier = Modifier.wrapContentWidth(),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}

@Preview
@Composable
private fun XBandIndicatorPreview() {
    BandsIndicator(
        bands = BandArrowIndicator(0x08),
        modifier = Modifier.wrapContentWidth(),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}


