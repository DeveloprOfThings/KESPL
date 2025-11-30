package io.github.developrofthings.helloV1.ui.dialog.sweep

import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.helloV1.ui.component.SweepDefinitionsColumnState
import io.github.developrofthings.helloV1.ui.component.SweepSectionColumnState
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.onFailure
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.alert.copy
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.sweep.maxSweepIndex
import io.github.developrofthings.kespl.packet.data.sweep.sweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.sweepSections
import io.github.developrofthings.kespl.unWrap
import io.github.developrofthings.kespl.utilities.extensions.primitive.shl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import kotlin.experimental.or

@OptIn(ExperimentalCoroutinesApi::class)
@Factory
class SweepInfoPresenter(
    private val scope: CoroutineScope,
    private val espService: IESPService,
    private val espDataLogRepository: ESPDataLogRepository,
) {
    private val sweepSections = MutableStateFlow<List<SweepSection>>(emptyList())

    private val currentSweeps = MutableStateFlow<List<SweepDefinition>>(emptyList())

    private val defaultSweeps = MutableStateFlow<List<SweepDefinition>>(emptyList())

    private val maxSweepIndex = MutableStateFlow(0)

    val uiState = combine(
        flow = maxSweepIndex,
        flow2 = sweepSections
            .map(::SweepSectionColumnState)
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = SweepSectionColumnState(
                    sweepSections = emptySweepSections,
                )
            ),
        flow3 = currentSweeps
            .map(::SweepDefinitionsColumnState)
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = SweepDefinitionsColumnState(
                    sweepDefinitions = emptySweepDefinitions
                )
            ),
        flow4 = espService
            .v1CapabilityInfo
            .map { it.supportsDefaultSweepRequest },
        flow5 = defaultSweeps
            .map(::SweepDefinitionsColumnState)
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = SweepDefinitionsColumnState(
                    sweepDefinitions = emptySweepDefinitions
                )
            ),
        transform = ::SweepInfoUiState,
    ).stateIn(
        scope,
        started = SharingStarted.Lazily,
        initialValue = SweepInfoUiState.DEFAULT,
    )

    init {
        espService
            .espData
            .filter {
                when (it.packetIdentifier) {
                    ESPPacketId.RespMaxSweepIndex -> true
                    ESPPacketId.RespSweepSections -> true
                    else -> false
                }
            }
            .onEach {
                when (it.packetIdentifier) {
                    ESPPacketId.RespMaxSweepIndex -> maxSweepIndex.emit(it.maxSweepIndex)
                    ESPPacketId.RespSweepSections -> sweepSections.emit(it.sweepSections())
                    else -> false
                }
            }
            .launchIn(scope)

        espService
            .espData
            .filter {
                when (it.packetIdentifier) {
                    ESPPacketId.RespMaxSweepIndex -> true
                    ESPPacketId.RespSweepSections -> true
                    else -> false
                }
            }
            .onEach {
                when (it.packetIdentifier) {
                    ESPPacketId.RespMaxSweepIndex -> maxSweepIndex.emit(it.maxSweepIndex)
                    ESPPacketId.RespSweepSections -> sweepSections.emit(it.sweepSections())
                    else -> false
                }
            }
            .launchIn(scope)

        // Observe default sweeps
        espService
            .v1CapabilityInfo
            .flatMapLatest { capabilityInfo ->
                if (!capabilityInfo.supportsDefaultSweepRequest) emptyFlow<List<SweepDefinition>>()
                else espService
                    .espData
                    .sweepDefs { it.packetIdentifier == ESPPacketId.RespDefaultSweepDefinitions }
            }
            .onEach(defaultSweeps::emit)
            .launchIn(scope)

        // Observe current sweeps
        espService
            .espData
            .sweepDefs()
            .onEach(currentSweeps::emit)
            .launchIn(scope)
    }

    fun onReadSweepSectionsClicked(): Unit {
        scope.launch {
            espService.requestSweepSections()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read Sweeps Sections reason: $it")
                }
        }
    }

    fun onReadMaxSweepIndexClicked(): Unit {
        scope.launch {
            espService
                .requestMaxSweepIndex()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read Max Sweep Index reason: $it")
                }
        }
    }

    fun onReadAllSweepsClicked(): Unit {
        scope.launch {
            espService.requestCustomSweeps()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read current Sweeps Defs. reason: $it")
                }
        }
    }

    fun onReadDefaultSweepsClicked(): Unit {
        scope.launch {
            espService.requestDefaultSweeps()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read Default Sweeps Defs. reason: $it")
                }
        }
    }

    fun onRestoreDefaultSweepsClicked(): Unit {
        scope.launch {
            espService.restoreDefaultSweeps()
                .unWrap(
                    onFailure = {
                        espDataLogRepository.addLog("Failed to restore Default Sweeps Defs. reason: $it")
                    },
                ) {
                    // Request the current sweeps
                    espService.requestCustomSweeps()
                        .onFailure {
                            espDataLogRepository.addLog("Failed to read current Sweeps Defs. reason: $it")
                        }
                }
        }
    }

    fun onReadSweepData(): Unit {
        scope.launch {
            espService.requestAllSweepData()
                .onFailure {
                    espDataLogRepository.addLog("Failed to read Sweeps Data reason: $it")
                }
        }
    }

    fun onWriteSweepsClicked(sweeps: List<SweepDefinition>): Unit {
        scope.launch {
            espService
                .writeSweepDefinitions(sweeps)
                .unWrap(
                    onFailure = {
                        espDataLogRepository.addLog("Failed to write Sweep Defs. reason: $it")
                    }
                ) { writeResult ->
                    if (writeResult != 0) {
                        espDataLogRepository.addLog("Invalid Sweep Definition @ index: $writeResult")
                        return@launch
                    }

                    // Sweeps were successfully accepted, now read them back to observe any changes
                    espService.requestCustomSweeps()
                        .onFailure {
                            espDataLogRepository.addLog("Failed to read current Sweeps Defs. reason: $it")
                        }
                }
        }
    }
}

