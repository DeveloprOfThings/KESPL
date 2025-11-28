import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.component.textColor
import io.github.developrofthings.helloV1.ui.isDigitsOnly

@Composable
fun IntField(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    maxLength: Int = 5,
    textAlign: TextAlign? = TextAlign.Center,
    style: TextStyle = LocalTextStyle.current,
    textPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp,
        vertical = 8.dp,
    ),
    interactionSource: MutableInteractionSource? = null,
    outputTransformation: OutputTransformation? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val textColor = MaterialTheme.colorScheme.textColor(
        enabled = enabled,
        isError = isError,
        focused = focused,
    )

    val mergedTextStyle = style.merge(
        TextStyle(
            color = textColor,
            textAlign = textAlign ?: TextAlign.Unspecified,
        )
    )

    BasicTextField(
        state = textFieldState,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        inputTransformation =
            InputTransformation.maxLength(maxLength)
                .then { if (!asCharSequence().isDigitsOnly()) revertAllChanges() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        lineLimits = TextFieldLineLimits.SingleLine,
        interactionSource = interactionSource,
        outputTransformation = outputTransformation,
        decorator = TextFieldDecorator { innerTextField ->
            val errorLineColor by animateColorAsState(
                targetValue = if (isError) MaterialTheme.colorScheme.error
                else Color.Transparent,
                label = "ErrorLineColor"
            )

            Box(
                Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                    .drawBehind {
                        val lineThickness = 1.5.dp.toPx()
                        drawLine(
                            color = errorLineColor,
                            start = Offset(
                                x = 0f,
                                y = size.height - lineThickness,
                            ),
                            end = Offset(
                                x = size.width,
                                y = size.height - lineThickness,
                            ),
                        )
                    }
                    .padding(paddingValues = textPadding),
                contentAlignment = Alignment.Center,
            ) {
                innerTextField()
            }
        }
    )
}