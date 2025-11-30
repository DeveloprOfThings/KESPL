package io.github.developrofthings.kespl

import arrow.core.Either
import arrow.fx.coroutines.raceN
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.ESPRequest
import io.github.developrofthings.kespl.packet.bogeyCounterMode
import io.github.developrofthings.kespl.packet.data.SAVVYStatus
import io.github.developrofthings.kespl.packet.data.SAVVYThumbwheelOverride
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.V1Volumes
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.alert.AlertData
import io.github.developrofthings.kespl.packet.data.alert.alertTable
import io.github.developrofthings.kespl.packet.data.allVolumes
import io.github.developrofthings.kespl.packet.data.asDouble
import io.github.developrofthings.kespl.packet.data.batteryVoltage
import io.github.developrofthings.kespl.packet.data.currentVolume
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.InfDisplayData
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.displayData.displayData
import io.github.developrofthings.kespl.packet.data.serialNumber
import io.github.developrofthings.kespl.packet.data.speed
import io.github.developrofthings.kespl.packet.data.status
import io.github.developrofthings.kespl.packet.data.sweep.SweepData
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.sweep.maxSweepIndex
import io.github.developrofthings.kespl.packet.data.sweep.sweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.sweepSections
import io.github.developrofthings.kespl.packet.data.sweep.toPayload
import io.github.developrofthings.kespl.packet.data.sweep.writeResult
import io.github.developrofthings.kespl.packet.data.toPayload
import io.github.developrofthings.kespl.packet.data.user.USER_BYTE_COUNT
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import io.github.developrofthings.kespl.packet.data.user.techDisplayUserBytes
import io.github.developrofthings.kespl.packet.data.user.valentineOneUserBytes
import io.github.developrofthings.kespl.packet.data.version
import io.github.developrofthings.kespl.packet.isAlertData
import io.github.developrofthings.kespl.packet.isDisplayOn
import io.github.developrofthings.kespl.packet.isInfDisplayData
import io.github.developrofthings.kespl.packet.isSoft
import io.github.developrofthings.kespl.packet.mode
import io.github.developrofthings.kespl.preferences.IESPPreferencesManager
import io.github.developrofthings.kespl.utilities.V1VersionInfo
import io.github.developrofthings.kespl.utilities.extensions.flow.waitForESPResponseForESPRequest
import io.github.developrofthings.kespl.utilities.extensions.flow.waitForMatchingInfDisplayDataObserveRequestFeedback
import io.github.developrofthings.kespl.utilities.extensions.flow.waitForNInfDisplayDataUnitObserveRequestFeedback
import io.github.developrofthings.kespl.utilities.extensions.primitive.toByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

