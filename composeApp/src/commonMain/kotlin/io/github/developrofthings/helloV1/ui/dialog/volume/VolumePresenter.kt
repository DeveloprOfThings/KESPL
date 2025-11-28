package io.github.developrofthings.helloV1.ui.dialog.volume

import io.github.developrofthings.helloV1.combine
import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import io.github.developrofthings.helloV1.service.ESPService
import io.github.developrofthings.kespl.V1CapabilityInfo
import io.github.developrofthings.kespl.onFailure
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.allVolumes
import io.github.developrofthings.kespl.packet.data.currentVolume
import io.github.developrofthings.kespl.packet.isAllVolume
import io.github.developrofthings.kespl.packet.isVolume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VolumePresenter(
    private val scope: CoroutineScope,
    private val espService: ESPService,
    private val espDataLogRepository: ESPDataLogRepository,
) {

    private val _currentVolume = MutableStateFlow(
        V1Volume(
            mainVolume = 0,
            mutedVolume = 0,
        )
    )

    private val _savedVolume = MutableStateFlow(
        V1Volume(
            mainVolume = 0,
            mutedVolume = 0,
        )
    )

    private val _liveUpdateVolume = MutableStateFlow(false)
    private val _provideUserFeedback = MutableStateFlow(true)
    private val _skipFeedbackWhenIdentical = MutableStateFlow(false)
    private val _saveVolume = MutableStateFlow(true)


    val uiState = combine(
        flow = _currentVolume,
        flow2 = _savedVolume,
        flow3 = _liveUpdateVolume,
        flow4 = _provideUserFeedback,
        flow5 = _skipFeedbackWhenIdentical,
        flow6 = _saveVolume,
        flow7 = espService.v1CapabilityInfo,
        transform = ::VolumeUiState
    )
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VolumeUiState.DEFAULT
        )

    init {
        espService
            .espData
            .filter { it.isVolume }
            .map { it.currentVolume() }
            .onEach(_currentVolume::emit)
            .launchIn(scope)

        espService
            .espData
            .filter { it.isAllVolume }
            .map { it.allVolumes() }
            .onEach {
                _currentVolume.emit(it.currentVolume)
                _savedVolume.emit(it.savedVolume)
            }
            .launchIn(scope)
    }

    fun updateMainVolume(volumeLevel: Float) {
        with(uiState.value) {
            if(shouldLiveUpdateVolume) {
                writeVolume(
                    main = volumeLevel.toInt(),
                    mute = currentMuteVolume.toInt(),
                    provideUserFeedback = shouldProvideUserFeedback,
                    skipFeedBackWhenNoChange = shouldSkipFeedbackWhenIdentical,
                    saveVolume = shouldSaveVolume,
                )
            }
        }
        _currentVolume.update {
            it.copy(mainVolume = volumeLevel.toInt())
        }
    }

    fun updateMuteVolume(volumeLevel: Float) {
        with(uiState.value) {
            if(shouldLiveUpdateVolume) {
                writeVolume(
                    main = currentMainVolume.toInt(),
                    mute = volumeLevel.toInt(),
                    provideUserFeedback = shouldProvideUserFeedback,
                    skipFeedBackWhenNoChange = shouldSkipFeedbackWhenIdentical,
                    saveVolume = shouldSaveVolume,
                )
            }
        }
        _currentVolume.update {
            it.copy(mutedVolume = volumeLevel.toInt())
        }
    }

    fun onLiveUpdateVolumeChanged(change: Boolean) {
        _liveUpdateVolume.value = change
    }

    fun onProvideUserFeedbackChanged(change: Boolean) {
        _provideUserFeedback.value = change
    }

    fun onSkipFeedbackWhenIdenticalChanged(change: Boolean) {
        _skipFeedbackWhenIdentical.value = change
    }

    fun onSaveVolumeChanged(change: Boolean) {
        _saveVolume.value = change
    }

    fun onReadVolumeClicked() {
        scope.launch {
            espService.requestCurrentVolume()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read the V1's current volume: $it")
                }
        }
    }

    fun onReadAllVolumeClicked() {
        scope.launch {
            espService.requestAllVolumes()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read V1's current & saved volumes: $it")
                }
        }
    }

    fun onDisplayCurrentVolumeClicked() {
        scope.launch {
            espService.requestDisplayCurrentVolume()
                .onFailure {
                    espDataLogRepository.addLog("Failed to display the V1's current volume: $it")
                }
        }
    }

    fun onAbortDelayClicked() {
        scope.launch {
            espService.requestAbortAudioDelay()
                .onFailure {
                    espDataLogRepository.addLog("Failed to abort V1's audio delay: $it")
                }
        }
    }

    fun onWriteVolumeClicked() {
        with(uiState.value) {
            writeVolume(
                main = currentMainVolume.toInt(),
                mute = currentMuteVolume.toInt(),
                provideUserFeedback = shouldProvideUserFeedback,
                skipFeedBackWhenNoChange = shouldSkipFeedbackWhenIdentical,
                saveVolume = shouldSaveVolume,
            )
        }
    }

    private fun writeVolume(
        main: Int,
        mute: Int,
        provideUserFeedback: Boolean,
        skipFeedBackWhenNoChange: Boolean,
        saveVolume: Boolean,
    ) {
        scope.launch {
            espService.requestWriteV1Volume(
                main = main,
                mute = mute,
                provideUserFeedback = provideUserFeedback,
                skipFeedBackWhenNoChange = skipFeedBackWhenNoChange,
                saveVolume = saveVolume,
            )
                .onFailure {
                    espDataLogRepository.addLog("Failed to write the V1's volume: $it")
                }
        }
    }
}

