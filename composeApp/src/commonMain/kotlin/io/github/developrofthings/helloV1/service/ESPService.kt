@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.helloV1.service

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.IESPClient
import io.github.developrofthings.kespl.V1CapabilityInfo
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.SAVVYStatus
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.V1Volumes
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.sweep.SweepData
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

class ESPService(
    private val espClient: IESPClient,
    private val coroutineScope: CoroutineScope,
) : IESPService {

    override val isBluetoothSupported: SharedFlow<Boolean> = IESPClient::querySystemBluetoothSupport
        .asFlow()
        .shareIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            replay = 1,
        )

    override val v1CapabilityInfo: StateFlow<V1CapabilityInfo>
        get() = espClient.v1CapabilityInfo

    override val espData: Flow<ESPPacket>
        get() = espClient.packets

    override val displayData: Flow<DisplayData>
        get() = espClient.displayData

    override val v1Type: StateFlow<ESPDevice.ValentineOne>
        get() = espClient.valentineOneType

    override val connectionStatus: StateFlow<ESPConnectionStatus>
        get() = espClient.connectionStatus

    private var _v1connection: MutableStateFlow<V1connection?> = MutableStateFlow(null)

    override val connection = v1connection
        .filterNotNull()
        .map { v1c ->
            connect(v1c)
            Unit
        }
        .onCompletion {
            // When this flow completes we MUST call disconnect from a separate and distinct
            // CoroutineContext because the current one will throw an `ChildCancelledException` if
            // we attempt to attempt to collect from a Flow
            coroutineScope.launch {
                disconnect()
            }
        }
        .shareIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(50)
        )

    override val connectionLoss: Flow<Unit> = combine(
        flow = v1connection
            .filter { it != null }
            .map { it != null }
            .distinctUntilChanged(),
        flow2 = connectionStatus
            .map { it == ESPConnectionStatus.ConnectionLost }
            .filter { it },
    ) { hasV1connection, connLost ->
        if(hasV1connection && connLost) connect()
        Unit
    }

    override val v1connection: StateFlow<V1connection?> get() = _v1connection

    override val hasV1connection: Boolean
        get() = v1connection.value != null

    override val isGen2: Boolean
        get() = espClient
            .v1CapabilityInfo
            .value.isGen2

    override fun setV1connection(v1c: V1connection) {
        _v1connection.tryEmit(value = v1c)
    }

    override suspend fun connect(): Boolean =
        _v1connection.value?.let { connect(v1c = it) } ?: false

    override suspend fun connect(v1c: V1connection): Boolean {
        // If we are already connected we don't need to do anything
        if (_v1connection.value == v1c && espClient.isConnected) return true

        // Terminate the previous connection
        espClient.disconnect()
        return espClient.connect(v1c = v1c)
    }

    override suspend fun disconnect() = espClient.disconnect()

    override suspend fun requestVersion(device: ESPDevice): ESPResponse<Version, ESPFailure> =
        espClient.requestDeviceVersion(destination = device)

    override suspend fun requestSerial(device: ESPDevice): ESPResponse<SerialNumber, ESPFailure> =
        espClient.requestDeviceSerialNumber(destination = device)

    override suspend fun requestUserBytes(device: ESPDevice): ESPResponse<UserSettings, ESPFailure> =
        espClient.requestUserSettings(destination = device, forceVersionRequest = false)

    override suspend fun writeUserBytes(
        device: ESPDevice,
        userBytes: ByteArray
    ): ESPResponse<UserSettings, ESPFailure> =
        espClient.writeUserBytes(destination = device, userBytes = userBytes, verifyBytes = true)

    override suspend fun restoreDefaultSettings(
        device: ESPDevice,
    ): ESPResponse<Unit, ESPFailure> = espClient.restoreFactoryDefaults(device)

    override suspend fun requestSweepSections(): ESPResponse<List<SweepSection>, ESPFailure> =
        espClient.requestSweepSections()

    override suspend fun requestCustomSweeps(): ESPResponse<List<SweepDefinition>, ESPFailure> =
        espClient.requestSweepDefinitions()

    override suspend fun requestDefaultSweeps(): ESPResponse<List<SweepDefinition>, ESPFailure> =
        espClient.requestDefaultSweepDefinitions()

    override suspend fun requestMaxSweepIndex(): ESPResponse<Int, ESPFailure> =
        espClient.requestMaxSweepIndex()

    override suspend fun restoreDefaultSweeps(): ESPResponse<Unit, ESPFailure> =
        espClient.restoreDefaultSweeps()

    override suspend fun requestAllSweepData(): ESPResponse<SweepData, ESPFailure> =
        espClient.requestSweepData()

    override suspend fun writeSweepDefinitions(sweepDefinitions: List<SweepDefinition>): ESPResponse<Int, ESPFailure> =
        espClient.writeSweepDefinitions(sweepDefinitions)

    override suspend fun requestV1DisplayOn(on: Boolean): ESPResponse<Unit, ESPFailure> =
        espClient.setMainDisplay(on = on)

    override suspend fun requestV1Mute(muted: Boolean): ESPResponse<Unit, ESPFailure> =
        espClient.mute(muted = muted)

    override suspend fun requestChangeV1Mode(mode: V1Mode): ESPResponse<Unit, ESPFailure> =
        espClient.changeMode(mode)

    override suspend fun requestCurrentVolume(): ESPResponse<V1Volume, ESPFailure> =
        espClient.requestCurrentVolume()

    override suspend fun requestAllVolumes(): ESPResponse<V1Volumes, ESPFailure> =
        espClient.requestAllVolumes()

    override suspend fun requestDisplayCurrentVolume(): ESPResponse<Unit, ESPFailure> =
        espClient.displayCurrentVolume()

    override suspend fun requestWriteV1Volume(
        main: Int,
        mute: Int,
        provideUserFeedback: Boolean,
        skipFeedBackWhenNoChange: Boolean,
        saveVolume: Boolean,
    ): ESPResponse<Unit, ESPFailure> = espClient.writeVolume(
        volume = V1Volume(
            mainVolume = main,
            mutedVolume = mute,
        ),
        provideUserFeedback = provideUserFeedback,
        skipFeedbackWhenNoChange = skipFeedBackWhenNoChange,
        saveVolume = saveVolume,
    )

    override suspend fun requestAbortAudioDelay(): ESPResponse<Unit, ESPFailure> =
        espClient.abortAudioDelay()

    override suspend fun requestAlertTables(enable: Boolean): ESPResponse<Unit, ESPFailure> =
        espClient.enableAlertTable(enable = enable)

    override suspend fun requestBatteryVoltage(): ESPResponse<String, ESPFailure> =
        espClient.requestBatteryVoltage()

    override suspend fun requestSAVVYStatus(): ESPResponse<SAVVYStatus, ESPFailure> =
        espClient.requestSAVVYStatus()

    override suspend fun requestVehicleSpeed(): ESPResponse<Int, ESPFailure> =
        espClient.requestVehicleSpeed()

    override suspend fun requestOverrideSAVVYThumbwheel(speed: Int): ESPResponse<Unit, ESPFailure> =
        espClient.overrideSAVVYThumbWheel(speed = speed)

    override suspend fun requestUnmuteSAVVY(enableUnmuting: Boolean): ESPResponse<Unit, ESPFailure> =
        espClient.unmuteSAVVY(enableUnmuting = enableUnmuting)
}

