import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.developrofthings.helloV1.ui.component.LabelButton
import io.github.developrofthings.helloV1.ui.component.SweepDefinitionsColumn
import io.github.developrofthings.helloV1.ui.component.SweepSectionsColumn
import io.github.developrofthings.helloV1.ui.dialog.sweep.SweepInfoPresenter
import io.github.developrofthings.helloV1.ui.dialog.sweep.SweepInfoUiState
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.current_sweep_definitions
import hellov1.composeapp.generated.resources.default_sweep_definitions
import hellov1.composeapp.generated.resources.read
import hellov1.composeapp.generated.resources.read_default_sweeps
import hellov1.composeapp.generated.resources.read_sweep_sections
import hellov1.composeapp.generated.resources.restore_sweeps
import hellov1.composeapp.generated.resources.sweep_data
import hellov1.composeapp.generated.resources.sweep_definitions
import hellov1.composeapp.generated.resources.sweep_sections
import hellov1.composeapp.generated.resources.write
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun SweepInfoDialog(
    onDismissRequest: () -> Unit,
) = Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
        usePlatformDefaultWidth = false,
    )
) {
    Surface(
        modifier = Modifier.widthIn(max = 550.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        val presenter: SweepInfoPresenter = koinInject { parametersOf(coroutineScope) }
        val uiState by presenter.uiState.collectAsStateWithLifecycle()

        SweepInfoDialogContent(
            uiState = uiState,
            onReadSweepSectionsClicked = presenter::onReadSweepSectionsClicked,
            onReadMaxSweepIndexClicked = presenter::onReadMaxSweepIndexClicked,
            onReadAllSweepsClicked = presenter::onReadAllSweepsClicked,
            onReadDefaultSweepsClicked = presenter::onReadDefaultSweepsClicked,
            onRestoreDefaultSweepsClicked = presenter::onRestoreDefaultSweepsClicked,
            onReadSweepData = presenter::onReadSweepData,
            onWriteSweepsClicked = presenter::onWriteSweepsClicked,
        )
    }
}

@Composable
private fun SweepInfoDialogContent(
    uiState: SweepInfoUiState,
    modifier: Modifier = Modifier,
    onReadSweepSectionsClicked: () -> Unit,
    onReadMaxSweepIndexClicked: () -> Unit,
    onReadAllSweepsClicked: () -> Unit,
    onReadDefaultSweepsClicked: () -> Unit,
    onRestoreDefaultSweepsClicked: () -> Unit,
    onReadSweepData: () -> Unit,
    onWriteSweepsClicked: (List<SweepDefinition>) -> Unit,
) = Column(
    modifier = modifier
        .padding(all = 16.dp)
        .verticalScroll(state = rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = stringResource(Res.string.sweep_sections),
        style = MaterialTheme.typography.titleMedium,
    )

    SweepSectionsColumn(
        sweepSectionsState = uiState.sweepSectionsState,
        modifier = Modifier.fillMaxWidth(),
    )

    LabelButton(
        label = stringResource(Res.string.read_sweep_sections),
        onClick = onReadSweepSectionsClicked,
        modifier = Modifier.fillMaxWidth(),
    )

    Text(
        text = stringResource(Res.string.sweep_definitions),
        style = MaterialTheme.typography.titleMedium,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val textFieldState = rememberTextFieldState(uiState.maxSweepIndex.toString())
        LaunchedEffect(uiState.maxSweepIndex) {
            textFieldState.setTextAndPlaceCursorAtEnd(uiState.maxSweepIndex.toString())
        }

        LabelButton(
            label = "Max Sweep Index",
            onClick = onReadMaxSweepIndexClicked,
            modifier = Modifier.weight(1F),
        )

        IntField(
            textFieldState = textFieldState,
            enabled = true,
            readOnly = true,
            modifier = Modifier.width(35.dp),
        )
    }

    Text(
        text = stringResource(Res.string.current_sweep_definitions),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.align(Alignment.Start),
    )

    SweepDefinitionsColumn(
        sweepDefinitionsState = uiState.currentSweepsState,
        modifier = Modifier.fillMaxWidth(),
    )

    FlowRow(
        modifier = Modifier.padding(horizontal = 8.dp),
        maxLines = 4,
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabelButton(
            label = stringResource(Res.string.read),
            onClick = onReadAllSweepsClicked,
            modifier = Modifier.weight(1F),
        )

        LabelButton(
            label = stringResource(Res.string.write),
            onClick = { onWriteSweepsClicked(uiState.currentSweepsState.allSweeps) },
            modifier = Modifier.weight(1F),
        )

        LabelButton(
            label = stringResource(Res.string.sweep_data),
            onClick = onReadSweepData,
            modifier = Modifier.weight(1F),
        )

        AnimatedVisibility(visible = uiState.defaultSweepsSupported) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.default_sweep_definitions),
                    style = MaterialTheme.typography.titleSmall,
                )

                SweepDefinitionsColumn(
                    sweepDefinitionsState = uiState.defaultSweepsState,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                LabelButton(
                    label = stringResource(Res.string.read_default_sweeps),
                    onClick = onReadDefaultSweepsClicked,
                    modifier = Modifier.fillMaxWidth(),
                )

                LabelButton(
                    label = stringResource(Res.string.restore_sweeps),
                    onClick = onRestoreDefaultSweepsClicked,
                    enabled = uiState.defaultSweepsSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SweepDefRow(
    sweep: SweepDefinition,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier
        .padding(
            horizontal = 16.dp,
            vertical = 4.dp,
        ),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = "${sweep.index}).",
        maxLines = 1,
        modifier = Modifier.width(20.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(Modifier.width(16.dp))

    val lowerTextFieldState = rememberTextFieldState(sweep.lowerEdge.toString())

    Row(
        modifier = Modifier.weight(1F),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        IntField(
            textFieldState = lowerTextFieldState,
            modifier = Modifier
                .width(100.dp),
            style = MaterialTheme.typography.bodyLarge,
            textPadding = PaddingValues(
                horizontal = 2.dp,
                vertical = 8.dp,
            ),
            outputTransformation = { insert(this.length, " Mhz") },
        )

        val upperTextFieldState = rememberTextFieldState(sweep.upperEdge.toString())
        IntField(
            textFieldState = upperTextFieldState,
            modifier = Modifier
                .width(100.dp),
            style = MaterialTheme.typography.bodyLarge,
            textPadding = PaddingValues(
                horizontal = 2.dp,
                vertical = 8.dp,
            ),
            outputTransformation = { insert(this.length, " Mhz") },
        )
    }
}

@Preview
@Composable
private fun SweepDefRowPreview() {
    Valentine1Theme(darkTheme = true) {
        SweepDefRow(
            sweep = SweepDefinition(
                index = 1,
                lowerEdge = 34545,
                upperEdge = 35100,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun CustomSweepPopupContentPreview() {
    Valentine1Theme(darkTheme = true) {
        Surface(
            modifier = Modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            SweepInfoDialogContent(
                uiState = SweepInfoUiState.DEFAULT,
                onReadSweepSectionsClicked = {},
                onReadMaxSweepIndexClicked = {},
                onReadAllSweepsClicked = {},
                onReadDefaultSweepsClicked = {},
                onRestoreDefaultSweepsClicked = {},
                onReadSweepData = {},
                onWriteSweepsClicked = {},
            )
        }
    }
}