internal class ESPClient(
    initialConnection: IConnection,
    private val espContext: ESPContext,
    private val flowController: ESPFlowController,
    private val preferencesManager: IESPPreferencesManager,
    private val scope: CoroutineScope,
) : IESPClient {

    //region State
    private var _connection: IConnection = initialConnection

    val connection: IConnection get() = _connection

    override val connectionType: V1cType = connection.connectionType


    override val valentineOneType: StateFlow<ESPDevice.ValentineOne> = flowController.valentineOneType

    override val v1CapabilityInfo: StateFlow<V1CapabilityInfo> = flowController
        .v1Version
        .map(::V1CapabilityInfo)
        .stateIn(
            scope = scope,
            initialValue = V1CapabilityInfo.DEFAULT,
            started = SharingStarted.Lazily,
        )

    override val connectionStatus: StateFlow<ESPConnectionStatus> = flowController.connectionStatus

    val espData: SharedFlow<ByteArray> = flowController.espData

    override val packets: Flow<ESPPacket> = flowController
        .espData
        .map(::ESPPacket)

    override val noData: Flow<Unit> = flowController.noData

    override val notificationData: Flow<String>
        get() = flowController.notificationData

    override val displayData: Flow<DisplayData>
        get() = espData
            .filter { it.isInfDisplayData }
            .map { it.displayData() }

    override val isDisplayOn: Flow<Boolean>
        get() = displayData
            .map { it.isDisplayOn }
            .distinctUntilChanged()

    override val isSoft: Flow<Boolean>
        get() = displayData
            .map { it.isSoft }
            .distinctUntilChanged()

    override val isEuro: Flow<Boolean>
        get() = displayData
            .map { it.isEuro }
            .distinctUntilChanged()

    override val isLegacy: Flow<Boolean>
        get() = displayData
            .map { it.isLegacy }
            .distinctUntilChanged()

    override val isDisplayActive: Flow<Boolean>
        get() = displayData
            .map { it.isDisplayActive }
            .distinctUntilChanged()

    override val isCustomSweep: Flow<Boolean>
        get() = displayData
            .map { it.isCustomSweep }
            .distinctUntilChanged()

    override val isSearchingForAlerts: Flow<Boolean>
        get() = displayData
            .map { it.isSearchingForAlerts }
            .distinctUntilChanged()

    override val infDisplayDataMode: Flow<V1Mode>
        get() = displayData
            .map { it.mode }
            .distinctUntilChanged()

    override val isTimeSlicing: Flow<Boolean>
        get() = displayData
            .map { it.isTimeSlicing }
            .distinctUntilChanged()

    override val alertTable: Flow<List<AlertData>>
        get() = espData
            .filter { it.isAlertData }
            .alertTable()

    override val priorityAlert: Flow<AlertData>
        get() = alertTable.filter { it.isNotEmpty() }
            .map { table -> table.first { it.isPriority } }

    override val junkAlerts: Flow<List<AlertData>>
        get() = alertTable
            .filter { it.isNotEmpty() }
            .map { table -> table.filter { it.isJunk } }

    override val alertTableClosable: Flow<List<AlertData>>
        get() = espData
            .onSubscription { enableAlertTable(enable = true) }
            .filter { it.isAlertData }
            .alertTable()
            .onCompletion { enableAlertTable(enable = false) }

    override val isConnected: Boolean
        get() = connection.connectionStatus.value == ESPConnectionStatus.Connected
    //endregion

    //region Connection & Device Store
    private suspend fun fetchLastCompatibleDevice(): V1connection? = connection.filterDevices(
        preferencesManager.persistedDevices.firstOrNull() ?: emptyList()
    ).lastOrNull()

    private suspend fun persistLastDevice(v1c: V1connection) {
        if (preferencesManager.canPersistDevices()) {
            preferencesManager.addV1connection(v1c)
        }
    }

    override suspend fun connect(
        connectionStrategy: ConnectionStrategy,
        scanDurationMillis: Duration,
    ): Boolean = when (connectionStrategy) {
        ConnectionStrategy.First -> firstScannedV1c(scanDurationMillis = scanDurationMillis)
        ConnectionStrategy.Strongest -> findDeviceOrStrongV1connection(
            deviceId = null,
            scanDurationMillis = scanDurationMillis,
        )

        ConnectionStrategy.Last -> fetchLastCompatibleDevice()
        ConnectionStrategy.LastThenStrongest -> {
            findDeviceOrStrongV1connection(
                // If there is no last device, passing in null will cause the strong devices to be
                // found
                deviceId = fetchLastCompatibleDevice()?.id,
                scanDurationMillis = scanDurationMillis,
            )
        }
    }?.let { v1c -> connect(v1c = v1c, directConnect = true) } == true

    @Suppress("UNCHECKED_CAST")
    @OptIn(FlowPreview::class)
    private suspend fun firstScannedV1c(
        scanDurationMillis: Duration,
    ) = withTimeoutOrNull(
        timeout = scanDurationMillis,
    ) {
        connection
            .scan()
            .firstOrNull()
            ?.device
    }

    /**
     * Scans for Bluetooth LE devices for a specified duration to find a specific device
     * by its MAC address or, if no address is provided or the specific device isn't found,
     * determines the device with the strongest average RSSI.
     *
     * The scan uses [ESPScanMode.LowLatency] and collects scan results, accumulating RSSI values
     * and scan counts for each discovered device. If a target `address` is provided,
     * the scan attempts to stop early once that device is found.
     *
     * @param deviceId A pseudo-unique identifier of the target device to find. If `null`, the
     * function will attempt to find the device with the strongest signal.
     * @param scanDurationMillis The maximum duration for the Bluetooth LE scan.
     * @return The [V1connection] device object if found (either the specified `address` or the one
     *         with the strongest signal), or `null` if no devices are found within the
     *         scan duration or if an error occurs.
     */
    private suspend fun findDeviceOrStrongV1connection(
        deviceId: String?,
        scanDurationMillis: Duration,
    ): V1connection? {
        val devices = mutableMapOf<String, ScanStats>()
        withTimeoutOrNull(
            timeout = scanDurationMillis
        ) {
            connection.scan(ESPScanMode.LowLatency)
                // Collect until we find the target device
                .transformWhile {
                    emit(it)
                    it.id != deviceId
                }
                .collect { scanResult ->
                    val device = devices[scanResult.id]?.let {
                        it.copy(
                            scans = it.scans + 1,
                            accumulatedRSSI = it.accumulatedRSSI + scanResult.rssi
                        )
                    } ?: ScanStats(
                        scans = 1,
                        accumulatedRSSI = scanResult.rssi,
                        scan = scanResult
                    )
                    devices[scanResult.id] = device
                }
        }

        // Check to see if we found the target device, otherwise choose the device with the strongest RSSI
        return deviceId?.let {
            devices[it]?.scan?.device
        } ?: devices
            .map { it.value }
            .maxByOrNull { it.accumulatedRSSI / it.scans }
            ?.scan?.device
    }

    override suspend fun connect(v1c: V1connection, directConnect: Boolean): Boolean {
        persistLastDevice(v1c)

        if (connection.connectionType != v1c.type) {
            _connection = getConnection(
                espContext = espContext,
                connectionScope = scope,
                connType = v1c.type,
            )
        }

        return connection.connect(v1c = v1c, directConnect = directConnect)
    }

    override suspend fun disconnect(): Unit = connection.disconnect()

    override fun connectAsync(
        connectionStrategy: ConnectionStrategy,
        scanDurationMillis: Duration,
    ): Job =
        /*
            We want to proxy through to [ESPClient#connect()] instead of the
            [IConnection#connectAsync] part of IConnection because the ESPClient will take care of
            bookkeeping details such as persisting the last device.
        */
        connection.connectionScope.launch {
            connect(
                connectionStrategy = connectionStrategy,
                scanDurationMillis = scanDurationMillis,
            )
        }

    override fun connectAsync(v1c: V1connection, directConnect: Boolean): Deferred<Boolean> =
        /*
            We want to proxy through to [ESPClient#connect()] instead of the
            [IConnection#connectAsync] part of IConnection because the ESPClient will take care of
            bookkeeping details such as persisting the last device.
        */
        connection.connectionScope.async {
            connect(v1c = v1c, directConnect = directConnect)
        }

    override fun disconnectAsync(): Job = connection.connectionScope.launch { disconnect() }

    override suspend fun canPersistLastDevices(persist: Boolean) {
        preferencesManager.setPersistDevices(canPersist = persist)
        // If we disable persistence, clear the saved device
        if (!persist) preferencesManager.clearPersistedDevices()
    }

    override suspend fun clearPersistedLastDevices() {
        preferencesManager.clearPersistedDevices()
    }

    override suspend fun hasPreviousV1connection(): Boolean = preferencesManager.hasPreviousDevice()
    //endregion

    //region Device Information
    override suspend fun requestV1Version(timeout: Duration): ESPResponse<Version, ESPFailure> =
        when (val v1Type = connection.v1Type.first()) {
            ESPDevice.ValentineOne.Unknown -> ESPFailure.V1NotDetermined.asFailure()
            ESPDevice.ValentineOne.Legacy -> ESPFailure.NotSupported.asFailure()
            ESPDevice.ValentineOne.NoChecksum,
            ESPDevice.ValentineOne.Checksum,
                -> {
                requestDeviceVersion(destination = v1Type, timeout = timeout)
            }
        }

    override suspend fun requestDeviceVersion(
        destination: ESPDevice,
        timeout: Duration,
    ): ESPResponse<Version, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqVersion,
            )
        ).onFailureResponse { return@requestDeviceVersion it }

        return waitForRequestResponse(
            target = destination,
            targetPacketId = ESPPacketId.ReqVersion,
            responsePacketId = ESPPacketId.RespVersion,
            timeout = timeout,
            transformESPPacket = { it.version() },
        )
    }

    override suspend fun requestV1SerialNumber(
        timeout: Duration,
    ): ESPResponse<SerialNumber, ESPFailure> =
        when (val v1Type = connection.v1Type.first()) {
            ESPDevice.ValentineOne.Unknown -> ESPFailure.V1NotDetermined.asFailure()
            ESPDevice.ValentineOne.Legacy -> ESPFailure.NotSupported.asFailure()
            ESPDevice.ValentineOne.NoChecksum,
            ESPDevice.ValentineOne.Checksum,
                -> {
                requestDeviceSerialNumber(destination = v1Type)
            }
        }

    @OptIn(FlowPreview::class)
    override suspend fun requestDeviceSerialNumber(
        destination: ESPDevice,
        timeout: Duration,
    ): ESPResponse<SerialNumber, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqSerialNumber,
            )
        ).onFailureResponse { return@requestDeviceSerialNumber it }

        return waitForRequestResponse(
            target = destination,
            targetPacketId = ESPPacketId.ReqSerialNumber,
            responsePacketId = ESPPacketId.RespSerialNumber,
            timeout = timeout,
            transformESPPacket = { it.serialNumber() },
        )
    }
    //endregion

    //region User Setup Options
    override suspend fun requestV1UserSettings(
        forceVersionRequest: Boolean,
        timeout: Duration,
    ): ESPResponse<UserSettings, ESPFailure> = when (val v1Type = connection.v1Type.first()) {
        ESPDevice.ValentineOne.Unknown -> ESPFailure.V1NotDetermined.asFailure()
        ESPDevice.ValentineOne.Legacy -> ESPFailure.NotSupported.asFailure()
        ESPDevice.ValentineOne.NoChecksum,
        ESPDevice.ValentineOne.Checksum,
            -> {
            requestUserSettings(
                destination = v1Type,
                forceVersionRequest = forceVersionRequest,
                timeout = timeout,
            )
        }
    }

    override suspend fun requestUserSettings(
        destination: ESPDevice,
        forceVersionRequest: Boolean,
        timeout: Duration,
    ): ESPResponse<UserSettings, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        val deviceVersion = if (destination.isV1) {
            if (forceVersionRequest || !connection.hasV1Version) {
                when (val versionResult = requestV1Version(timeout = timeout)) {
                    is ESPResponse.Failure -> return ESPFailure.ESPOperationFailed.asFailure()
                    is ESPResponse.Success -> versionResult.data.asDouble()
                }
            } else connection.v1Version.value
        } else when (
            val versionResult = requestDeviceVersion(
                destination = destination,
                timeout = timeout
            )
        ) {
            is ESPResponse.Failure -> return versionResult
            is ESPResponse.Success -> versionResult.data.asDouble()
        }

        val writeResult = connection.writeRequest(
            ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqUserBytes,
            )
        )

        if (writeResult is ESPResponse.Failure) return writeResult

        return waitForRequestResponse(
            target = destination,
            targetPacketId = ESPPacketId.ReqUserBytes,
            responsePacketId = ESPPacketId.RespUserBytes,
            timeout = timeout,
            transformESPPacket = {
                if (destination.isV1) it.valentineOneUserBytes(deviceVersion)
                else it.techDisplayUserBytes(deviceVersion)
            },
        )
    }

    override suspend fun requestUserBytes(
        destination: ESPDevice,
        timeout: Duration,
    ): ESPResponse<ByteArray, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqUserBytes,
            )
        ).onFailureResponse { return@requestUserBytes it }

        return waitForRequestResponse(
            target = destination,
            targetPacketId = ESPPacketId.ReqUserBytes,
            responsePacketId = ESPPacketId.RespUserBytes,
            timeout = timeout,
            transformESPPacket = {
                ByteArray(USER_BYTE_COUNT).apply {
                    it.copyInto(
                        destination = this,
                        destinationOffset = 0,
                        startIndex = PAYLOAD_START_IDX,
                        endIndex = PAYLOAD_START_IDX + this@apply.size
                    )
                }
            },
        )
    }

    override suspend fun writeV1UserBytes(
        userBytes: ByteArray,
        verifyBytes: Boolean,
        timeout: Duration,
    ): ESPResponse<UserSettings, ESPFailure> = when (val v1Type = connection.v1Type.first()) {
        ESPDevice.ValentineOne.Unknown -> ESPFailure.V1NotDetermined.asFailure()
        ESPDevice.ValentineOne.Legacy -> ESPFailure.NotSupported.asFailure()
        ESPDevice.ValentineOne.NoChecksum,
        ESPDevice.ValentineOne.Checksum,
            -> {
            writeUserBytes(
                destination = v1Type,
                userBytes = userBytes,
                verifyBytes = verifyBytes,
                timeout = timeout,
            )
        }
    }

    override suspend fun writeUserBytes(
        destination: ESPDevice,
        userBytes: ByteArray,
        verifyBytes: Boolean,
        timeout: Duration,
    ): ESPResponse<UserSettings, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqWriteUserBytes,
                payload = userBytes,
            ),
        ).onFailureResponse { return@writeUserBytes it }

        waitForNInfDisplayDataUnit(
            target = destination,
            targetPacketId = ESPPacketId.ReqWriteUserBytes,
            v1 = connection
                .v1Type
                .first(),
            timeout = timeout,
            infDisplayDataCount = 5,
        ).onFailureResponse {
            return@writeUserBytes it
        }

        val userBytesResult = requestUserSettings(
            destination = destination,
            forceVersionRequest = false,
            timeout = timeout,
        )

        if (userBytesResult is ESPResponse.Failure) return userBytesResult
        // If the Legacy bit is set, this will always fail
        if (verifyBytes) {
            val readUserBytes = (userBytesResult as ESPResponse.Success)
                .data
                .userBytes

            if (!userBytes.contentEquals(readUserBytes)) {
                return ESPFailure.ESPOperationFailed.asFailure()
            }
        }
        return userBytesResult
    }

    override suspend fun restoreFactoryDefaults(
        destination: ESPDevice,
        verify: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = destination,
                requestId = ESPPacketId.ReqFactoryDefault,
            )
        ).onFailureResponse { return@restoreFactoryDefaults it }

        waitForNInfDisplayDataUnit(
            target = destination,
            targetPacketId = ESPPacketId.ReqFactoryDefault,
            v1 = if (destination.isV1) destination as ESPDevice.ValentineOne
            else connection.v1Type.first(),
            timeout = timeout,
            // Wait for 4 InfDisplayPackets
            infDisplayDataCount = 4,
        ).onFailureResponse { return@restoreFactoryDefaults it }

        return if (!verify) EmptySuccessESPResponse
        else {
            // Read the current user bytes
            val currentUserBytes = requestUserBytes(
                destination = destination,
                timeout = timeout,
            ).unWrap(
                onFailure = { return@restoreFactoryDefaults ESPFailure.ESPOperationFailed.asFailure() }
            ) { it }

            // Make sure we have a valid V1 version
            val v1Version = if (!connection.hasV1Version) {
                when (val versionResult = requestV1Version(timeout = timeout)) {
                    is ESPResponse.Failure -> return ESPFailure.ESPOperationFailed.asFailure()
                    is ESPResponse.Success -> versionResult.data.asDouble()
                }
            } else connection.v1Version.value

            V1VersionInfo.defaultUserBytesForVersion(version = v1Version)
                .let { defaultSweeps ->
                    if (!defaultSweeps.contentEquals(currentUserBytes)) {
                        ESPFailure.ESPOperationFailed.asFailure()
                    } else EmptySuccessESPResponse
                }
        }
    }
    //endregion

    //region Custom Sweep
    override suspend fun writeSweepDefinitions(
        sweepDefinitions: List<SweepDefinition>,
        timeout: Duration,
    ): ESPResponse<Int, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                sweepDefinitions.forEachIndexed { index, def ->
                    val isCommit = index == sweepDefinitions.lastIndex
                    connection.writeRequest(
                        ESPRequest(
                            destination = v1,
                            requestId = ESPPacketId.ReqWriteSweepDefinition,
                            payload = def.toPayload(commit = isCommit)
                        )
                    ).onFailureResponse { return@let it }

                    if (!isCommit) {
                        waitForNInfDisplayDataUnit(
                            target = v1,
                            targetPacketId = ESPPacketId.ReqWriteSweepDefinition,
                            v1 = v1,
                            timeout = timeout,
                            infDisplayDataCount = 2,
                        ).onFailureResponse { return@let it }
                    }
                }

                return waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqWriteSweepDefinition,
                    responsePacketId = ESPPacketId.RespSweepWriteResult,
                    timeout = timeout,
                    transformESPPacket = { it.writeResult },
                ).onSuccessResponse { writeResult ->
                    if (writeResult.data != 0) {
                        return ESPFailure.InvalidSweep(sweepNumber = writeResult.data).asFailure()
                    }
                    return writeResult
                }
            }
    }

    private suspend fun requestSweeps(
        defaultSweeps: Boolean,
        maxSweepIndex: Int,
        timeout: Duration,
    ): ESPResponse<List<SweepDefinition>, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        val sweepIndex = if (maxSweepIndex == -1) {
            val result = requestMaxSweepIndex(timeout = timeout)
            if (result is ESPResponse.Failure) return result
            (result as ESPResponse.Success).data
        } else maxSweepIndex

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    ESPRequest(
                        destination = v1,
                        requestId = if (defaultSweeps) ESPPacketId.ReqDefaultSweepDefinitions
                        else ESPPacketId.ReqAllSweepDefinitions,
                    )
                ).onFailureResponse { return@let it }

                val sweepDefinitions = mutableListOf<SweepDefinition>()
                val result = waitForRequestResponse<List<SweepDefinition>>(
                    target = v1,
                    targetPacketId = if (defaultSweeps) ESPPacketId.ReqDefaultSweepDefinitions
                    else ESPPacketId.ReqAllSweepDefinitions,
                    responsePacketId = if (defaultSweeps) ESPPacketId.RespDefaultSweepDefinitions
                    else ESPPacketId.RespSweepDefinition,
                    timeout = timeout,
                    transformESPPacket = {
                        sweepDefinitions.add(it.sweepDefinition())
                        if (sweepDefinitions.count() == (sweepIndex + 1)) sweepDefinitions
                        else null
                    },
                )
                if (result is ESPResponse.Failure) return@let result

                // This sort shouldn't be necessary but I like the sugary sweet guarantee of ordered
                // sweeps
                return sweepDefinitions.apply { this.sortBy { it.index } }.asSuccess()
            }
    }

    override suspend fun requestSweepDefinitions(timeout: Duration) =
        requestSweeps(
            defaultSweeps = false,
            maxSweepIndex = -1,
            timeout = timeout,
        )

    override suspend fun restoreDefaultSweeps(
        verify: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqDefaultSweeps,
                    )
                ).onFailureResponse { return@let it }

                waitForNInfDisplayDataUnit(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqDefaultSweeps,
                    v1 = v1,
                    timeout = timeout,
                    // Wait for 4 InfDisplayPackets
                    infDisplayDataCount = 4,
                ).onFailureResponse { return@restoreDefaultSweeps it }

                return if (!verify) EmptySuccessESPResponse
                else {
                    // Read the current sweeps
                    val currentSweeps = requestSweepDefinitions()
                        .unWrap(
                            onFailure = { return@let ESPFailure.ESPOperationFailed.asFailure() }
                        ) { it }

                    // Make sure we have a valid V1 version
                    val v1Version = if (!connection.hasV1Version) {
                        when (val versionResult = requestV1Version(timeout = timeout)) {
                            is ESPResponse.Failure -> return ESPFailure.ESPOperationFailed.asFailure()
                            is ESPResponse.Success -> versionResult.data.asDouble()
                        }
                    } else connection.v1Version.value

                    V1VersionInfo.defaultSweepForVersion(version = v1Version)
                        .let { defaultSweeps ->
                            if (defaultSweeps != currentSweeps) ESPFailure.ESPOperationFailed.asFailure()
                            else EmptySuccessESPResponse
                        }
                }
            }
    }

    override suspend fun requestMaxSweepIndex(timeout: Duration): ESPResponse<Int, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqMaxSweepIndex,
                    )
                ).onFailureResponse { return@let it }

                waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqMaxSweepIndex,
                    responsePacketId = ESPPacketId.RespMaxSweepIndex,
                    timeout = timeout,
                    transformESPPacket = { it.maxSweepIndex },
                )
            }
    }

    override suspend fun requestSweepSections(
        timeout: Duration,
    ): ESPResponse<List<SweepSection>, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqSweepSections,
                    )
                ).onFailureResponse { return@let it }

                waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqSweepSections,
                    responsePacketId = ESPPacketId.RespSweepSections,
                    timeout = timeout,
                    transformESPPacket = { it.sweepSections() },
                )
            }
    }

    override suspend fun requestDefaultSweepDefinitions(timeout: Duration) =
        requestSweeps(
            defaultSweeps = true,
            maxSweepIndex = -1,
            timeout = timeout,
        )

    override suspend fun requestSweepData(
        timeout: Duration,
    ): ESPResponse<SweepData, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let {
                val maxSweepIndexResult = requestMaxSweepIndex(timeout = timeout)
                val maxSweepIndex = if (maxSweepIndexResult is ESPResponse.Failure) {
                    return@let maxSweepIndexResult
                } else (maxSweepIndexResult as ESPResponse.Success).data

                val sweepSectionResult = requestSweepSections(timeout = timeout)
                val sweepSection = if (sweepSectionResult is ESPResponse.Failure) {
                    return@let sweepSectionResult
                } else (sweepSectionResult as ESPResponse.Success).data

                val defaultSweeps = if (v1CapabilityInfo.value.supportsReqDefaultSweepDefinitions) {
                    val defaultSweepDefinitionResult = requestSweeps(
                        defaultSweeps = true,
                        maxSweepIndex = maxSweepIndex,
                        timeout = timeout,
                    )
                    if (defaultSweepDefinitionResult is ESPResponse.Failure) {
                        return@let defaultSweepDefinitionResult
                    } else (defaultSweepDefinitionResult as ESPResponse.Success).data
                } else emptyList()

                val sweepDefinitionResult = requestSweeps(
                    defaultSweeps = false,
                    maxSweepIndex = maxSweepIndex,
                    timeout = timeout,
                )
                val sweepDefinitions = if (sweepDefinitionResult is ESPResponse.Failure) {
                    return@let sweepDefinitionResult
                } else (sweepDefinitionResult as ESPResponse.Success).data

                SweepData(
                    maxSweepIndex = maxSweepIndex,
                    sweepSections = sweepSection,
                    defaultSweepsDefinitions = defaultSweeps,
                    customSweepsDefinitions = sweepDefinitions,
                ).asSuccess()
            }
    }
    //endregion

    //region Display and Audio
    override suspend fun setMainDisplay(
        on: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = if (on) ESPPacketId.ReqTurnOnMainDisplay
                        else ESPPacketId.ReqTurnOffMainDisplay,
                    )
                ).onFailureResponse { return@let it }

                waitForMatchingInfDisplayData(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqChangeMode,
                    v1 = v1,
                    timeout = timeout,
                ) { (on == it.isDisplayOn) }
                    .onSuccess { EmptySuccessESPResponse }
            }
    }

    override suspend fun turnOnMainDisplay(timeout: Duration): ESPResponse<Unit, ESPFailure> =
        setMainDisplay(on = true, timeout = timeout)

    override suspend fun turnOffMainDisplay(timeout: Duration): ESPResponse<Unit, ESPFailure> =
        setMainDisplay(on = false, timeout = timeout)

    override suspend fun mute(
        muted: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = if (muted) ESPPacketId.ReqMuteOn
                        else ESPPacketId.ReqMuteOff,
                    )
                ).onFailureResponse { return@let it }

                waitForMatchingInfDisplayData(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqChangeMode,
                    v1 = v1,
                    timeout = timeout,
                ) { (muted == it.isSoft) }
                    .onSuccess { EmptySuccessESPResponse }
            }
    }

    override suspend fun mute(timeout: Duration): ESPResponse<Unit, ESPFailure> =
        mute(muted = true, timeout = timeout)

    override suspend fun unmute(timeout: Duration): ESPResponse<Unit, ESPFailure> =
        mute(muted = false, timeout = timeout)

    override suspend fun changeMode(
        mode: V1Mode,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                val writeResult = connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqChangeMode,
                        byteArrayOf(mode.byteValue),
                    )
                )
                if (writeResult is ESPResponse.Failure) return writeResult

                val capabilities = v1CapabilityInfo.value
                waitForMatchingInfDisplayData(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqChangeMode,
                    v1 = v1,
                    timeout = timeout,
                ) {
                    if (capabilities.hasInfDisplayDataLogicMode) it.mode == mode
                    else mode == it.bogeyCounterMode
                }.onSuccess { EmptySuccessESPResponse }
            }
    }

    override suspend fun requestCurrentVolume(
        timeout: Duration,
    ): ESPResponse<V1Volume, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqCurrentVolume,
                    )
                ).onFailureResponse { return@let it }

                waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqCurrentVolume,
                    responsePacketId = ESPPacketId.RespCurrentVolume,
                    timeout = timeout,
                    transformESPPacket = { it.currentVolume() },
                )
            }
    }

    override suspend fun requestAllVolumes(timeout: Duration): ESPResponse<V1Volumes, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqAllVolume,
                    )
                ).onFailureResponse { return@let it }

                waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqAllVolume,
                    responsePacketId = ESPPacketId.RespAllVolume,
                    timeout = timeout,
                    transformESPPacket = { it.allVolumes() },
                )
            }
    }

    override suspend fun writeVolume(
        volume: V1Volume,
        provideUserFeedback: Boolean,
        skipFeedbackWhenNoChange: Boolean,
        saveVolume: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqWriteVolume,
                        payload = volume.toPayload(
                            provideUserFeedback = provideUserFeedback,
                            skipFeedbackWhenNoChange = skipFeedbackWhenNoChange,
                            saveVolume = saveVolume,
                        ),
                    )
                ).onFailureResponse { return@writeVolume it }

                return@let EmptySuccessESPResponse
            }
    }

    override suspend fun abortAudioDelay(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqAbortAudioDelay,
                    )
                ).onFailureResponse { return@let it }

                // According to ESP Specification v. 3.012, it could take up to 100 milliseconds for
                // the Valentine One to response to this request so we wanna wait for at least 4
                // InfDisplayData packets
                waitForNInfDisplayDataUnit(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqAbortAudioDelay,
                    v1 = v1,
                    timeout = timeout,
                    // Wait for 4 InfDisplayPackets
                    infDisplayDataCount = 4,
                )
            }
    }

    override suspend fun displayCurrentVolume(timeout: Duration): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqDisplayCurrentVolume,
                    )
                ).onFailureResponse { return@let it }

                waitForNInfDisplayDataUnit(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqDisplayCurrentVolume,
                    v1 = v1,
                    timeout = timeout,
                    // Wait for 4 InfDisplayPackets
                    infDisplayDataCount = 4,
                )
            }
    }
    //endregion

    //region Alert Output
    override suspend fun enableAlertTable(
        enable: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    ESPRequest(
                        destination = v1,
                        requestId = if (enable) ESPPacketId.ReqStartAlertData
                        else ESPPacketId.ReqStopAlertData,
                    )
                ).onFailureResponse { return@let it }

                when (enable) {
                    true -> waitForRequestResponse(
                        target = v1,
                        targetPacketId = ESPPacketId.ReqStartAlertData,
                        responsePacketId = ESPPacketId.RespAlertData,
                        timeout = timeout,
                        transformESPPacket = { Unit },
                    )

                    false -> {
                        // TODO evaluate a solution that waits for 5 consecutive InfDisplayData
                        //  packets w/o RespAlertData packets
                        waitForNInfDisplayDataUnit(
                            target = v1,
                            targetPacketId = ESPPacketId.ReqStopAlertData,
                            v1 = v1,
                            timeout = timeout,
                            // Wait for 5 InfDisplayPackets
                            infDisplayDataCount = 5,
                        )
                    }
                }
            }
    }
    //endregion

    //region Miscellaneous
    override suspend fun requestBatteryVoltage(timeout: Duration): ESPResponse<String, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        return connection
            .v1Type
            .first()
            .let { v1 ->
                connection.writeRequest(
                    request = ESPRequest(
                        destination = v1,
                        requestId = ESPPacketId.ReqBatteryVoltage,
                    )
                ).onFailureResponse { return@let it }

                waitForRequestResponse(
                    target = v1,
                    targetPacketId = ESPPacketId.ReqBatteryVoltage,
                    responsePacketId = ESPPacketId.RespBatteryVoltage,
                    timeout = timeout,
                    transformESPPacket = { it.batteryVoltage() },
                )
            }
    }
    //endregion

    //region SAVVY Specific
    override suspend fun requestSAVVYStatus(timeout: Duration): ESPResponse<SAVVYStatus, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = ESPDevice.SAVVY,
                requestId = ESPPacketId.ReqSavvyStatus,
            )
        ).onFailureResponse { return@requestSAVVYStatus it }

        return waitForRequestResponse(
            target = ESPDevice.SAVVY,
            targetPacketId = ESPPacketId.ReqSavvyStatus,
            responsePacketId = ESPPacketId.RespSavvyStatus,
            timeout = timeout,
            transformESPPacket = { it.status() },
        )
    }

    override suspend fun requestVehicleSpeed(timeout: Duration): ESPResponse<Int, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = ESPDevice.SAVVY,
                requestId = ESPPacketId.ReqVehicleSpeed,
            )
        ).onFailureResponse { return@requestVehicleSpeed it }

        return waitForRequestResponse(
            target = ESPDevice.SAVVY,
            targetPacketId = ESPPacketId.ReqVehicleSpeed,
            responsePacketId = ESPPacketId.RespVehicleSpeed,
            timeout = timeout,
            transformESPPacket = { it.speed },
        )
    }

    override suspend fun overrideSAVVYThumbWheel(
        override: SAVVYThumbwheelOverride,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = ESPDevice.SAVVY,
                requestId = ESPPacketId.ReqOverrideThumbwheel,
                payload = byteArrayOf(override.speed)
            )
        ).onFailureResponse { return@overrideSAVVYThumbWheel it }

        // Wait for an appropriate number of InfDisplayData packets to be received before we attempt
        // to read back the SAVVY status
        waitForNInfDisplayDataUnit(
            target = ESPDevice.SAVVY,
            targetPacketId = ESPPacketId.ReqOverrideThumbwheel,
            v1 = connection.v1Type.first(),
            timeout = timeout,
            infDisplayDataCount = 4,
        ).onFailureResponse { return@overrideSAVVYThumbWheel it }

        requestSAVVYStatus(timeout = timeout)
            .unWrap(
                onFailure = {
                    return@overrideSAVVYThumbWheel ESPFailure.ESPOperationFailed.asFailure()
                }
            ) { it }
            .run {
                // If the threshold isn't user overridden we know immediately the operation failed
                // same for speed
                return if (!isThresholdUserOverride ||
                    currentSpeedThresholdKPH != override.speed.toInt()
                ) {
                    ESPFailure.ESPOperationFailed.asFailure()
                } else EmptySuccessESPResponse
            }
    }

    override suspend fun unmuteSAVVY(
        enableUnmuting: Boolean,
        timeout: Duration,
    ): ESPResponse<Unit, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()

        connection.writeRequest(
            request = ESPRequest(
                destination = ESPDevice.SAVVY,
                requestId = ESPPacketId.ReqSetSavvyUnmuteEnable,
                payload = byteArrayOf(enableUnmuting.toByte())
            )
        ).onFailureResponse { return@unmuteSAVVY it }

        // Wait for an appropriate number of InfDisplayData packets to be received before we attempt
        // to read back the SAVVY status
        waitForNInfDisplayDataUnit(
            target = ESPDevice.SAVVY,
            targetPacketId = ESPPacketId.ReqOverrideThumbwheel,
            v1 = connection.v1Type.first(),
            timeout = timeout,
            infDisplayDataCount = 4,
        ).onFailureResponse { return@unmuteSAVVY it }

        val status = requestSAVVYStatus(timeout = timeout)
            .unWrap(
                onFailure = { return@unmuteSAVVY ESPFailure.ESPOperationFailed.asFailure() }
            ) { it }

        if (status.isUnmuteEnabled != enableUnmuting) {
            return ESPFailure.ESPOperationFailed.asFailure()
        }

        return EmptySuccessESPResponse
    }
    //endregion

    //region ESPPacket Flow utilities
    /**
     * Suspends until the connection status changes from [ESPConnectionStatus.Connected]
     * to any other status, indicating a disconnection event.
     *
     * This function collects from the `connection.connectionStatus` flow and completes
     * when the first status that is not `Connected` is emitted.
     *
     * @return An [ESPResponse.Failure] containing [ESPFailure.NotConnected],
     *         signifying that a disconnection has occurred.
     * @see [IConnection.connectionStatus] The flow providing connection status updates.
     * @see ESPConnectionStatus For the different possible connection states.
     * @see ESPResponse.Failure
     * @see ESPFailure.NotConnected
     */
    private suspend inline fun waitUntilDisconnected(): ESPResponse.Failure<ESPFailure.NotConnected> =
        connection
            .connectionStatus
            .first { it != ESPConnectionStatus.Connected }
            .let { ESPFailure.NotConnected.asFailure() }

    /**
     * Waits for a response to a request sent to an ESP device.
     *
     * This function concurrently waits for either the expected ESP packet response
     * or a disconnection event. If the device is not connected, it immediately returns a
     * [ESPFailure.NotConnected] error.
     *
     * @param ResultType The type of data expected in the successful response.
     * @param target The [ESPDevice] to which the request was sent.
     * @param targetPacketId The [ESPPacketId] of the request packet that was sent.
     * @param responsePacketId The [ESPPacketId] of the expected response packet.
     * @param timeout The maximum [Duration] to wait for the response. Defaults to
     * [defaultRequestTimeout].
     * @param transformESPPacket A lambda function that transforms the raw [ByteArray] of the
     * response packet into the desired [ResultType]. It can return `null` if the transformation
     * fails or if the packet is not the expected one.
     * @return An [ESPResponse] which is either a [ESPResponse.Success] containing the [ResultType]
     * data or an [ESPResponse.Failure] containing an [ESPFailure]. Possible failures include
     * [ESPFailure.NotConnected] if the device disconnects or was not connected initially, or other
     * [ESPFailure] types if the response is not received within the timeout or if the
     * transformation fails.
     */
    @OptIn(FlowPreview::class)
    private suspend fun <ResultType> waitForRequestResponse(
        target: ESPDevice,
        targetPacketId: ESPPacketId,
        responsePacketId: ESPPacketId,
        timeout: Duration = defaultRequestTimeout,
        transformESPPacket: (ByteArray) -> ResultType?,
    ): ESPResponse<ResultType, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()
        // Wait for either the requested data to be returned or a disconnection
        return raceN(
            fa = {
                espData
                    .waitForESPResponseForESPRequest<ResultType>(
                        requestDestination = target,
                        requestPacketId = targetPacketId,
                        responsePacketId = responsePacketId,
                        timeout = timeout,
                        transformESPPacket = transformESPPacket
                    )
            },
            fb = { waitUntilDisconnected() },
        ).let {
            return@let when (it) {
                is Either.Left -> it.value
                is Either.Right -> it.value
            }
        }
    }

    /**
     * Waits for a specified number of [InfDisplayData] packets or a disconnection event after
     * sending a request.
     *
     * This function is useful for operations that don't have a direct response packet but are
     * confirmed by subsequent [InfDisplayData] packets indicating the device has processed the
     * request.
     * It concurrently waits for either the required number of [InfDisplayData] packets or a
     * disconnection. If the device is not connected, it immediately returns a
     * [ESPFailure.NotConnected] error.
     *
     * @param target The [ESPDevice] to which the request was sent.
     * @param targetPacketId The [ESPPacketId] of the request packet that was sent.
     * @param timeout The maximum [Duration] to wait for the [InfDisplayData] packets. Defaults to
     * [defaultRequestTimeout].
     * @param maxBusyPackets The maximum number of "busy" packets to ignore before considering the
     * operation timed out.
     * @param infDisplayDataCount The number of [InfDisplayData] packets to wait for.
     * @return An [ESPResponse] which is either a [ESPResponse.Success] containing the raw
     * [ByteArray] of the last received [InfDisplayData] packet, or an [ESPResponse.Failure]
     * containing an [ESPFailure]. Possible failures include [ESPFailure.NotConnected] if the device
     * disconnects or was not connected initially, or [ESPFailure.TimedOut] if the required number
     * of [InfDisplayData] packets is not received within the timeout.
     */
    @OptIn(FlowPreview::class)
    private suspend inline fun waitForNInfDisplayData(
        target: ESPDevice,
        targetPacketId: ESPPacketId,
        v1: ESPDevice.ValentineOne,
        timeout: Duration = defaultRequestTimeout,
        maxBusyPackets: Int = 10,
        infDisplayDataCount: Int = 5,
    ): ESPResponse<ByteArray, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()
        // Wait for either the requested data to be returned or a disconnection
        return raceN(
            fa = {
                espData
                    .waitForNInfDisplayDataUnitObserveRequestFeedback(
                        requestDestination = target,
                        requestPacketId = targetPacketId,
                        v1 = v1,
                        timeout = timeout,
                        maxBusyPackets = maxBusyPackets,
                        infDisplayDataCount = infDisplayDataCount,
                    )
            },
            fb = { waitUntilDisconnected() },
        ).let {
            return@let when (it) {
                is Either.Left -> it.value
                is Either.Right -> it.value
            }
        }
    }

    /**
     * Waits for a specified number of [InfDisplayData] packets or a disconnection event, then
     * returns a success or failure [ESPResponse].
     *
     * This is a specialized version of [waitForNInfDisplayData] that discards the actual
     * [InfDisplayData] content and simply signals success (with [Unit]) if the required number
     * of packets are received, or failure otherwise. It's useful when the mere reception of
     * these packets confirms an operation, and their content is irrelevant.
     *
     * It concurrently waits for either the required number of [InfDisplayData] packets or a
     * disconnection. If the device is not connected, it immediately returns a
     * [ESPFailure.NotConnected] error.
     *
     * @param target The [ESPDevice] to which the request was sent.
     * @param targetPacketId The [ESPPacketId] of the request packet that was sent.
     * @param timeout The maximum [Duration] to wait for the [InfDisplayData] packets. Defaults to
     * [defaultRequestTimeout].
     * @param maxBusyPackets The maximum number of "busy" packets to ignore before considering the
     * operation timed out.
     * @param infDisplayDataCount The number of [InfDisplayData] packets to wait for.
     * @return An [ESPResponse] which is either a [ESPResponse.Success] containing [Unit] if the
     * operation is successful, or an [ESPResponse.Failure] containing an [ESPFailure].
     * Possible failures include [ESPFailure.NotConnected] if the device disconnects or was not
     * connected initially, or [ESPFailure.TimedOut] if the required number of [InfDisplayData]
     * packets is not received within the timeout.
     */
    private suspend inline fun waitForNInfDisplayDataUnit(
        target: ESPDevice,
        targetPacketId: ESPPacketId,
        v1: ESPDevice.ValentineOne,
        timeout: Duration = defaultRequestTimeout,
        maxBusyPackets: Int = 10,
        infDisplayDataCount: Int = 2,
    ): ESPResponse<Unit, ESPFailure> = waitForNInfDisplayData(
        target = target,
        targetPacketId = targetPacketId,
        v1 = v1,
        timeout = timeout,
        maxBusyPackets = maxBusyPackets,
        infDisplayDataCount = infDisplayDataCount,
    ).onSuccess { EmptySuccessESPResponse }

    /**
     * Waits for an [InfDisplayData] packet that matches a specific condition, or for a
     * disconnection event.
     *
     * This function is used when an operation's success is indicated by a change in the
     * [InfDisplayData] stream that satisfies a given predicate. It concurrently waits for either
     * a matching [InfDisplayData] packet or a disconnection. If the device is not connected,
     * it immediately returns a [ESPFailure.NotConnected] error.
     *
     * @param target The [ESPDevice] to which the request was sent.
     * @param targetPacketId The [ESPPacketId] of the request packet that was sent.
     * @param timeout The maximum [Duration] to wait for the matching [InfDisplayData] packet.
     * Defaults to [defaultRequestTimeout].
     * @param maxBusyPackets The maximum number of "busy" packets to ignore before considering the
     * operation timed out.
     * @param transformESPPacket A lambda function that takes the raw [ByteArray] of an
     * [InfDisplayData] packet and returns `true` if it matches the desired condition, `false`
     * otherwise.
     * @return An [ESPResponse] which is either a [ESPResponse.Success] containing the raw
     * [ByteArray] of the matching [InfDisplayData] packet, or an [ESPResponse.Failure]
     * containing an [ESPFailure]. Possible failures include [ESPFailure.NotConnected] if the device
     * disconnects or was not connected initially, or [ESPFailure.TimedOut] if a matching
     * [InfDisplayData] packet is not received within the timeout.
     */
    @OptIn(FlowPreview::class)
    private suspend inline fun waitForMatchingInfDisplayData(
        target: ESPDevice,
        targetPacketId: ESPPacketId,
        v1: ESPDevice.ValentineOne,
        timeout: Duration = defaultRequestTimeout,
        maxBusyPackets: Int = 10,
        noinline transformESPPacket: (ByteArray) -> Boolean,
    ): ESPResponse<ByteArray, ESPFailure> {
        if (!isConnected) return ESPFailure.NotConnected.asFailure()
        // Wait for either the requested data to be returned or a disconnection
        return raceN(
            fa = {
                espData
                    .waitForMatchingInfDisplayDataObserveRequestFeedback(
                        requestDestination = target,
                        requestPacketId = targetPacketId,
                        v1 = v1,
                        timeout = timeout,
                        maxBusyPackets = maxBusyPackets,
                        infDisplayDataPredicate = transformESPPacket,
                    )
            },
            fb = { waitUntilDisconnected() },
        ).let {
            return@let when (it) {
                is Either.Left -> it.value
                is Either.Right -> it.value
            }
        }
    }
    //endregion
}

private data class ScanStats(
    val scans: Int,
    val accumulatedRSSI: Int,
    val scan: V1connectionScanResult,
)