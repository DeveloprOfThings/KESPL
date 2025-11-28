package io.github.developrofthings.helloV1.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ESPLogViewModel(
    val espDataLogRepository: ESPDataLogRepository,
) : ViewModel() {

    val uiState: StateFlow<ESPLogUiState> = combine(
        flow = espDataLogRepository.filterDisplayData,
        flow2 = espDataLogRepository.filterAlertData,
        flow3 = espDataLogRepository.log,
        transform = { isFilteringDisplayData, isFilteringAlerts, log ->
            ESPLogUiState(
                isFilteringDisplayData = isFilteringDisplayData,
                isFilteringAlertData = isFilteringAlerts,
                espLog = log
            )
        }
    )
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ESPLogUiState(),
        )

    fun onDisplayDataFilteringChange(change: Boolean) {
        espDataLogRepository.setDisplayDataFiltering(enabled = change)
    }

    fun onAlertDataFilteringChange(change: Boolean) {
        espDataLogRepository.setAlertDataFiltering(enabled = change)
    }

    fun onClearLogClicked() {
        espDataLogRepository.clearLog()
    }
}

data class ESPLogUiState(
    val isFilteringDisplayData: Boolean = true,
    val isFilteringAlertData: Boolean = true,
    val espLog: List<String> = emptyList(),
) {
    val logCount: Int get() = espLog.size
}