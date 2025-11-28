package io.github.developrofthings.helloV1.ui.controls

import SweepInfoDialog
import androidx.compose.runtime.Composable
import io.github.developrofthings.helloV1.ui.dialog.user.UserBytesDialog
import io.github.developrofthings.helloV1.ui.dialog.volume.VolumeDialog
import io.github.developrofthings.kespl.ESPDevice

@Composable
internal fun ControlsScreenDialog(
    dialogType: ControlsDialogType,
    onDismissRequest: () -> Unit,
) {
    when (dialogType) {
        ControlsDialogType.None -> { /* NO OP*/
        }

        ControlsDialogType.SweepInfo -> {
            SweepInfoDialog(onDismissRequest = onDismissRequest)
        }

        is ControlsDialogType.UserBytes -> {
            UserBytesDialog(
                targetDevice = dialogType.targetDevice,
                userBytes = dialogType.userBytes,
                onDismissRequest = onDismissRequest,
            )
        }

        ControlsDialogType.Volume -> {
            VolumeDialog(onDismissRequest = onDismissRequest)
        }
    }
}

sealed interface ControlsDialogType {

    data object None : ControlsDialogType

    data object SweepInfo : ControlsDialogType

    data object Volume : ControlsDialogType

    data class UserBytes(
        val targetDevice: ESPDevice,
        val userBytes: ByteArray,
    ) : ControlsDialogType {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as UserBytes

            if (!userBytes.contentEquals(other.userBytes)) return false
            if (targetDevice != other.targetDevice) return false

            return true
        }

        override fun hashCode(): Int {
            var result = userBytes.contentHashCode()
            result = 31 * result + targetDevice.hashCode()
            return result
        }
    }
}