fun Flow<ESPPacket>.sweepDefs(
    count: Int = 6,
    filter: (ESPPacket) -> Boolean = { it.packetIdentifier == ESPPacketId.RespSweepDefinition },
): Flow<List<SweepDefinition>> = flow {
    val sweepDefinitions = mutableListOf<SweepDefinition>()
    this@sweepDefs.filter(predicate = filter)
        .collect {
            sweepDefinitions.add(it.sweepDefinition())
            if (sweepDefinitions.count() == count) {
                // Make a copy before we emit
                emit(sweepDefinitions.copy())
                sweepDefinitions.clear()
            }
        }
}

data class SweepInfoUiState(
    val maxSweepIndex: Int,
    val sweepSectionsState: SweepSectionColumnState,
    val currentSweepsState: SweepDefinitionsColumnState,
    val defaultSweepsSupported: Boolean,
    val defaultSweepsState: SweepDefinitionsColumnState,
) {
    companion object Companion {
        val DEFAULT: SweepInfoUiState = SweepInfoUiState(
            maxSweepIndex = 0,
            sweepSectionsState = SweepSectionColumnState(sweepSections = emptySweepSections),
            currentSweepsState = SweepDefinitionsColumnState(sweepDefinitions = emptySweepDefinitions),
            defaultSweepsSupported = false,
            defaultSweepsState = SweepDefinitionsColumnState(sweepDefinitions = emptySweepDefinitions),
        )
    }
}


internal val emptySweepDefinitions: List<SweepDefinition> = List(6) {
    SweepDefinition(index = it, lowerEdge = 0, upperEdge = 0)
}

internal val emptySweepSections: List<SweepSection> = List(2) {
    SweepSection(
        indexCount = (((it + 1).toByte() shl 4) or 0x02.toByte()),
        lowerEdge = 0,
        upperEdge = 0,
    )
}