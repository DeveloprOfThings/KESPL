package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.gui
import hellov1.composeapp.generated.resources.read
import hellov1.composeapp.generated.resources.user_bytes
import hellov1.composeapp.generated.resources.write
import io.github.developrofthings.helloV1.ui.byteValue
import io.github.developrofthings.helloV1.ui.isHex
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.utilities.V1VersionInfo
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun rememberUserBytesState(
    userBytes: ByteArray = V1VersionInfo.UserSettingsInfo.V4_1000_UserBytes,
): UserBytesRowState {
    require(userBytes.size == 6) { "userBytes must be a ByteArray of `length == 6`" }
    return rememberSaveable(userBytes, saver = UserBytesRowState.Saver) {
        UserBytesRowState(userBytes = userBytes)
    }
}

fun UserBytesRowState(userBytes: ByteArray): UserBytesRowState = UserBytesRowState(
    userBytes = userBytes.map { userByte ->
        val initialText = userByte
            .toHexString()
            .uppercase()
        TextFieldState(
            initialText = initialText,
            initialSelection = TextRange(initialText.length),
        )
    }
)

class UserBytesRowState(
    private val userBytes: List<TextFieldState>,
) {
    operator fun get(index: Int): TextFieldState = userBytes[index]

    val userByte1TextFieldState: TextFieldState get() = userBytes[0]
    val userByte2TextFieldState: TextFieldState get() = userBytes[1]
    val userByte3TextFieldState: TextFieldState get() = userBytes[2]
    val userByte4TextFieldState: TextFieldState get() = userBytes[3]
    val userByte5TextFieldState: TextFieldState get() = userBytes[4]
    val userByte6TextFieldState: TextFieldState get() = userBytes[5]

    val userByte1Text: CharSequence get() = userByte1TextFieldState.text
    val userByte2Text: CharSequence get() = userByte2TextFieldState.text
    val userByte3Text: CharSequence get() = userByte3TextFieldState.text
    val userByte4Text: CharSequence get() = userByte4TextFieldState.text
    val userByte5Text: CharSequence get() = userByte5TextFieldState.text
    val userByte6Text: CharSequence get() = userByte6TextFieldState.text

    val userByte1: Byte get() = userByte1Text.byteValue()
    val userByte2: Byte get() = userByte2Text.byteValue()
    val userByte3: Byte get() = userByte3Text.byteValue()
    val userByte4: Byte get() = userByte4Text.byteValue()
    val userByte5: Byte get() = userByte5Text.byteValue()
    val userByte6: Byte get() = userByte6Text.byteValue()

    val currentUserBytes: ByteArray
        get() = byteArrayOf(
            userByte1,
            userByte2,
            userByte3,
            userByte4,
            userByte5,
            userByte6,
        )

    val userBytesFlow: Flow<ByteArray> = snapshotFlow { currentUserBytes }

    fun update(userBytes: ByteArray) {
        userByte1TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[0]
                .toHexString()
                .uppercase()
        )
        userByte2TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[1]
                .toHexString()
                .uppercase()
        )
        userByte3TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[2]
                .toHexString()
                .uppercase()
        )
        userByte4TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[3]
                .toHexString()
                .uppercase()
        )
        userByte5TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[4]
                .toHexString()
                .uppercase()
        )
        userByte6TextFieldState.setTextAndPlaceCursorAtEnd(
            userBytes[5]
                .toHexString()
                .uppercase()
        )
    }

    object Saver : androidx.compose.runtime.saveable.Saver<UserBytesRowState, Any> {
        override fun restore(value: Any): UserBytesRowState? =
            UserBytesRowState(
                userBytes = (value as List<*>).map { it: Any? ->
                    with(TextFieldState.Saver) { restore(it as Any) } as TextFieldState
                }
            )

        override fun SaverScope.save(value: UserBytesRowState): Any? =
            value.userBytes.map { userByte ->
                with(TextFieldState.Saver) { save(userByte) }
            }
    }
}

@Composable
fun UserBytesRow(
    modifier: Modifier = Modifier,
    userBytesState: UserBytesRowState,
    enabled: Boolean = true,
    onReadClick: () -> Unit,
    onWriteClick: (ByteArray) -> Unit,
    onGuiClick: () -> Unit,
) = Column(
    modifier
        .decorateUserBytes()
        .padding(
            start = 8.dp,
            top = 24.dp,
            end = 8.dp,
            bottom = 4.dp,
        )
        .semantics(mergeDescendants = true) {},
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(6) {
            val textFieldState = userBytesState[it]
            val isTextInvalidValid =
                textFieldState.text.isNotEmpty() && !textFieldState.text.isHex()
            HexField(
                textFieldState = textFieldState,
                modifier = Modifier
                    .width(38.dp)
                    // We need to disable focusability so that we don't cause scrollable content to
                    // automatically move to center on this field
                    .focusable(enabled = false),
                enabled = enabled,
                isError = isTextInvalidValid,
            )
        }
    }

    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onReadClick,
            enabled = enabled,
        ) { Text(text = stringResource(Res.string.read)) }

        Button(
            onClick = { onWriteClick(userBytesState.currentUserBytes) },
            enabled = enabled,
        ) { Text(text = stringResource(Res.string.write)) }

        Button(
            onClick = onGuiClick,
            enabled = enabled,
        ) { Text(text = stringResource(Res.string.gui)) }
    }
}

@Composable
private fun Modifier.decorateUserBytes(): Modifier {
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurface
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val label = stringResource(Res.string.user_bytes)

    return this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val textLayoutResult = textMeasurer.measure(text = label)
            val labelTopLeft = Offset(
                x = (size.width - textLayoutResult.size.width) / 2,
                y = 0F
            )

            val lineThickness = 1.5.dp.toPx()
            val path = Path().apply {
                addRoundRect(
                    roundRect = RoundRect(
                        left = lineThickness / 2,
                        // We want top edge to be vertically centered with the label text
                        top = textLayoutResult.size.center.y.toFloat(),
                        right = size.width - (lineThickness / 2),
                        bottom = size.height - (lineThickness / 2),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    ),
                )
            }

            val labelClearSize = Size(
                width = textLayoutResult.size.width + 8.dp.toPx(),
                height = textLayoutResult.size.height + 5.dp.toPx(),
            )

            onDrawBehind {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = lineThickness,
                    )
                )
                // CLear
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        x = (size.width - labelClearSize.width) / 2,
                        y = 0F,
                    ),
                    blendMode = BlendMode.Clear,
                    size = labelClearSize,
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    color = labelColor,
                    topLeft = labelTopLeft,
                )
            }
        }
}

@Preview
@Composable
private fun ESPButtonPanelPreview() {
    Valentine1Theme(
        darkTheme = true,
    ) {
        UserBytesRow(
            modifier = Modifier.wrapContentSize(),
            userBytesState = rememberUserBytesState(),
            onReadClick = {},
            onWriteClick = { },
            onGuiClick = { },
        )
    }
}