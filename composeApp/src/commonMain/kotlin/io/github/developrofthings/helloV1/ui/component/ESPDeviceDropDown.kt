@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.developrofthings.helloV1.ui.component

import DropDownTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.developrofthings.helloV1.TargetESPDevice

@Composable
fun ESPDeviceDropDown(
    selectedDevice: TargetESPDevice,
    devices: List<TargetESPDevice>,
    onDeviceSelected: (TargetESPDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDeviceText = selectedDevice.label()
    val textFieldState = rememberTextFieldState(initialText = selectedDeviceText)
    LaunchedEffect(selectedDevice, devices) {
        textFieldState.edit {
            replace(0, length, selectedDeviceText)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        DropDownTextField(
            modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            textFieldState = textFieldState,
            readOnly = true,
            expanded = expanded,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            devices.forEach { device ->
                val deviceLabel = device.label()
                DropdownMenuItem(
                    text = { Text(deviceLabel, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onDeviceSelected(device)
                        textFieldState.edit { replace(0, length, selectedDeviceText) }
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}