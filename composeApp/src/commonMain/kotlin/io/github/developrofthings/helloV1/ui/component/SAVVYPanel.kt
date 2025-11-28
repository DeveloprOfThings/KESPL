package io.github.developrofthings.helloV1.ui.component

import IntField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.developrofthings.helloV1.ui.intValueSafe
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.helloV1.ui.toKPH
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.disable_unmute
import hellov1.composeapp.generated.resources.enable_unmute
import hellov1.composeapp.generated.resources.kph
import hellov1.composeapp.generated.resources.mph
import hellov1.composeapp.generated.resources.override_thumbwheel
import hellov1.composeapp.generated.resources.request_savvy_status
import hellov1.composeapp.generated.resources.savvy
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SAVVYPanel(
    onEnableSAVVYUnmutingClicked: () -> Unit,
    onDisableSAVVYUnmutingClicked: () -> Unit,
    onReadSAVVYStatusClicked: () -> Unit,
    onOverrideThumbwheelClicked: (speed: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = ConstraintLayout(modifier = modifier) {
    val (
        titleTxt,
        overrideBtn,
        overrideTxt,
        speedRadioGrp,
        savvyStatusBtn,
        enableUnmuteBtn,
        disableUnmuteBtn,
    ) = createRefs()

    Text(
        text = stringResource(Res.string.savvy),
        modifier = Modifier.constrainAs(titleTxt) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            end.linkTo(parent.end)
        },
        style = MaterialTheme.typography.titleMedium,
    )

    val textFieldState = rememberTextFieldState(initialText = "0")
    var isKPH by remember { mutableStateOf(false) }
    LabelButton(
        label = stringResource(Res.string.override_thumbwheel),
        onClick = {
            onOverrideThumbwheelClicked(
                textFieldState.text.intValueSafe().let { if (isKPH) it else it.toKPH() }
            )
        },
        modifier = Modifier
            .constrainAs(overrideBtn) {
                top.linkTo(titleTxt.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(overrideTxt.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
        enabled = enabled,
    )

    IntField(
        textFieldState = textFieldState,
        modifier = Modifier
            .width(55.dp)
            .constrainAs(overrideTxt) {
                top.linkTo(overrideBtn.top)
                bottom.linkTo(overrideBtn.bottom)
                end.linkTo(speedRadioGrp.start, margin = 8.dp)
            },
        maxLength = 3,
        enabled = enabled,
    )

    SpeedSelector(
        isKPH = isKPH,
        onKPHSelected = {},
        onMPHSelected = {},
        modifier = Modifier.constrainAs(speedRadioGrp) {
            top.linkTo(overrideBtn.top)
            end.linkTo(parent.end)
        }
    )

    LabelButton(
        label = stringResource(Res.string.request_savvy_status),
        onClick = onReadSAVVYStatusClicked,
        modifier = Modifier
            .constrainAs(savvyStatusBtn) {
                top.linkTo(overrideBtn.bottom)
                start.linkTo(parent.start)
                end.linkTo(speedRadioGrp.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            },
        enabled = enabled,
    )

    LabelButton(
        label = stringResource(Res.string.enable_unmute),
        onClick = onEnableSAVVYUnmutingClicked,
        modifier = Modifier
            .constrainAs(enableUnmuteBtn) {
                top.linkTo(savvyStatusBtn.bottom)
                start.linkTo(parent.start)
                end.linkTo(disableUnmuteBtn.start, 4.dp)
                width = Dimension.fillToConstraints

            },
        enabled = enabled,
    )

    LabelButton(
        label = stringResource(Res.string.disable_unmute),
        onClick = onDisableSAVVYUnmutingClicked,
        modifier = Modifier
            .constrainAs(disableUnmuteBtn) {
                top.linkTo(enableUnmuteBtn.top)
                start.linkTo(enableUnmuteBtn.end, 4.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
        enabled = enabled,
    )
}

@Composable
private fun SpeedSelector(
    isKPH: Boolean,
    onKPHSelected: () -> Unit,
    onMPHSelected: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier.selectableGroup()) {
    LabeledRadialButton(
        label = stringResource(Res.string.kph),
        selected = isKPH,
        onClick = onKPHSelected,
    )

    LabeledRadialButton(
        label = stringResource(Res.string.mph),
        selected = !isKPH,
        onClick = onMPHSelected,
    )
}

@Preview
@Composable
private fun SAVVYPanelPreview() {
    Valentine1Theme(darkTheme = true) {
        Surface {
            SAVVYPanel(
                onEnableSAVVYUnmutingClicked = {},
                onDisableSAVVYUnmutingClicked = {},
                onReadSAVVYStatusClicked = {},
                onOverrideThumbwheelClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}