package io.github.developrofthings.helloV1.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LabelButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    enabled: Boolean = true,
) = Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
) {
    Text(
        text = label,
        maxLines = maxLines,
        overflow = overflow,
    )
}