data class VolumeUiState(
    val currentMainVolume: Float,
    val currentMuteVolume: Float,
    val savedMainVolume: Float,
    val savedMuteVolume: Float,
    val shouldLiveUpdateVolume: Boolean,
    val shouldProvideUserFeedback: Boolean,
    val shouldSkipFeedbackWhenIdentical: Boolean,
    val shouldSaveVolume: Boolean,
    val supportsDisplayVolume: Boolean,
    val supportsAbortVolumeDelay: Boolean,
    val supportsAllVolume: Boolean,
) {

    constructor(
        currentVolume: V1Volume,
        savedVolume: V1Volume,
        liveUpdateVolume: Boolean,
        provideUserFeedback: Boolean,
        skipFeedbackWhenIdentical: Boolean,
        saveVolume: Boolean,
        capabilities: V1CapabilityInfo,
    ) : this(
        currentMainVolume = currentVolume.mainVolume.toFloat(),
        currentMuteVolume = currentVolume.mutedVolume.toFloat(),
        savedMainVolume = savedVolume.mainVolume.toFloat(),
        savedMuteVolume = savedVolume.mutedVolume.toFloat(),
        shouldLiveUpdateVolume = liveUpdateVolume,
        shouldProvideUserFeedback = provideUserFeedback,
        shouldSkipFeedbackWhenIdentical = skipFeedbackWhenIdentical,
        shouldSaveVolume = saveVolume,
        supportsDisplayVolume = capabilities.supportsDisplayVolumeRequest,
        supportsAbortVolumeDelay = capabilities.supportsAbortAudioDelay,
        supportsAllVolume = capabilities.supportsAllVolumesRequest,
    )

    companion object {
        val DEFAULT: VolumeUiState = VolumeUiState(
            currentMainVolume = 0F,
            currentMuteVolume = 0F,
            savedMainVolume = 0F,
            savedMuteVolume = 0F,
            shouldLiveUpdateVolume = false,
            shouldProvideUserFeedback = false,
            shouldSkipFeedbackWhenIdentical = false,
            shouldSaveVolume = false,
            supportsDisplayVolume = false,
            supportsAbortVolumeDelay = false,
            supportsAllVolume = false,
        )
    }
}