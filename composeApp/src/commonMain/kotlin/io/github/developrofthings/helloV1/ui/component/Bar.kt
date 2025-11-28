package io.github.developrofthings.helloV1.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

val unlitBarColor: Color = Color(0xFF2F0000)
val litBarColor: Color = Color(0xFFCB0000)

@Composable
fun Bar(
    lit: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    shape: Shape = CircleShape,
) {
    val color by animateColorAsState(targetValue = if (lit) litBarColor else unlitBarColor)
    Box(
        modifier = modifier
            .size(size = size)
            .background(
                color = color,
                shape = shape,
            ),
    )
}

@Preview
@Composable
private fun UnlitBarPreview() {
    Bar(lit = false)
}

@Preview
@Composable
private fun litBarPreview() {
    Bar(lit = true)
}