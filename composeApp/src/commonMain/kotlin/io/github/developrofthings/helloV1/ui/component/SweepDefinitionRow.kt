import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.intValue
import io.github.developrofthings.helloV1.ui.intValueSafe
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.ic_close
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.vectorResource

@Composable
fun rememberSweepDefinitionRowState(
    sweepDefinition: SweepDefinition,
): SweepDefinitionRowState =
    rememberSaveable(saver = SweepDefinitionRowState.Saver) {
        SweepDefinitionRowState(sweepDefinition)
    }

class SweepDefinitionRowState(
    val sweepIndex: Int,
    val lowerTextFieldState: TextFieldState,
    val upperTextFieldState: TextFieldState,
) {
    val lowerEdge: Int get() = lowerTextFieldState.text.intValue()

    val upperEdge: Int get() = upperTextFieldState.text.intValue()

    val sweep: Flow<SweepDefinition>
        get() = snapshotFlow {
            lowerTextFieldState.text.intValueSafe() to
                    upperTextFieldState.text.intValueSafe()
        }.map { (lower, upper) ->
            SweepDefinition(
                index = sweepIndex,
                lowerEdge = lower,
                upperEdge = upper,
            )
        }

    object Saver : androidx.compose.runtime.saveable.Saver<SweepDefinitionRowState, Any> {
        override fun restore(value: Any): SweepDefinitionRowState? =
            (value as List<*>).let { state ->
                SweepDefinitionRowState(
                    sweepIndex = state[0] as Int,
                    lowerTextFieldState = with(TextFieldState.Saver) { state[1] } as TextFieldState,
                    upperTextFieldState = with(TextFieldState.Saver) { state[2] } as TextFieldState,
                )
            }

        override fun SaverScope.save(value: SweepDefinitionRowState): Any? = listOf(
            value.sweepIndex,
            with(TextFieldState.Saver) { save(value.lowerTextFieldState) },
            with(TextFieldState.Saver) { save(value.upperTextFieldState) },
        )
    }
}

fun SweepDefinitionRowState(sweepDefinition: SweepDefinition): SweepDefinitionRowState {
    val lowerText = sweepDefinition.lowerEdge.toString()
    val upperText = sweepDefinition.upperEdge.toString()
    return SweepDefinitionRowState(
        sweepIndex = sweepDefinition.index,
        lowerTextFieldState = TextFieldState(
            initialText = lowerText,
            initialSelection = TextRange(lowerText.length),
        ),
        upperTextFieldState = TextFieldState(
            initialText = upperText,
            initialSelection = TextRange(upperText.length),
        ),
    )
}

@Composable
internal fun SweepDefinitionRow(
    sweepDefState: SweepDefinitionRowState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) = Row(
    modifier = modifier
        .semantics(mergeDescendants = true) {},
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = "${sweepDefState.sweepIndex}).",
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(48.dp),
        style = MaterialTheme.typography.bodyLarge,
    )

    Row(
        modifier = Modifier
            .weight(1F),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntField(
                textFieldState = sweepDefState.lowerTextFieldState,
                modifier = Modifier
                    .width(105.dp),
                enabled = enabled,
                readOnly = readOnly,
                style = MaterialTheme.typography.bodyLarge,
                textPadding = PaddingValues(
                    horizontal = 2.dp,
                    vertical = 8.dp,
                ),
                outputTransformation = { insert(this.length, " Mhz") },
            )

            AnimatedVisibility(visible = !readOnly) {
                Image(
                    imageVector = vectorResource(Res.drawable.ic_close),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            enabled = true,
                            onClickLabel = null,
                            onClick = {
                                sweepDefState.lowerTextFieldState.setTextAndPlaceCursorAtEnd("0")
                            },
                            role = Role.Button,
                            indication = null,
                            interactionSource = null,
                        ),
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntField(
                textFieldState = sweepDefState.upperTextFieldState,
                modifier = Modifier
                    .width(105.dp),
                enabled = enabled,
                readOnly = readOnly,
                style = MaterialTheme.typography.bodyLarge,
                textPadding = PaddingValues(
                    horizontal = 2.dp,
                    vertical = 8.dp,
                ),
                outputTransformation = { insert(this.length, " Mhz") },
            )

            AnimatedVisibility(visible = !readOnly) {
                Image(
                    imageVector = vectorResource(Res.drawable.ic_close),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            enabled = true,
                            onClickLabel = null,
                            onClick = {
                                sweepDefState.upperTextFieldState.setTextAndPlaceCursorAtEnd("0")
                            },
                            role = Role.Button,
                            indication = null,
                            interactionSource = null,
                        ),
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}