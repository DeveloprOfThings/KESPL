package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.toggle
import org.jetbrains.compose.resources.stringResource

@Composable
fun CheckableText(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) = Row(
    modifier = modifier
        .clickable(
            onClickLabel = stringResource(Res.string.toggle, text),
            onClick = { onCheckedChange(!checked) },
            indication = null,
            interactionSource = null
        ),
    horizontalArrangement = Arrangement.spacedBy(spacing),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = text,
        maxLines = 1,
        style = style,
        overflow = TextOverflow.Ellipsis,
    )

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(8.dp),
        )
    }
}