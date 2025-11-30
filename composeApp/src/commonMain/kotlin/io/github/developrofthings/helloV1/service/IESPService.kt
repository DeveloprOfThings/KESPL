@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.helloV1.service

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPResponse
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.ExperimentalUuidApi

interface IESPService {

    val connection: Flow<Unit>

    val connectionLoss: Flow<Unit>

    val v1CapabilityInfo: StateFlow<V1CapabilityInfo>


    val espData: Flow<ESPPacket>

    val displayData: Flow<DisplayData>

    val v1Type: StateFlow<ESPDevice.ValentineOne>

    val connectionStatus: StateFlow<ESPConnectionStatus>

    val v1connection: StateFlow<V1connection?>

    val hasV1connection: Boolean

    val isGen2: Boolean

    fun setV1connection(v1c: V1connection)

    suspend fun connect(): Boolean

    suspend fun connect(v1c: V1connection): Boolean

    suspend fun disconnect()

    suspend fun requestVersion(device: ESPDevice): ESPResponse<Version, ESPFailure>

    suspend fun requestSerial(device: ESPDevice): ESPResponse<SerialNumber, ESPFailure>

    suspend fun requestUserBytes(device: ESPDevice): ESPResponse<UserSettings, ESPFailure>

    suspend fun writeUserBytes(
        device: ESPDevice,
        userBytes: ByteArray
    ): ESPResponse<UserSettings, ESPFailure>

    suspend fun restoreDefaultSettings(device: ESPDevice): ESPResponse<Unit, ESPFailure>

    suspend fun requestSweepSections(): ESPResponse<List<SweepSection>, ESPFailure>

    suspend fun requestCustomSweeps(): ESPResponse<List<SweepDefinition>, ESPFailure>

    suspend fun requestDefaultSweeps(): ESPResponse<List<SweepDefinition>, ESPFailure>

    suspend fun requestMaxSweepIndex(): ESPResponse<Int, ESPFailure>

    suspend fun restoreDefaultSweeps(): ESPResponse<Unit, ESPFailure>

    suspend fun requestAllSweepData(): ESPResponse<SweepData, ESPFailure>

    suspend fun writeSweepDefinitions(
        sweepDefinitions: List<SweepDefinition>
    ): ESPResponse<Int, ESPFailure>

    suspend fun requestV1DisplayOn(on: Boolean): ESPResponse<Unit, ESPFailure>

    suspend fun requestV1Mute(muted: Boolean): ESPResponse<Unit, ESPFailure>

    suspend fun requestChangeV1Mode(mode: V1Mode): ESPResponse<Unit, ESPFailure>

    suspend fun requestCurrentVolume(): ESPResponse<V1Volume, ESPFailure>

    suspend fun requestAllVolumes(): ESPResponse<V1Volumes, ESPFailure>

    suspend fun requestDisplayCurrentVolume(): ESPResponse<Unit, ESPFailure>

    suspend fun requestWriteV1Volume(
        main: Int,
        mute: Int,
        provideUserFeedback: Boolean,
        skipFeedBackWhenNoChange: Boolean,
        saveVolume: Boolean,
    ): ESPResponse<Unit, ESPFailure>

    suspend fun requestAbortAudioDelay(): ESPResponse<Unit, ESPFailure>

    suspend fun requestAlertTables(enable: Boolean): ESPResponse<Unit, ESPFailure>

    suspend fun requestBatteryVoltage(): ESPResponse<String, ESPFailure>

    suspend fun requestSAVVYStatus(): ESPResponse<SAVVYStatus, ESPFailure>

    suspend fun requestVehicleSpeed(): ESPResponse<Int, ESPFailure>

    suspend fun requestOverrideSAVVYThumbwheel(
        speed: Int,
    ): ESPResponse<Unit, ESPFailure>

    suspend fun requestUnmuteSAVVY(
        enableUnmuting: Boolean,
    ): ESPResponse<Unit, ESPFailure>
}