package io.github.developrofthings.helloV1.ui.controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.developrofthings.helloV1.ESPDataRequest
import io.github.developrofthings.helloV1.TargetESPDevice
import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.onFailure
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.allVolumes
import io.github.developrofthings.kespl.packet.data.currentVolume
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.user.defaultUserBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ControlsViewModel(
    private val espService: IESPService,
    private val espDataLogRepository: ESPDataLogRepository,
) : ViewModel() {

    private val _userBytes = MutableStateFlow(defaultUserBytes)

    private val _infDisplayUiState: StateFlow<InfDisplayState>
        get() = espService
            .displayData
            .map(::InfDisplayState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = InfDisplayState.DEFAULT,
            )

    private val _volumeUiState: MutableStateFlow<VolumeUiState> = MutableStateFlow(
        VolumeUiState.DEFAULT
    )

    private val _targets: StateFlow<List<TargetESPDevice>>
        get() = espService
            .v1Type
            .map {
                when (it) {
                    ESPDevice.ValentineOne.Legacy -> {
                        // When the bus is in legacy mode we only can send version request to the
                        // V1connection
                        listOf(TargetESPDevice.V1connection)
                    }

                    ESPDevice.ValentineOne.Checksum -> {
                        defaultAvailableDevices + TargetESPDevice.ValentineOne.Checksum
                    }

                    ESPDevice.ValentineOne.NoChecksum -> {
                        defaultAvailableDevices + TargetESPDevice.ValentineOne.NoChecksum
                    }

                    else -> defaultAvailableDevices
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = defaultAvailableDevices,
            )

    val uiState: StateFlow<ControlsUiState> = combine(
        flow = espService.connectionStatus,
        flow2 = _infDisplayUiState,
        flow3 = _userBytes,
        flow4 = _volumeUiState,
        flow5 = _targets,
        transform = ::ControlsUiState
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ControlsUiState.DEFAULT,
        )

    init {
        espService
            .espData
            .onEach(::handleESPData)
            .launchIn(viewModelScope)

        espService
            .v1CapabilityInfo
            .onEach { capabilities ->
                _volumeUiState.update { currState ->
                    currState.copy(
                        canControlVolume = capabilities.supportsVolumeControl,
                        canAbortAudioDelay = capabilities.supportsAbortAudioDelay,
                        canRequestDisplayVolume = capabilities.supportsDisplayVolumeRequest,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun connect() {
        viewModelScope.launch {
            espService.connect()
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            espService.disconnect()
        }
    }

    private suspend fun handleESPData(packet: ESPPacket) {
        when (packet.packetIdentifier) {
            ESPPacketId.RespUserBytes -> {
                _userBytes.emit(
                    ByteArray(6).apply {
                        packet.copyInto(
                            destination = this,
                            length = this@apply.size,
                        )
                    }
                )
            }

            ESPPacketId.RespCurrentVolume -> {
                _volumeUiState.update { currState ->
                    packet.currentVolume().let {
                        currState.copy(
                            mainVolume = it.mainVolume.toFloat(),
                            muteVolume = it.mutedVolume.toFloat(),
                        )
                    }

                }
            }

            ESPPacketId.RespAllVolume -> {
                _volumeUiState.update { currState ->
                    packet.allVolumes().currentVolume.let {
                        currState.copy(
                            mainVolume = it.mainVolume.toFloat(),
                            muteVolume = it.mutedVolume.toFloat(),
                        )
                    }

                }
            }

            else -> {/*NO-OP*/
            }
        }
    }


    fun onESPAction(request: ESPDataRequest) {
        viewModelScope.launch {
            performESPRequest(request)
        }
    }

    private suspend fun performESPRequest(request: ESPDataRequest) {
        when (request) {
            is ESPDataRequest.Serial -> handleSerialRequest(request)
            is ESPDataRequest.Version -> handleVersionRequest(request)
            is ESPDataRequest.ReadUserBytes -> handleReadUserBytesRequest(request)
            is ESPDataRequest.WriteUserBytes -> handleWriteUserBytesRequest(request)
            is ESPDataRequest.RestoreDefaults -> handleRestoreDefaults(request)
            is ESPDataRequest.V1 -> handleV1Request(request)
            is ESPDataRequest.SAVVY -> handleSAVVYRequest(request)
        }
    }

    private suspend fun handleSerialRequest(request: ESPDataRequest.Serial) {
        espService.requestSerial(request.targetDevice)
            .onFailure {
                espDataLogRepository.addLog("Failed to read back device serial number reason: $it")
            }
    }

    private suspend fun handleVersionRequest(request: ESPDataRequest.Version) {
        espService.requestVersion(request.targetDevice)
            .onFailure {
                espDataLogRepository.addLog("Failed to read back device version number reason: $it")
            }
    }

    private suspend fun handleReadUserBytesRequest(request: ESPDataRequest.ReadUserBytes) {
        espDataLogRepository.addLog("Reading V1's user bytes")
        espService.requestUserBytes(request.targetDevice)
            .onFailure {
                espDataLogRepository.addLog("Failed to read back V1 user bytes: $it")
            }
    }

    private suspend fun handleWriteUserBytesRequest(request: ESPDataRequest.WriteUserBytes) {
        espDataLogRepository.addLog("Writing user bytes: ${request.userBytes.toHexString()}")
        espService.writeUserBytes(
            device = request.targetDevice,
            userBytes = request.userBytes
        ).also { resp ->
            when (resp) {
                is ESPResponse.Failure -> espDataLogRepository.addLog("Failed to write V1 user bytes: ${resp.data}")
                is ESPResponse.Success -> espDataLogRepository.addLog("Successfully wrote V1 user bytes: ${resp.data}")
            }
        }
    }

    private suspend fun handleV1Request(v1Request: ESPDataRequest.V1) {
        when (v1Request) {
            is ESPDataRequest.V1.AlertTable -> handleAlertTableRequest(v1Request)
            is ESPDataRequest.V1.DisplayOn -> handleDisplayRequest(v1Request.on)
            is ESPDataRequest.V1.Mute -> handleMuteRequest(v1Request.muted)
            is ESPDataRequest.V1.ChangeMode -> handleChangeModeRequest(v1Request.v1Mode)
            ESPDataRequest.V1.BatteryVoltage -> handleBatteryVoltageRequest()
        }
    }

    private suspend fun handleDisplayRequest(displayOn: Boolean) {
        espService.requestV1DisplayOn(displayOn)
            .onFailure {
                espDataLogRepository.addLog(
                    if (displayOn) "Failed to turn on the main display: $it"
                    else "Failed to turn off the main display: $it"
                )
            }
    }

    private suspend fun handleMuteRequest(muted: Boolean) {
        espService.requestV1Mute(muted)
            .onFailure {
                espDataLogRepository.addLog(
                    if (muted) "Failed to mute the V1: $it"
                    else "Failed to unmute the v1: $it"
                )
            }
    }

    private suspend fun handleChangeModeRequest(mode: V1Mode) {
        espService.requestChangeV1Mode(mode)
            .onFailure {
                espDataLogRepository.addLog("Failed to change the V1's logic mode: $it")
            }
    }

    private suspend fun handleBatteryVoltageRequest() {
        espService.requestBatteryVoltage()
            .onFailure {
                espDataLogRepository.addLog("Failed to request the battery voltage: $it")
            }
    }

    private suspend fun handleRestoreDefaults(request: ESPDataRequest.RestoreDefaults) {
        espService.restoreDefaultSettings(request.targetDevice)
            .onFailure {
                espDataLogRepository.addLog("Failed to restore default settings: ${it}")
            }
    }

    private suspend fun handleAlertTableRequest(request: ESPDataRequest.V1.AlertTable) {
        espService.requestAlertTables(
            enable = request.on,
        ).also { resp ->
            when (resp) {
                is ESPResponse.Failure -> espDataLogRepository.addLog(
                    log = if (request.on) {
                        "Failed to enable Alert tables reason: ${resp.data}"
                    } else "Failed to disable Alert tables reason: ${resp.data}"
                )

                is ESPResponse.Success -> {
                    espDataLogRepository.addLog(
                        log = if (request.on) "Successfully enabled Alert tables"
                        else "Successfully disabled Alert tables"
                    )
                }
            }
        }
    }

    private suspend fun handleSAVVYRequest(savvyRequest: ESPDataRequest.SAVVY) {
        when (savvyRequest) {
            is ESPDataRequest.SAVVY.OverrideThumbwheel -> handleSAVVYThumbwheelOverride(savvyRequest.speed)
            ESPDataRequest.SAVVY.SAVVYStatus -> handleSAVVYStatusRequest()
            is ESPDataRequest.SAVVY.Unmute -> handleSAVVYUnmuteRequest(savvyRequest.enableUnmuting)
            ESPDataRequest.SAVVY.VehicleSpeed -> handleVehicleSpeedRequest()
        }
    }

    private suspend fun handleSAVVYThumbwheelOverride(speed: Int) {
        espService.requestOverrideSAVVYThumbwheel(speed = speed)
            .onFailure {
                espDataLogRepository.addLog("Failed to override the SAVVY thumbwheel: $it")
            }
    }

    private suspend fun handleSAVVYUnmuteRequest(enableUnmuting: Boolean) {
        espService.requestUnmuteSAVVY(enableUnmuting = enableUnmuting)
            .onFailure {
                espDataLogRepository.addLog(
                    if (enableUnmuting) "Failed to enable the SAVVY unmuting: $it"
                    else "Failed to disable the SAVVY unmuting: $it"
                )
            }
    }

    private suspend fun handleSAVVYStatusRequest() {
        espService.requestSAVVYStatus()
            .onFailure {
                espDataLogRepository.addLog("Failed to request the SAVVY status: ${it}")
            }
    }

    private suspend fun handleVehicleSpeedRequest() {
        espService.requestVehicleSpeed()
            .onFailure {
                espDataLogRepository.addLog("Failed to request the vehicle speed: ${it}")
            }
    }
}

data class ControlsUiState(
    val connectionStatus: ESPConnectionStatus,
    val infDisplayState: InfDisplayState,
    val userBytes: ByteArray,
    val volumeState: VolumeUiState,
    val targets: List<TargetESPDevice>,
) {

    val isConnected: Boolean get() = connectionStatus == ESPConnectionStatus.Connected

    val didConnectionFailed: Boolean get() = connectionStatus == ESPConnectionStatus.ConnectionFailed

    val didLoseConnection: Boolean get() = connectionStatus == ESPConnectionStatus.ConnectionLost

    companion object {
        val DEFAULT: ControlsUiState = ControlsUiState(
            connectionStatus = ESPConnectionStatus.Disconnected,
            infDisplayState = InfDisplayState.DEFAULT,
            userBytes = defaultUserBytes,
            volumeState = VolumeUiState.DEFAULT,
            targets = defaultAvailableDevices,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ControlsUiState

        if (connectionStatus != other.connectionStatus) return false
        if (infDisplayState != other.infDisplayState) return false
        if (!userBytes.contentEquals(other.userBytes)) return false
        if (volumeState != other.volumeState) return false
        if (targets != other.targets) return false
        if (isConnected != other.isConnected) return false
        if (didConnectionFailed != other.didConnectionFailed) return false
        if (didLoseConnection != other.didLoseConnection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = connectionStatus.hashCode()
        result = 31 * result + infDisplayState.hashCode()
        result = 31 * result + userBytes.contentHashCode()
        result = 31 * result + volumeState.hashCode()
        result = 31 * result + targets.hashCode()
        result = 31 * result + isConnected.hashCode()
        result = 31 * result + didConnectionFailed.hashCode()
        result = 31 * result + didLoseConnection.hashCode()
        return result
    }
}

data class InfDisplayState(
    val isSoft: Boolean,
    val isEuro: Boolean,
    val isLegacy: Boolean,
    val isCustomSweeps: Boolean,
    val isTimeSlicing: Boolean,
    val isSearchingForAlerts: Boolean,
    val isDisplayActive: Boolean,
) {
    constructor(displayData: DisplayData) : this(
        isSoft = displayData.isSoft,
        isEuro = displayData.isEuro,
        isLegacy = displayData.isLegacy,
        isCustomSweeps = displayData.isCustomSweep,
        isTimeSlicing = displayData.isTimeSlicing,
        isSearchingForAlerts = displayData.isSearchingForAlerts,
        isDisplayActive = displayData.isDisplayActive,
    )

    companion object {
        val DEFAULT: InfDisplayState = InfDisplayState(
            isSoft = false,
            isLegacy = false,
            isEuro = false,
            isCustomSweeps = false,
            isTimeSlicing = false,
            isSearchingForAlerts = false,
            isDisplayActive = false,
        )
    }
}

data class VolumeUiState(
    val mainVolume: Float,
    val muteVolume: Float,
    val canControlVolume: Boolean,
    val canAbortAudioDelay: Boolean,
    val canRequestDisplayVolume: Boolean,
) {

    companion object {
        val DEFAULT: VolumeUiState = VolumeUiState(
            mainVolume = 5F,
            muteVolume = 4F,
            canControlVolume = false,
            canAbortAudioDelay = false,
            canRequestDisplayVolume = false,
        )
    }
}

private val defaultAvailableDevices: List<TargetESPDevice> = listOf(
    TargetESPDevice.RemoteDisplay,
    TargetESPDevice.RemoteAudio,
    TargetESPDevice.SAVVY,
    TargetESPDevice.ThirdParty1,
    TargetESPDevice.ThirdParty2,
    TargetESPDevice.ThirdParty3,
    TargetESPDevice.V1connection,
    TargetESPDevice.Reserved,
    TargetESPDevice.GeneralBroadcast,
    TargetESPDevice.Custom,
)