import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.intValue
import io.github.developrofthings.helloV1.ui.intValueSafe
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
fun rememberSweepSectionRowState(sweepSection: SweepSection): SweepSectionRowState =
    rememberSaveable(saver = SweepSectionRowState.Saver) { SweepSectionRowState(sweepSection) }

class SweepSectionRowState(
    val index: Int,
    val count: Int,
    val lowerTextFieldState: TextFieldState,
    val upperTextFieldState: TextFieldState,
) {
    val lowerEdge: Int = lowerTextFieldState.text.intValue()

    val upperEdge: Int = lowerTextFieldState.text.intValue()

    val sweepSection: Flow<SweepSection>
        get() = snapshotFlow {
            lowerTextFieldState.text.intValueSafe() to
                    upperTextFieldState.text.intValueSafe()
        }.map { (lower, upper) ->
            SweepSection(
                index = index,
                count = count,
                lowerEdge = lower,
                upperEdge = upper,
            )
        }

    object Saver : androidx.compose.runtime.saveable.Saver<SweepSectionRowState, Any> {
        override fun restore(value: Any): SweepSectionRowState? =
            (value as List<*>).let { state ->
                SweepSectionRowState(
                    index = state[0] as Int,
                    count = state[1] as Int,
                    lowerTextFieldState = with(TextFieldState.Saver) { state[2] } as TextFieldState,
                    upperTextFieldState = with(TextFieldState.Saver) { state[3] } as TextFieldState,
                )
            }

        override fun SaverScope.save(value: SweepSectionRowState): Any? = listOf(
            value.index,
            value.count,
            with(TextFieldState.Saver) { save(value.lowerTextFieldState) },
            with(TextFieldState.Saver) { save(value.upperTextFieldState) },
        )
    }
}

fun SweepSectionRowState(sweepSection: SweepSection): SweepSectionRowState {
    val lowerText = sweepSection.lowerEdge.toString()
    val upperText = sweepSection.upperEdge.toString()
    return SweepSectionRowState(
        index = sweepSection.index,
        count = sweepSection.count,
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
internal fun SweepSectionRow(
    sweepSectionState: SweepSectionRowState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = true,
) = Row(
    modifier = modifier
        .semantics(mergeDescendants = true) {},
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = "${sweepSectionState.index} / ${sweepSectionState.count}",
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(55.dp),
        style = MaterialTheme.typography.bodyLarge,
    )

    Row(
        modifier = Modifier.weight(1F),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        IntField(
            textFieldState = sweepSectionState.lowerTextFieldState,
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

        IntField(
            textFieldState = sweepSectionState.upperTextFieldState,
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
    }
}