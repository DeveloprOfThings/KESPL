package io.github.developrofthings.helloV1.ui.display

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.developrofthings.helloV1.ui.EdgeToEdge
import io.github.developrofthings.helloV1.ui.component.ArrowDisplay
import io.github.developrofthings.helloV1.ui.component.BandsIndicator
import io.github.developrofthings.helloV1.ui.component.BarGraph
import io.github.developrofthings.helloV1.ui.component.SevenSegment
import io.github.developrofthings.helloV1.ui.component.litBarColor
import io.github.developrofthings.helloV1.ui.component.unlitBarColor
import io.github.developrofthings.kespl.packet.data.displayData.BandArrowIndicator
import io.github.developrofthings.kespl.packet.data.displayData.BogeyCounter7Segment
import io.github.developrofthings.kespl.packet.data.displayData.SignalStrengthBarGraph
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.ic_bluetooth
import hellov1.composeapp.generated.resources.ic_mute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object InfDisplay

fun NavController.showInfDisplayMirror() {
    navigate(InfDisplay)
}

fun NavGraphBuilder.infDisplayRoute() {
    composable<InfDisplay> {
        InfDisplayScreen()
    }
}

@Composable
fun InfDisplayScreen(viewModel: InfDisplayViewModel = koinViewModel()) {
    EdgeToEdge(lightIcons = true)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InfDisplay(
        uiState = uiState,
        modifier = Modifier
            .background(Color.Black)
            .systemBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
    )
}

@Composable
private fun InfDisplay(
    uiState: InfDisplayUiState,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1F,
        targetValue = 0F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500), // Adjust duration as needed
            repeatMode = RepeatMode.Restart
        ),
        label = "animated_progress"
    )
    val on = blinkProgress > 0.35F

    val bogeyCounter: BogeyCounter7Segment =
        if (on) uiState.bogeyCounterImg1 else uiState.bogeyCounterImg2
    val bandArrow: BandArrowIndicator =
        if (on) uiState.bandArrowIndicatorImg1 else uiState.bandArrowIndicatorImg2
    val signalStrength: SignalStrengthBarGraph = uiState.signalStrengthBarGraph

    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelMedium.copy(
            fontSize = 18.sp
        )
    ) {
        ConstraintLayout(modifier = modifier) {
            val (
                bogeyCounterRef,
                arrowsRef,
                bargraphRef,
                bandsRef,
                muteRef,
                btRef
            ) = createRefs()

            SevenSegment(
                segments = bogeyCounter.raw,
                modifier = Modifier
                    .width(55.dp)
                    .constrainAs(bogeyCounterRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(bandsRef.start)
                        horizontalBias = 0F
                    },
            )

            BandsIndicator(
                bands = bandArrow,
                modifier = Modifier.constrainAs(bandsRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    horizontalBias = 1F
                },
                textWidth = 28.dp,
                barSize = 20.dp,
            )

            if (uiState.hasMuteAndBTIndicator) {
                val mute: Boolean = if (on) uiState.muteImg1 else uiState.muteImg2
                Image(
                    imageVector = vectorResource(Res.drawable.ic_mute),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .constrainAs(muteRef) {
                            top.linkTo(bandsRef.top)
                            end.linkTo(bandsRef.start, margin = 8.dp)
                        },
                    colorFilter = ColorFilter.tint(
                        if(mute) litBarColor else unlitBarColor
                    )
                )

                val bt: Boolean = if (on) uiState.bluetoothImg1 else uiState.bluetoothImg2
                if (bt) {
                    Image(
                        imageVector = vectorResource(Res.drawable.ic_bluetooth),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
                            .constrainAs(btRef) {
                                bottom.linkTo(bandsRef.bottom)
                                end.linkTo(bandsRef.start, margin = 8.dp)
                            },
                        colorFilter = ColorFilter.tint(Color.Blue),
                    )
                }
            }


            ArrowDisplay(
                arrows = bandArrow,
                modifier = Modifier
                    .constrainAs(arrowsRef) {
                        top.linkTo(anchor = parent.top, margin = 16.dp)
                        start.linkTo(anchor = parent.start, margin = 16.dp)
                        end.linkTo(anchor = parent.end, margin = 16.dp)
                        bottom.linkTo(anchor = bargraphRef.top, margin = 16.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
            )

            BarGraph(
                strength = signalStrength,
                modifier = Modifier.constrainAs(bargraphRef) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    horizontalBias = 0F
                },
                barSize = 20.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun InfDisplayCompactPreview() {
    InfDisplay(
        uiState = InfDisplayUiState
            .DEFAULT
            .copy(
                bogeyCounterImg1 = BogeyCounter7Segment(0xCF.toByte()),
                bogeyCounterImg2 = BogeyCounter7Segment(0xCF.toByte()),
                signalStrengthBarGraph = SignalStrengthBarGraph(0xF.toByte()),
                bandArrowIndicatorImg1 = BandArrowIndicator(0xC6.toByte()),
                bandArrowIndicatorImg2 = BandArrowIndicator(0x86.toByte()),
            ),
        modifier = Modifier
            .background(Color.Black)
            .systemBarsPadding()
            .fillMaxSize(),
    )
}