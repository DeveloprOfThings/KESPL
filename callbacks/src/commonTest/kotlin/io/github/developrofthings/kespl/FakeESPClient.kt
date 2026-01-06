package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.SAVVYStatus
import io.github.developrofthings.kespl.packet.data.SAVVYThumbwheelOverride
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.V1Volumes
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.alert.AlertData
import io.github.developrofthings.kespl.packet.data.alert.alertTable
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.displayData.displayData
import io.github.developrofthings.kespl.packet.data.sweep.SweepData
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import io.github.developrofthings.kespl.packet.isAlertData
import io.github.developrofthings.kespl.packet.isInfDisplayData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

class FakeESPClient(
    private val connectionStatusSource: StateFlow<ESPConnectionStatus>,
    private val espDataSource: Flow<ByteArray>,
    private val noDataSource: Flow<Unit>,
    private val notificationDataSource: Flow<String>,
) : IESPClient {

    override val connectionStatus: StateFlow<ESPConnectionStatus>
        get() = connectionStatusSource

    override val packets: Flow<ESPPacket>
        get() = espDataSource
            .map(::ESPPacket)

    override val displayData: Flow<DisplayData>
        get() = espDataSource
            .filter { it.isInfDisplayData }
            .map { it.displayData() }

    override val alertTable: Flow<List<AlertData>>
        get() = espDataSource
            .filter { it.isAlertData }
            .alertTable()

    override val noData: Flow<Unit>
        get() = noDataSource

    override val notificationData: Flow<String>
        get() = notificationDataSource

    override val v1CapabilityInfo: StateFlow<V1CapabilityInfo>
        get() = TODO("Intentionally NOT Implemented")
    override val valentineOneType: StateFlow<ESPDevice.ValentineOne>
        get() = TODO("Intentionally NOT Implemented")
    override val isDisplayOn: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isSoft: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isEuro: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isLegacy: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isDisplayActive: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isCustomSweep: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isTimeSlicing: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val isSearchingForAlerts: Flow<Boolean>
        get() = TODO("Intentionally NOT Implemented")
    override val infDisplayDataMode: Flow<V1Mode>
        get() = TODO("Intentionally NOT Implemented")
    override val priorityAlert: Flow<AlertData>
        get() = TODO("Intentionally NOT Implemented")
    override val junkAlerts: Flow<List<AlertData>>
        get() = TODO("Intentionally NOT Implemented")
    override val alertTableClosable: Flow<List<AlertData>>
        get() = TODO("Intentionally NOT Implemented")
    override val isConnected: Boolean
        get() = TODO("Intentionally NOT Implemented")
    override val connectionType: V1cType
        get() = TODO("Intentionally NOT Implemented")

    override suspend fun connect(
        connectionStrategy: ConnectionStrategy,
        scanDurationMillis: Duration
    ): Boolean {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun connect(
        v1c: V1connection,
        directConnect: Boolean
    ): Boolean {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun disconnect() {
        TODO("Intentionally NOT Implemented")
    }

    override fun connectAsync(
        connectionStrategy: ConnectionStrategy,
        scanDurationMillis: Duration
    ): Job {
        TODO("Intentionally NOT Implemented")
    }

    override fun connectAsync(
        v1c: V1connection,
        directConnect: Boolean
    ): Deferred<Boolean> {
        TODO("Intentionally NOT Implemented")
    }

    override fun disconnectAsync(): Job {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun canPersistLastDevices(persist: Boolean) {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun clearPersistedLastDevices() {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun hasPreviousV1connection(): Boolean {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestV1Version(timeout: Duration): ESPResponse<Version, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestDeviceVersion(
        destination: ESPDevice,
        timeout: Duration
    ): ESPResponse<Version, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestV1SerialNumber(timeout: Duration): ESPResponse<SerialNumber, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestDeviceSerialNumber(
        destination: ESPDevice,
        timeout: Duration
    ): ESPResponse<SerialNumber, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestV1UserSettings(
        forceVersionRequest: Boolean,
        timeout: Duration
    ): ESPResponse<UserSettings, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestUserSettings(
        destination: ESPDevice,
        forceVersionRequest: Boolean,
        timeout: Duration
    ): ESPResponse<UserSettings, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestUserBytes(
        destination: ESPDevice,
        timeout: Duration
    ): ESPResponse<ByteArray, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun writeV1UserBytes(
        userBytes: ByteArray,
        verifyBytes: Boolean,
        timeout: Duration
    ): ESPResponse<UserSettings, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun writeUserBytes(
        destination: ESPDevice,
        userBytes: ByteArray,
        verifyBytes: Boolean,
        timeout: Duration
    ): ESPResponse<UserSettings, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun restoreFactoryDefaults(
        destination: ESPDevice,
        verify: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun writeSweepDefinitions(
        sweepDefinitions: List<SweepDefinition>,
        timeout: Duration
    ): ESPResponse<Int, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestSweepDefinitions(timeout: Duration): ESPResponse<List<SweepDefinition>, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun restoreDefaultSweeps(
        verify: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestMaxSweepIndex(timeout: Duration): ESPResponse<Int, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestSweepSections(timeout: Duration): ESPResponse<List<SweepSection>, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestDefaultSweepDefinitions(timeout: Duration): ESPResponse<List<SweepDefinition>, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestSweepData(timeout: Duration): ESPResponse<SweepData, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun setMainDisplay(
        on: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun turnOnMainDisplay(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun turnOffMainDisplay(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun mute(
        muted: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun mute(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun unmute(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun changeMode(
        mode: V1Mode,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestCurrentVolume(timeout: Duration): ESPResponse<V1Volume, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestAllVolumes(timeout: Duration): ESPResponse<V1Volumes, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun writeVolume(
        volume: V1Volume,
        provideUserFeedback: Boolean,
        skipFeedbackWhenNoChange: Boolean,
        saveVolume: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun abortAudioDelay(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun displayCurrentVolume(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun enableAlertTable(
        enable: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestBatteryVoltage(timeout: Duration): ESPResponse<String, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestSAVVYStatus(timeout: Duration): ESPResponse<SAVVYStatus, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun requestVehicleSpeed(timeout: Duration): ESPResponse<Int, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun overrideSAVVYThumbWheel(
        override: SAVVYThumbwheelOverride,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }

    override suspend fun unmuteSAVVY(
        enableUnmuting: Boolean,
        timeout: Duration
    ): ESPResponse<Unit, ESPFailure> {
        TODO("Intentionally NOT Implemented")
    }
}