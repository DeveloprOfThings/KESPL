package io.github.developrofthings.helloV1.ui.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.custom_sweeps
import hellov1.composeapp.generated.resources.euro
import hellov1.composeapp.generated.resources.ic_article
import hellov1.composeapp.generated.resources.legacy
import hellov1.composeapp.generated.resources.searching_for_alerts
import hellov1.composeapp.generated.resources.soft
import hellov1.composeapp.generated.resources.time_slicing
import hellov1.composeapp.generated.resources.view_esp_log
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.helloV1.ui.component.ESPCommandPanel
import io.github.developrofthings.helloV1.ui.component.V1cAppBar
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
private fun AppBar(
    espService: IESPService = koinInject(),
    modifier: Modifier = Modifier,
    onScanClick: (V1cType) -> Unit,
    onConnectClick: (V1connection) -> Unit,
    onDisconnectClick: () -> Unit,
) {
    val connStatus by espService.connectionStatus.collectAsStateWithLifecycle()
    val device by espService.v1connection.collectAsStateWithLifecycle()
    V1cAppBar(
        connectionStatus = connStatus,
        v1c = device,
        modifier = modifier,
        onScanClick = onScanClick,
        onConnectClick = onConnectClick,
        onDisconnectClick = onDisconnectClick,
    )
}

@Composable
fun ControlsScreen(
    viewModel: ControlsViewModel = koinViewModel(),
    showESPLogButton: Boolean,
    onShowESPLog: () -> Unit,
    onScanClick: (V1cType) -> Unit,
    onMirrorDisplayClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog: ControlsDialogType by remember { mutableStateOf(ControlsDialogType.None) }
    Column(
        Modifier
            .fillMaxSize()
    ) {
        AppBar(
            onScanClick = onScanClick,
            onConnectClick = { viewModel.connect() },
            onDisconnectClick = { viewModel.disconnect() },
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row {
                InformationDisplay(
                    uiState = uiState.infDisplayState,
                    modifier = Modifier
                        .weight(1F),
                )

                if (showESPLogButton) {
                    IconButton(onClick = onShowESPLog) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_article),
                            contentDescription = stringResource(Res.string.view_esp_log),
                            modifier = Modifier
                                .size(24.dp),
                        )
                    }
                }
            }

            ESPCommandPanel(
                uiState = uiState,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                onSweepInfoClick = {
                    dialog = ControlsDialogType.SweepInfo
                },
                onMirrorDisplayClick = onMirrorDisplayClick,
                onUserBytesGuiClick = {
                    dialog = ControlsDialogType.UserBytes(
                        targetDevice = it,
                        userBytes = uiState.userBytes,
                    )
                },
                onVolumeControlsClick = {
                    dialog = ControlsDialogType.Volume
                },
                onRequestData = viewModel::onESPAction,
            )
        }
    }

    ControlsScreenDialog(
        dialogType = dialog,
        onDismissRequest = {
            dialog = ControlsDialogType.None
        },
    )
}

@Composable
private fun InformationDisplay(
    uiState: InfDisplayState,
    modifier: Modifier = Modifier,
) = FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    maxItemsInEachRow = 3,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    maxLines = 3
) {
    Text(
        text = stringResource(Res.string.legacy, uiState.isLegacy),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )

    Text(
        text = stringResource(Res.string.soft, uiState.isSoft),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )

    Text(
        text = stringResource(Res.string.euro, uiState.isEuro),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )

    Text(
        text = stringResource(Res.string.custom_sweeps, uiState.isCustomSweeps),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )

    Text(
        text = stringResource(Res.string.time_slicing, uiState.isTimeSlicing),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )

    Text(
        text = stringResource(Res.string.searching_for_alerts, uiState.isSearchingForAlerts),
        maxLines = 1,
        modifier = Modifier.weight(1F),
    )
}