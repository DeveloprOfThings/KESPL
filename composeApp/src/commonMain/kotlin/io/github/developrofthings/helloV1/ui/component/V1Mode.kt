@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.developrofthings.helloV1.ui.component

import DropDownTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.displayData.modes
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.mode
import org.jetbrains.compose.resources.stringResource

@Composable
fun V1Mode(
    modifier: Modifier = Modifier,
    selectedMode: V1Mode = V1Mode.AllBogeysKKa,
    enabled: Boolean = true,
    onWriteModeClicked: (V1Mode) -> Unit,
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    var selectedMode by remember(selectedMode) { mutableStateOf<V1Mode>(selectedMode) }
    V1ModeDropDown(
        selectedMode = selectedMode,
        modifier = Modifier.width(225.dp),
        onModeSelected = { selectedMode = it },
    )

    Button(
        onClick = { onWriteModeClicked(selectedMode) },
        enabled = enabled,
    ) {
        Text(
            text = stringResource(Res.string.mode),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun V1ModeDropDown(
    selectedMode: V1Mode,
    modifier: Modifier = Modifier,
    onModeSelected: (V1Mode) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(initialText = selectedMode.label())
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        DropDownTextField(
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            textFieldState = textFieldState,
            readOnly = true,
            expanded = expanded,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            modes.forEach { mode ->
                val modeLabel = mode.label()
                DropdownMenuItem(
                    text = { Text(modeLabel, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onModeSelected(mode)
                        textFieldState.setTextAndPlaceCursorAtEnd(modeLabel)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}