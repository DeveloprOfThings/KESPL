package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.kespl.packet.data.displayData.SignalStrengthBarGraph
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.strength
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BarGraph(
    strength: SignalStrengthBarGraph,
    modifier: Modifier = Modifier,
    barSize: Dp = 30.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    textStyle: TextStyle = LocalTextStyle.current,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(8) {
            Bar(
                lit = strength[it],
                modifier = Modifier,
                size = barSize,
            )
        }
    }
    Text(
        text = stringResource(Res.string.strength),
        modifier = Modifier.padding(start = barSize / 2),
        color = Color.White,
        textAlign = TextAlign.Start,
        style = textStyle,
    )
}


@Preview
@Composable
private fun `0LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x00), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `1LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x01), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `2LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x02), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `3LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x04), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `4LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x08), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `5LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x10), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `6LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x20), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `7LightBarGraphPreview`() {
    BarGraph(strength = SignalStrengthBarGraph(0x40), modifier = Modifier.wrapContentWidth())
}

@Preview
@Composable
private fun `8LightBarGraphPreview`() {
    BarGraph(
        strength = SignalStrengthBarGraph(0x80.toByte()),
        modifier = Modifier.wrapContentWidth()
    )
}