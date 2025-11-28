@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.developrofthings.helloV1.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ESPDataRequest
import io.github.developrofthings.helloV1.ui.controls.ControlsUiState
import io.github.developrofthings.helloV1.ui.controls.VolumeUiState
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.at_off
import hellov1.composeapp.generated.resources.at_on
import hellov1.composeapp.generated.resources.battery_voltage
import hellov1.composeapp.generated.resources.display_mirror
import hellov1.composeapp.generated.resources.display_off
import hellov1.composeapp.generated.resources.display_on
import hellov1.composeapp.generated.resources.mute_v1
import hellov1.composeapp.generated.resources.sweep_info
import hellov1.composeapp.generated.resources.unmute_v1
import hellov1.composeapp.generated.resources.v1_logic_modes
import hellov1.composeapp.generated.resources.valentine_1
import hellov1.composeapp.generated.resources.volume_controls
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ESPCommandPanel(
    uiState: ControlsUiState,
    modifier: Modifier = Modifier,
    onSweepInfoClick: () -> Unit,
    onVolumeControlsClick: () -> Unit,
    onMirrorDisplayClick: () -> Unit,
    onUserBytesGuiClick: (ESPDevice) -> Unit,
    onRequestData: (ESPDataRequest) -> Unit,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    GeneralButtonPanel(
        availableDevices = uiState.targets,
        userBytes = uiState.userBytes,
        onRequestData = onRequestData,
        onUserBytesGuiClicked = onUserBytesGuiClick,
        enabled = uiState.isConnected,
    )

    V1CommandPanel(
        volumeUiState = uiState.volumeState,
        buttonsEnabled = uiState.isConnected,
        onSweepInfoClick = onSweepInfoClick,
        onMirrorDisplayClick = onMirrorDisplayClick,
        onVolumeControlsClick = onVolumeControlsClick,
        onRequestData = onRequestData,
    )

    SAVVYPanel(
        onEnableSAVVYUnmutingClicked = {
            onRequestData(ESPDataRequest.SAVVY.Unmute(enableUnmuting = true))
        },
        onDisableSAVVYUnmutingClicked = {
            onRequestData(ESPDataRequest.SAVVY.Unmute(enableUnmuting = false))
        },
        onReadSAVVYStatusClicked = {
            onRequestData(ESPDataRequest.SAVVY.SAVVYStatus)
        },
        onOverrideThumbwheelClicked = {
            onRequestData(
                ESPDataRequest.SAVVY.OverrideThumbwheel(
                    speed = it
                )
            )
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.isConnected,
    )
}

@Composable
fun V1CommandPanel(
    volumeUiState: VolumeUiState,
    modifier: Modifier = Modifier,
    buttonsEnabled: Boolean = true,
    onSweepInfoClick: () -> Unit,
    onVolumeControlsClick: () -> Unit,
    onMirrorDisplayClick: () -> Unit,
    onRequestData: (ESPDataRequest) -> Unit,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = stringResource(Res.string.valentine_1),
        style = MaterialTheme.typography.titleMedium,
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            Alignment.CenterHorizontally,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
    ) {
        LabelButton(
            label = stringResource(Res.string.sweep_info),
            onClick = onSweepInfoClick,
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.display_mirror),
            onClick = onMirrorDisplayClick,
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.display_on),
            onClick = { onRequestData(ESPDataRequest.V1.DisplayOn(true)) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.display_off),
            onClick = { onRequestData(ESPDataRequest.V1.DisplayOn(false)) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.mute_v1),
            onClick = { onRequestData(ESPDataRequest.V1.Mute(true)) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.unmute_v1),
            onClick = { onRequestData(ESPDataRequest.V1.Mute(false)) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        V1Mode(
            enabled = buttonsEnabled,
            onWriteModeClicked = { onRequestData(ESPDataRequest.V1.ChangeMode(it)) }
        )

        LabelButton(
            label = stringResource(Res.string.at_on),
            onClick = { onRequestData(ESPDataRequest.V1.AlertTable(on = true)) },
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.at_off),
            onClick = { onRequestData(ESPDataRequest.V1.AlertTable(on = false)) },
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.battery_voltage),
            onClick = { onRequestData(ESPDataRequest.V1.BatteryVoltage) },
            enabled = buttonsEnabled,
        )

        AnimatedVisibility(visible = volumeUiState.canControlVolume) {
            LabelButton(
                label = stringResource(Res.string.volume_controls),
                onClick = onVolumeControlsClick,
                enabled = buttonsEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ESPCommandPanelPreview() {
    Valentine1Theme(darkTheme = true) {
        Surface {
            ESPCommandPanel(
                uiState = ControlsUiState.DEFAULT.copy(
                    connectionStatus = ESPConnectionStatus.Connected
                ),
                modifier = Modifier.fillMaxSize(),
                onMirrorDisplayClick = {},
                onSweepInfoClick = {},
                onUserBytesGuiClick = {},
                onVolumeControlsClick = {},
                onRequestData = {},
            )
        }
    }
}

@Preview
@Composable
private fun V1CommandPanelPreview() {
    Valentine1Theme(darkTheme = true) {
        Surface {
            V1CommandPanel(
                volumeUiState = VolumeUiState.DEFAULT,
                onMirrorDisplayClick = {},
                onSweepInfoClick = {},
                onVolumeControlsClick = {},
                onRequestData = {},
            )
        }
    }
}

@Composable
fun V1Mode.label(): String = stringArrayResource(
    resource = Res.array.v1_logic_modes
)[
    when (this@label) {
        V1Mode.AllBogeysKKa -> 0
        V1Mode.LogicKa -> 1
        V1Mode.AdvancedLogic -> 2
        V1Mode.Invalid -> 3
    }
]