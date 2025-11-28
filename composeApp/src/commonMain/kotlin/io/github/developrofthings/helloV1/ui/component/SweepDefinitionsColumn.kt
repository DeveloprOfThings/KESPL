package io.github.developrofthings.helloV1.ui.component

import SweepDefinitionRow
import SweepDefinitionRowState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.index
import hellov1.composeapp.generated.resources.lower
import hellov1.composeapp.generated.resources.upper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun rememberSweepDefinitionColumnState(
    sweepDefinitions: List<SweepDefinition> = emptyList(),
): SweepDefinitionsColumnState =
    rememberSaveable(
        sweepDefinitions,
        saver = SweepDefinitionsColumnState.Saver
    ) { SweepDefinitionsColumnState(sweepDefinitions = sweepDefinitions) }

class SweepDefinitionsColumnState(
    internal val sweepDefinitionStates: List<SweepDefinitionRowState>,
) {
    val sweeps: Flow<List<SweepDefinition>>
        get() = combine(
            flows = sweepDefinitionStates.map { it.sweep },
            transform = { it.toList() }
        )

    operator fun get(index: Int): SweepDefinitionRowState = sweepDefinitionStates[index]

    val allSweeps: List<SweepDefinition>
        get() = sweepDefinitionStates.map {
            SweepDefinition(
                index = it.sweepIndex,
                lowerEdge = it.lowerEdge,
                upperEdge = it.upperEdge,
            )
        }

    object Saver : androidx.compose.runtime.saveable.Saver<SweepDefinitionsColumnState, Any> {
        override fun restore(value: Any): SweepDefinitionsColumnState? =
            SweepDefinitionsColumnState(
                sweepDefinitionStates = (value as List<*>).map {
                    with(SweepDefinitionRowState.Saver) {
                        restore(
                            it as Any
                        )
                    } as SweepDefinitionRowState
                }
            )

        override fun SaverScope.save(value: SweepDefinitionsColumnState): Any? =
            value.sweepDefinitionStates.map { states ->
                with(SweepDefinitionRowState.Saver) { save(states) }
            }
    }
}

fun SweepDefinitionsColumnState(
    sweepDefinitions: List<SweepDefinition>
): SweepDefinitionsColumnState = SweepDefinitionsColumnState(
    sweepDefinitionStates = sweepDefinitions.map(::SweepDefinitionRowState)
)

@Composable
fun SweepDefinitionsColumn(
    sweepDefinitionsState: SweepDefinitionsColumnState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
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
                text = stringResource(Res.string.index),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp),
            )

            Text(
                text = stringResource(Res.string.lower),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1F),
            )

            Text(
                text = stringResource(Res.string.upper),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1F),
            )
        }
    }

    sweepDefinitionsState.sweepDefinitionStates.forEach { state ->
        SweepDefinitionRow(
            sweepDefState = state,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
        )
    }
}

@Preview
@Composable
private fun SweepDefinitionsColumnPreview() {
    Valentine1Theme(darkTheme = true) {
        SweepDefinitionsColumn(
            sweepDefinitionsState = rememberSweepDefinitionColumnState(
                sweepDefinitions = previewSweepDefinitions,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal val previewSweepDefinitions: List<SweepDefinition> = listOf(
    SweepDefinition(
        index = 0,
        lowerEdge = 34545,
        upperEdge = 35100,
    ),
    SweepDefinition(
        index = 1,
        lowerEdge = 35110,
        upperEdge = 35220,
    ),
    SweepDefinition(
        index = 2,
        lowerEdge = 35235,
        upperEdge = 35290,
    ),
    SweepDefinition(
        index = 3,
        lowerEdge = 35550,
        upperEdge = 35660,
    ),
    SweepDefinition(
        index = 4,
        lowerEdge = 35698,
        upperEdge = 35725,
    ),
    SweepDefinition(
        index = 5,
        lowerEdge = 35705,
        upperEdge = 35905,
    ),
)