package io.github.developrofthings.helloV1.ui.dialog.volume

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.developrofthings.helloV1.ui.component.CheckableText
import io.github.developrofthings.helloV1.ui.component.LabelButton
import io.github.developrofthings.helloV1.ui.component.VolumeSlider
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.abort_audio_delay
import hellov1.composeapp.generated.resources.all_volumes
import hellov1.composeapp.generated.resources.current_volume
import hellov1.composeapp.generated.resources.display_current_volume
import hellov1.composeapp.generated.resources.live_update_v1_volume
import hellov1.composeapp.generated.resources.main_volume
import hellov1.composeapp.generated.resources.mute_volume
import hellov1.composeapp.generated.resources.read_volume
import hellov1.composeapp.generated.resources.save_volume
import hellov1.composeapp.generated.resources.saved_volume
import hellov1.composeapp.generated.resources.skip_feedback_if_no_volume_change
import hellov1.composeapp.generated.resources.user_feedback
import hellov1.composeapp.generated.resources.write_volume
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun VolumeDialog(onDismissRequest: () -> Unit) = Dialog(
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
        val presenter: VolumePresenter = koinInject { parametersOf(coroutineScope) }
        val uiState by presenter.uiState.collectAsStateWithLifecycle()
        VolumeDialogContent(
            uiState = uiState,
            onMainVolumeChange = presenter::updateMainVolume,
            onMuteVolumeChange = presenter::updateMuteVolume,
            onReadVolumeClick = presenter::onReadVolumeClicked,
            onLiveUpdateVolumeChange = presenter::onLiveUpdateVolumeChanged,
            onProvideUserFeedbackChange = presenter::onProvideUserFeedbackChanged,
            onSkipFeedbackWhenIdenticalChange = presenter::onSkipFeedbackWhenIdenticalChanged,
            onSaveVolumeChange = presenter::onSaveVolumeChanged,
            onReadAllVolumeClick = presenter::onReadAllVolumeClicked,
            onDisplayCurrentVolumeClick = presenter::onDisplayCurrentVolumeClicked,
            onAbortAudioDelayClick = presenter::onAbortDelayClicked,
            onWriteVolumeClick = presenter::onWriteVolumeClicked,
        )
    }
}

@Composable
private fun VolumeDialogContent(
    uiState: VolumeUiState,
    onMainVolumeChange: (Float) -> Unit,
    onMuteVolumeChange: (Float) -> Unit,
    onLiveUpdateVolumeChange: (Boolean) -> Unit,
    onProvideUserFeedbackChange: (Boolean) -> Unit,
    onSkipFeedbackWhenIdenticalChange: (Boolean) -> Unit,
    onSaveVolumeChange: (Boolean) -> Unit,
    onReadVolumeClick: () -> Unit,
    onReadAllVolumeClick: () -> Unit,
    onDisplayCurrentVolumeClick: () -> Unit,
    onAbortAudioDelayClick: () -> Unit,
    onWriteVolumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier
        .padding(all = 16.dp)
        .verticalScroll(state = rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    VolumeSlider(
        label = stringResource(Res.string.main_volume),
        level = uiState.currentMainVolume,
        onValueChange = onMainVolumeChange,
        modifier = Modifier.fillMaxWidth(),
    )

    VolumeSlider(
        label = stringResource(Res.string.mute_volume),
        level = uiState.currentMuteVolume,
        onValueChange = onMuteVolumeChange,
        modifier = Modifier.fillMaxWidth(),
    )

    CheckableText(
        text = stringResource(Res.string.live_update_v1_volume),
        checked = uiState.shouldLiveUpdateVolume,
        onCheckedChange = onLiveUpdateVolumeChange,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
    )


    CheckableText(
        text = stringResource(Res.string.user_feedback),
        checked = uiState.shouldProvideUserFeedback,
        onCheckedChange = onProvideUserFeedbackChange,
        modifier = Modifier.align(Alignment.Start),
    )


    CheckableText(
        text = stringResource(Res.string.skip_feedback_if_no_volume_change),
        checked = uiState.shouldSkipFeedbackWhenIdentical,
        onCheckedChange = onSkipFeedbackWhenIdenticalChange,
        modifier = Modifier.align(Alignment.Start),
    )


    CheckableText(
        text = stringResource(Res.string.save_volume),
        checked = uiState.shouldSaveVolume,
        onCheckedChange = onSaveVolumeChange,
        modifier = Modifier.align(Alignment.Start),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabelButton(
            label = stringResource(Res.string.read_volume),
            onClick = onReadVolumeClick,
            modifier = Modifier.weight(1F),
        )

        LabelButton(
            label = stringResource(Res.string.write_volume),
            onClick = onWriteVolumeClick,
            modifier = Modifier.weight(1F),
        )
    }

    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.supportsAllVolume) {
            LabelButton(
                label = stringResource(Res.string.all_volumes),
                onClick = onReadAllVolumeClick,
                modifier = Modifier.weight(1F),
            )
        }

        if (uiState.supportsDisplayVolume) {
            LabelButton(
                label = stringResource(Res.string.display_current_volume),
                onClick = onDisplayCurrentVolumeClick,
                modifier = Modifier.weight(1F),
            )
        }

        if (uiState.supportsAbortVolumeDelay) {
            LabelButton(
                label = stringResource(Res.string.abort_audio_delay),
                onClick = onAbortAudioDelayClick,
                modifier = Modifier.weight(1F),
            )
        }
    }

    VolumeSection(
        section = stringResource(Res.string.current_volume),
        main = uiState.currentMainVolume,
        mute = uiState.currentMuteVolume,
        modifier = Modifier.align(Alignment.Start),
    )

    if (uiState.supportsAllVolume) {
        VolumeSection(
            section = stringResource(Res.string.saved_volume),
            main = uiState.savedMainVolume,
            mute = uiState.savedMuteVolume,
            modifier = Modifier.align(Alignment.Start),
        )
    }
}

@Composable
private fun VolumeSection(
    section: String,
    main: Float,
    mute: Float,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    Text(text = section, style = MaterialTheme.typography.titleMedium)
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.main_volume))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" $main")
                }
            },
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.mute_volume))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" $mute")
                }
            },
            style = MaterialTheme.typography.titleSmall
        )
    }
}