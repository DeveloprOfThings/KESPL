package io.github.developrofthings.helloV1.ui.v1c

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.supportedTypes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class V1cDiscoveryViewModel(
    scanType: V1cType,
    private val espService: IESPService,
) : ViewModel() {

    private var _scanType: MutableStateFlow<V1cType> = MutableStateFlow(scanType)

    private var _availableScanType: MutableStateFlow<List<V1cType>> =
        MutableStateFlow(supportedTypes().filterNot { it == V1cType.Demo })

    private var _scannedDevices: MutableStateFlow<List<V1connectionScanResult>> =
        MutableStateFlow(emptyList())

    private var _scanJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    val state: StateFlow<V1cDiscoveryUiState> = combine(
        _scanType,
        _availableScanType,
        _scannedDevices,
        ::V1cDiscoveryUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = V1cDiscoveryUiState.DEFAULT,
    )

    init {
        scan()
    }

    private fun scan() {
        _scanJob = viewModelScope.launch { scan(_scanType.value) }
    }

    private suspend fun scan(scanType: V1cType) {
        // CLear the list before starting a scan
        _scannedDevices.emit(emptyList())
        IV1cScanner
            .getScanner(connType = scanType)
            .startScan(ESPScanMode.Balanced)
            .collect { v1c ->
                _scannedDevices.emit(
                    _scannedDevices.value
                        .toMutableList()
                        .updateOrInsert(v1c)
                )
            }
    }

    fun setScanType(scanType: V1cType) {
        viewModelScope.launch {
            _scanType.emit(scanType)
            scan()
        }
    }

    fun onV1cSelected(selectedV1c: V1connection) {
        espService.setV1connection(selectedV1c)
    }
}

fun MutableList<V1connectionScanResult>.updateOrInsert(
    scanResult: V1connectionScanResult
): MutableList<V1connectionScanResult> {
    val index = indexOfFirst { it.id == scanResult.id }
    if (index != -1) this[index] = scanResult
    else add(scanResult)
    return this
}

data class V1cDiscoveryUiState(
    val activeScanType: V1cType,
    val availableScanTypes: List<V1cType>,
    val devices: List<V1connectionScanResult>,
) {
    companion object {
        val DEFAULT: V1cDiscoveryUiState = V1cDiscoveryUiState(
            activeScanType = V1cType.LE,
            availableScanTypes = listOf(
                V1cType.Legacy,
                V1cType.LE,
            ),
            devices = emptyList(),
        )
    }
}