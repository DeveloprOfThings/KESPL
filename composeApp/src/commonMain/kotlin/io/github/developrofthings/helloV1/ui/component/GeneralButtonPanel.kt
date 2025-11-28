package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ESPDataRequest
import io.github.developrofthings.helloV1.TargetESPDevice
import io.github.developrofthings.helloV1.resolveESPDevice
import io.github.developrofthings.helloV1.ui.isHex
import io.github.developrofthings.kespl.ESPDevice
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.general
import hellov1.composeapp.generated.resources.restore_defaults
import hellov1.composeapp.generated.resources.serial
import hellov1.composeapp.generated.resources.version
import org.jetbrains.compose.resources.stringResource

@Composable
fun GeneralButtonPanel(
    availableDevices: List<TargetESPDevice>,
    userBytes: ByteArray,
    onRequestData: (ESPDataRequest) -> Unit,
    onUserBytesGuiClicked: (ESPDevice) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = stringResource(Res.string.general),
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
        var selectedDevice by remember(availableDevices) {
            // Auto select the first V1 device or grab the next available
            val device = availableDevices.firstOrNull { it is TargetESPDevice.ValentineOne }
                ?: availableDevices.first()
            mutableStateOf(device)
        }

        val textFieldState = rememberTextFieldState(initialText = "FF")
        val resolveSelectedDevice = remember(key1 = selectedDevice, key2 = textFieldState) {
            {
                selectedDevice.resolveESPDevice(
                    customId = textFieldState.text.toString()
                )
            }
        }

        val customTxtFieldEnabled = selectedDevice is TargetESPDevice.Custom
        val buttonsEnabled =
            if (enabled) {
                /*
                ___________________________________
                |   A   |   B   |   !(A & !(B))   |
                ___________________________________
                |   T   |   T   |        T        |
                |   T   |   F   |        F        |
                |   F   |   T   |        T        |
                |   F   |   F   |        T        |
                ___________________________________

             */
                !(customTxtFieldEnabled && !textFieldState.text.isNotEmpty())
            } else false

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ESPDeviceDropDown(
                selectedDevice = selectedDevice,
                devices = availableDevices,
                modifier = Modifier.width(225.dp),
                onDeviceSelected = { selectedDevice = it },
            )

            val isTextInvalidValid =
                textFieldState.text.isNotEmpty() && !textFieldState.text.isHex()

            HexField(
                textFieldState = textFieldState,
                modifier = Modifier.width(80.dp),
                isError = isTextInvalidValid,
                enabled = customTxtFieldEnabled,
            )
        }

        LabelButton(
            label = stringResource(Res.string.version),
            onClick = {
                onRequestData(
                    ESPDataRequest.Version(
                        targetDevice = selectedDevice.resolveESPDevice(
                            customId = textFieldState.text.toString()
                        )
                    )
                )
            },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        LabelButton(
            label = stringResource(Res.string.serial),
            onClick = { onRequestData(ESPDataRequest.Serial(resolveSelectedDevice())) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )

        UserBytesRow(
            modifier = Modifier.fillMaxWidth(),
            userBytesState = rememberUserBytesState(userBytes = userBytes),
            enabled = buttonsEnabled,
            onReadClick = { onRequestData(ESPDataRequest.ReadUserBytes(resolveSelectedDevice())) },
            onWriteClick = {
                onRequestData(
                    ESPDataRequest.WriteUserBytes(
                        targetDevice = resolveSelectedDevice(),
                        userBytes = it,
                    )
                )
            },
            onGuiClick = { onUserBytesGuiClicked(resolveSelectedDevice()) },
        )

        LabelButton(
            label = stringResource(Res.string.restore_defaults),
            onClick = { onRequestData(ESPDataRequest.RestoreDefaults(resolveSelectedDevice())) },
            modifier = Modifier.weight(.5f),
            enabled = buttonsEnabled,
        )
    }
}