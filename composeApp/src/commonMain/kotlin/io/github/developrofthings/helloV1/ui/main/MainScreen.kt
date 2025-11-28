@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.developrofthings.helloV1.ui.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AdaptStrategy
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.developrofthings.helloV1.ui.EdgeToEdge
import io.github.developrofthings.helloV1.ui.controls.ControlsScreen
import io.github.developrofthings.helloV1.ui.log.ESPLogScreen
import io.github.developrofthings.kespl.bluetooth.V1cType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Serializable
data object Main

fun NavGraphBuilder.mainRoute(
    onScanClick: (V1cType) -> Unit,
    onShowInfDisplayMirror: () -> Unit,
) {
    composable<Main> {
        MainScreen(
            onScanClick = onScanClick,
            onMirrorDisplayClick = onShowInfDisplayMirror,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    onScanClick: (V1cType) -> Unit,
    onMirrorDisplayClick: () -> Unit,
) {
    EdgeToEdge()
    Surface(
        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator(
            scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
            adaptStrategies = SupportingPaneScaffoldDefaults.adaptStrategies(
                supportingPaneAdaptStrategy = AdaptStrategy.Hide,
            ),
        )
        val scope = rememberCoroutineScope()
        SupportingPaneScaffold(
            directive = scaffoldNavigator.scaffoldDirective,
            scaffoldState = scaffoldNavigator.scaffoldState,
            mainPane = {
                MainPane(
                    onMirrorDisplayClick = onMirrorDisplayClick,
                    showESPLogButton = !scaffoldNavigator.isSupportingShowing(),
                    onShowESPLog = {
                        scope.launch {
                            scaffoldNavigator.navigateTo(ThreePaneScaffoldRole.Secondary)
                        }
                    },
                    onScanClick = onScanClick,
                )
            },
            supportingPane = {
                SupportingPane(
                    shouldShowDismissSupportingPaneButton = !scaffoldNavigator.isMainShowing(),
                    onDismissSupportingPane = {
                        scope.launch {
                            scaffoldNavigator.navigateBack(BackNavigationBehavior.PopUntilScaffoldValueChange)
                        }
                    },
                )
            },
            paneExpansionState =
                rememberPaneExpansionState(
                    keyProvider = scaffoldNavigator.scaffoldValue,
                    anchors = PaneExpansionAnchors,
                ),
            paneExpansionDragHandle = { state -> PaneExpansionDragHandleSample(state) },
        )
    }
}

@Composable
fun ThreePaneScaffoldPaneScope.MainPane(
    modifier: Modifier = Modifier,
    showESPLogButton: Boolean,
    onShowESPLog: () -> Unit,
    onScanClick: (V1cType) -> Unit,
    onMirrorDisplayClick: () -> Unit,
) = AnimatedPane(
    modifier = modifier
        .preferredWidth(.5F)
) {
    ControlsScreen(
        showESPLogButton = showESPLogButton,
        onShowESPLog = onShowESPLog,
        onScanClick = onScanClick,
        onMirrorDisplayClick = onMirrorDisplayClick,
    )
}

@Composable
fun ThreePaneScaffoldPaneScope.SupportingPane(
    shouldShowDismissSupportingPaneButton: Boolean,
    onDismissSupportingPane: () -> Unit,
    modifier: Modifier = Modifier,
) = AnimatedPane(
    modifier = modifier
        .preferredWidth(.5F)
) {
    ESPLogScreen(
        shouldShowDismissSupportingPaneButton = shouldShowDismissSupportingPaneButton,
        onDismissSupportingPane = onDismissSupportingPane
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview
@Composable
fun ThreePaneScaffoldScope.PaneExpansionDragHandleSample(
    state: PaneExpansionState = rememberPaneExpansionState()
) {
    val interactionSource = remember { MutableInteractionSource() }
    VerticalDragHandle(
        modifier =
            Modifier.paneExpansionDraggable(
                state,
                LocalMinimumInteractiveComponentSize.current,
                interactionSource
            ),
        interactionSource = interactionSource,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val PaneExpansionAnchors =
    listOf(
        PaneExpansionAnchor.Proportion(0.25f),
        PaneExpansionAnchor.Proportion(0.5f),
        PaneExpansionAnchor.Proportion(0.75f),
        PaneExpansionAnchor.Proportion(1f),
    )

private fun ThreePaneScaffoldNavigator<Any>.isShowing(role: ThreePaneScaffoldRole): Boolean =
    scaffoldValue[role] == PaneAdaptedValue.Expanded

private fun ThreePaneScaffoldNavigator<Any>.isMainShowing(): Boolean =
    isShowing(SupportingPaneScaffoldRole.Main)

private fun ThreePaneScaffoldNavigator<Any>.isSupportingShowing(): Boolean =
    scaffoldValue[SupportingPaneScaffoldRole.Supporting] != PaneAdaptedValue.Hidden