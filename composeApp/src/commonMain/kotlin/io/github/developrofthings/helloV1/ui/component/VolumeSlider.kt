package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VolumeSlider(
    label: String,
    level: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier) {
    Text(text = label)
    Slider(
        value = level,
        onValueChange = onValueChange,
        valueRange = 0F..9F,
        steps = 8,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}