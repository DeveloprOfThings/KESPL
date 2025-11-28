package io.github.developrofthings.helloV1.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.developrofthings.helloV1.ui.component.CheckableText
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.alert
import hellov1.composeapp.generated.resources.clear_esp_log
import hellov1.composeapp.generated.resources.close_esp_log
import hellov1.composeapp.generated.resources.esp_log_cnt
import hellov1.composeapp.generated.resources.esp_log_plus
import hellov1.composeapp.generated.resources.ic_chevron_backward
import hellov1.composeapp.generated.resources.ic_delete
import hellov1.composeapp.generated.resources.inf_display
import hellov1.composeapp.generated.resources.scroll
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ESPLogScreen(
    viewModel: ESPLogViewModel = koinViewModel(),
    shouldShowDismissSupportingPaneButton: Boolean,
    onDismissSupportingPane: () -> Unit,
) {
    ESPLogScreen(
        uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
        shouldShowDismissSupportingPaneButton = shouldShowDismissSupportingPaneButton,
        onDismissSupportingPane = onDismissSupportingPane,
        onClearLogClicked = viewModel::onClearLogClicked,
        onFilterDisplayDataChanged = viewModel::onDisplayDataFilteringChange,
        onFilterAlertDataChanged = viewModel::onAlertDataFilteringChange,
    )
}

@Composable
private fun ESPLogScreen(
    uiState: ESPLogUiState,
    shouldShowDismissSupportingPaneButton: Boolean,
    onDismissSupportingPane: () -> Unit,
    onClearLogClicked: () -> Unit,
    onFilterDisplayDataChanged: (Boolean) -> Unit,
    onFilterAlertDataChanged: (Boolean) -> Unit,
) = Column(
    modifier = Modifier
        .statusBarsPadding()
        .fillMaxSize()
) {
    var autoScroll by remember { mutableStateOf(true) }
    ESPESPLogHeader(
        shouldShowDismissSupportingPaneButton = shouldShowDismissSupportingPaneButton,
        onDismissSupportingPane = onDismissSupportingPane,
        count = uiState.logCount,
        autoScroll = autoScroll,
        filterDisplayData = uiState.isFilteringDisplayData,
        filterAlertData = uiState.isFilteringAlertData,
        onAutoScrollChanged = { autoScroll = it },
        modifier = Modifier.fillMaxWidth(),
        onClearLogClicked = onClearLogClicked,
        onFilterDisplayDataChanged = onFilterDisplayDataChanged,
        onFilterAlertDataChanged = onFilterAlertDataChanged,
    )

    val state: LazyListState = rememberLazyListState()

    LaunchedEffect(autoScroll, uiState.espLog.size) {
        if (autoScroll && uiState.espLog.isNotEmpty()) {
            state.scrollToItem(uiState.espLog.lastIndex)
        }
    }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress && state.lastScrolledBackward) {
            autoScroll = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 32.dp,
        )
    ) {
        itemsIndexed(
            items = uiState.espLog,
            key = { i, it -> i },
        ) { i, value ->
            Text(text = value)
        }
    }
}

@Composable
private fun ESPESPLogHeader(
    shouldShowDismissSupportingPaneButton: Boolean,
    onDismissSupportingPane: () -> Unit,
    count: Int,
    autoScroll: Boolean,
    filterDisplayData: Boolean,
    filterAlertData: Boolean,
    onAutoScrollChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onClearLogClicked: () -> Unit,
    onFilterDisplayDataChanged: (Boolean) -> Unit,
    onFilterAlertDataChanged: (Boolean) -> Unit,
) = CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (shouldShowDismissSupportingPaneButton) {
                IconButton(
                    modifier = Modifier,
                    onClick = onDismissSupportingPane
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_chevron_backward),
                        contentDescription = stringResource(Res.string.close_esp_log),
                        modifier = Modifier.requiredSize(24.dp),
                    )
                }
            }

            val showLiveCount = count < 10_000
            Text(
                text = if (showLiveCount) {
                    stringResource(resource = Res.string.esp_log_cnt, count)
                } else stringResource(Res.string.esp_log_plus),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1F))

            IconButton(onClick = onClearLogClicked) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.clear_esp_log),
                    modifier = Modifier
                        .requiredSize(24.dp),
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckableText(
                text = stringResource(Res.string.scroll),
                checked = autoScroll,
                onCheckedChange = onAutoScrollChanged,
            )

            CheckableText(
                text = stringResource(Res.string.inf_display),
                checked = filterDisplayData,
                onCheckedChange = onFilterDisplayDataChanged,
            )

            CheckableText(
                text = stringResource(Res.string.alert),
                checked = filterAlertData,
                onCheckedChange = onFilterAlertDataChanged,
            )
        }
    }
}