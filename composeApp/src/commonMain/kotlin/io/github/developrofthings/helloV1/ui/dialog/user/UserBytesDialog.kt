package io.github.developrofthings.helloV1.ui.dialog.user

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.developrofthings.helloV1.ui.component.UserBytesPager
import io.github.developrofthings.helloV1.ui.component.rememberUserBytesPagerState
import io.github.developrofthings.kespl.ESPDevice
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun UserBytesDialog(
    targetDevice: ESPDevice,
    userBytes: ByteArray,
    onDismissRequest: () -> Unit,
) = Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
        usePlatformDefaultWidth = false,
    )
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val presenter: UserBytesGuiPresenter = koinInject {
        parametersOf(
            targetDevice,
            userBytes,
            coroutineScope,
        )
    }
    val uiState by presenter.uiState.collectAsStateWithLifecycle()

    UserBytesPager(
        isGen2 = uiState.isGen2,
        userBytesState = rememberUserBytesPagerState(userBytes = uiState.userBytes),
        modifier = Modifier.widthIn(max = 550.dp),
        onCommitClicked = presenter::onWriteUserBytes,
    )
}