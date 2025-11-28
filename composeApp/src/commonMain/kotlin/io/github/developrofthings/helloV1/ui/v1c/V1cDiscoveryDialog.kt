package io.github.developrofthings.helloV1.ui.v1c

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import io.github.developrofthings.helloV1.ui.component.V1cScanResultItem
import io.github.developrofthings.helloV1.ui.util.serializableType
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.discover_v1connections
import hellov1.composeapp.generated.resources.select_scan_type
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

@Serializable
data class V1cScannerDialog(
    val connectionType: V1cType = V1cType.LE,
)

fun NavController.showDialog(connectionType: V1cType) {
    navigate(V1cScannerDialog(connectionType = connectionType))
}

fun NavGraphBuilder.v1connectionDialogRoute(
    onDismiss: () -> Unit
) {
    dialog<V1cScannerDialog>(
        typeMap = mapOf(
            typeOf<V1cType>() to serializableType<V1cType>()
        )
    ) {
        V1cDiscoveryDialog(
            scanType = it.toRoute<V1cScannerDialog>().connectionType,
            onDismissRequest = onDismiss,
        )
    }
}

@Composable
fun V1cDiscoveryDialog(
    scanType: V1cType,
    viewModel: V1cDiscoveryViewModel = koinViewModel<V1cDiscoveryViewModel>() {
        parametersOf(scanType)
    },
    onDismissRequest: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    DialogContent(
        uiState = uiState,
        modifier = Modifier
            .widthIn(max = 500.dp)
            .height(650.dp),
        onScanTypeSelected = viewModel::setScanType,
        onV1cSelected = {
            onDismissRequest()
            viewModel.onV1cSelected(it)
        },
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogContent(
    uiState: V1cDiscoveryUiState,
    modifier: Modifier = Modifier,
    onScanTypeSelected: (V1cType) -> Unit,
    onV1cSelected: (V1connection) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        contentColor = AlertDialogDefaults.titleContentColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DialogHeader(
                selectedScanType = uiState.activeScanType,
                availableScanTypes = uiState.availableScanTypes,
                modifier = Modifier.fillMaxWidth(),
                onScanTypeSelected = onScanTypeSelected,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(
                    items = uiState.devices,
                    key = { _, it -> it.id },
                ) { i, it ->
                    V1cScanResultItem(
                        colors = colorPairs.getModulo(i),
                        result = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onV1cSelected(it.device) },
                    )
                }
            }
        }
    }
}


@Composable
private fun DialogHeader(
    selectedScanType: V1cType,
    availableScanTypes: List<V1cType>,
    modifier: Modifier = Modifier,
    onScanTypeSelected: (V1cType) -> Unit,
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    Text(
        text = stringResource(Res.string.discover_v1connections),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    )

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 24.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            availableScanTypes.forEach {
                V1ConnectionTypeRadioOption(
                    isSelected = it == selectedScanType,
                    type = it,
                    typeSelected = onScanTypeSelected
                )
            }
        }
    }
}

private fun <T> List<T>.getModulo(index: Int): T = get(index % size)

@Composable
private fun V1ConnectionTypeRadioOption(
    isSelected: Boolean,
    type: V1cType,
    modifier: Modifier = Modifier,
    typeSelected: (V1cType) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { typeSelected(type) },
            onClickLabel = stringResource(Res.string.select_scan_type, type.name),
            role = Role.RadioButton,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { typeSelected(type) },
            interactionSource = interactionSource,
        )
        Text(text = type.name)
    }
}

/**
 * Background/Foreground color pairs that will make sure text is always visible against variable
 * color backgrounds.
 */
internal val colorPairs = listOf(
    Color(0XFF8ECAE6) to Color.Black,
    Color(0XFF023047) to Color.White,
    Color(0XFF219ebc) to Color.White,
    Color(0XFFfb8500) to Color.Black,
    Color(0XFFe5989b) to Color.Black,
    Color(0XFFb5838d) to Color.Black,
    Color(0XFF6d6875) to Color.White,
    Color(0XFF343a40) to Color.White,
    Color(0XFFffdab9) to Color.Black,
    Color(0XFFeaf2d7) to Color.Black,
    Color(0XFFb9fbc0) to Color.Black,
    /*Color(0XFFdc2f02) to Color.Black,*/
    Color(0XFFff758f) to Color.Black,
).shuffled()