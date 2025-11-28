package io.github.developrofthings.helloV1.ui.component

import SweepSectionRow
import SweepSectionRowState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.utilities.extensions.primitive.shl
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.idx_cnt
import hellov1.composeapp.generated.resources.lower
import hellov1.composeapp.generated.resources.upper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.experimental.or

@Composable
fun rememberSweepSectionColumnState(
    sweepSections: List<SweepSection> = emptyList(),
): SweepSectionColumnState =
    rememberSaveable(
        sweepSections,
        saver = SweepSectionColumnState.Saver) {
        SweepSectionColumnState(
            sweepSectionStates = sweepSections.map {
                val lowerText = it.lowerEdge.toString()
                val upperText = it.upperEdge.toString()
                SweepSectionRowState(
                    index = it.index,
                    count = it.count,
                    lowerTextFieldState = TextFieldState(
                        initialText = lowerText,
                        initialSelection = TextRange(lowerText.length),
                    ),
                    upperTextFieldState = TextFieldState(
                        initialText = upperText,
                        initialSelection = TextRange(upperText.length),
                    ),
                )
            },
        )
    }


class SweepSectionColumnState(internal val sweepSectionStates: List<SweepSectionRowState>) {

    val sweepSections: Flow<List<SweepSection>>
        get() = combine(
            flows = sweepSectionStates.map { it.sweepSection },
            transform = { it.toList() }
        )

    operator fun get(index: Int): SweepSectionRowState = sweepSectionStates[index]

    object Saver : androidx.compose.runtime.saveable.Saver<SweepSectionColumnState, Any> {
        override fun restore(value: Any): SweepSectionColumnState? =
            SweepSectionColumnState(
                sweepSectionStates = (value as List<*>).map {
                    with(SweepSectionRowState.Saver) { restore(it as Any) } as SweepSectionRowState
                }
            )

        override fun SaverScope.save(value: SweepSectionColumnState): Any? =
            value.sweepSectionStates.map { states ->
                with(SweepSectionRowState.Saver) { save(states) }
            }
    }
}

fun SweepSectionColumnState(
    sweepSections: List<SweepSection>
): SweepSectionColumnState = SweepSectionColumnState(
    sweepSectionStates = sweepSections.map(::SweepSectionRowState)
)


@Composable
fun SweepSectionsColumn(
    sweepSectionsState: SweepSectionColumnState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = true,
    verticalSpacing: Dp = 4.dp,
) = Column(
    modifier = modifier
        .semantics(mergeDescendants = true) {},
    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.idx_cnt),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(55.dp),
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = stringResource(Res.string.lower),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1F),
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = stringResource(Res.string.upper),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1F),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }

    sweepSectionsState.sweepSectionStates.forEach { state ->
        SweepSectionRow(
            sweepSectionState = state,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
        )
    }
}

@Preview
@Composable
private fun SweepSectionColumnPreview() {
    Valentine1Theme(darkTheme = true) {
        SweepSectionsColumn(
            sweepSectionsState = rememberSweepSectionColumnState(
                sweepSections = previewSweepSections,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


internal val previewSweepSections: List<SweepSection> = listOf(
    SweepSection(
        indexCount = ((0x01.toByte() shl 4) or 0x02.toByte()),
        lowerEdge = 34000,
        upperEdge = 35500,
    ),
    SweepSection(
        indexCount = ((0x02.toByte() shl 4) or 0x02.toByte()),
        lowerEdge = 35500,
        upperEdge = 36100,
    ),
)