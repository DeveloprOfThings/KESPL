package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.SAVVYStatus
import io.github.developrofthings.kespl.packet.data.SAVVYThumbwheelOverride
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.V1Volumes
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.alert.AlertData
import io.github.developrofthings.kespl.packet.data.displayData.Aux0
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.sweep.SweepData
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import io.github.developrofthings.kespl.packet.data.user.V18UserSettings
import io.github.developrofthings.kespl.preferences.IESPPreferencesManager
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.defaultESPScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface IESPClient {

    //region State
    /**
     * [StateFlow] of the most recently detected [V1CapabilityInfo] of the attached Valentine One
     */
    val v1CapabilityInfo: StateFlow<V1CapabilityInfo>

    /**
     * [StateFlow] of the current [ESPConnectionStatus] of the client. This connection status will
     * change based on the state of a connection with a [V1connection]. Default value of
     * [ESPConnectionStatus.Disconnected].
     */
    val connectionStatus: StateFlow<ESPConnectionStatus>

    /**
     * [StateFlow] of the most recently detected [ESPDevice.ValentineOne] of the attached Valentine
     * One. After establishing a connection with a [V1connection], this flow is initialized to
     * [ESPDevice.ValentineOne.Unknown] and will take approximately `4` [ESPPacketId.InfDisplayData]
     * packets to determine.
     */
    val valentineOneType: StateFlow<ESPDevice.ValentineOne>

    /**
     * A **hot** [Flow] of "No data detected" signals.
     *
     * This flow will emit [Unit] every 1.5 seconds if no "ESP" data has been detected.
     */
    val noData: Flow<Unit>

    /**
     * A **hot** Flow of "Notification Data".
     *
     * This flow emits [String]s in-between in the `<:` `>` tags contained in the "demo" ESP data
     * [String] used when constructing a [V1connection.Demo].
     */
    val notificationData: Flow<String>

    /**
     * A **cold** [Flow] of [ESPPacket] received via the connected [V1connection].
     */
    val packets: Flow<ESPPacket>

    /**
     * A **cold** [Flow] of [DisplayData] received via the connected [V1connection].
     *
     * @see DisplayData
     */
    val displayData: Flow<DisplayData>

    /**
     * A **hot** [Flow] that represents if the Valentine One's current display is "on" or "off".
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isDisplayOn].
     *
     * @see [displayData]
     * @see [DisplayData.isDisplayOn]
     * @see [Aux0.displayOn]
     */
    val isDisplayOn: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents the Valentine One's current audio status
     * ("muted" or "unmuted").
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isSoft].
     *
     * @see [displayData]
     * @see [DisplayData.isSoft]
     * @see [Aux0.soft]
     */
    val isSoft: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents if the Valentine One's is currently operating in
     * European ('Euro') mode.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isEuro].
     *
     * @see [displayData]
     * @see [DisplayData.isEuro]
     * @see [Aux0.euroMode]
     */
    val isEuro: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents if the Valentine One's is currently operating in
     * Legacy mode.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isLegacy].
     *
     * @see [displayData]
     * @see [DisplayData.isLegacy]
     * @see [Aux0.espLegacy]
     */
    val isLegacy: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents the Valentine One's current display status. This flow will
     * emit `false` if the Valentine One's display is showing a mode or the resting display
     * indicator. `true` is emitted if the display is actively showing an alert, volume or other
     * important information.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isDisplayActive].
     *
     * @see [displayData]
     * @see [DisplayData.isDisplayActive]
     * @see [Aux0.displayActive]
     */
    val isDisplayActive: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents if the Valentine One's currently has custom sweeps defined.
     * This flow will emit `false` if custom sweeps have not been defined. `true` is emitted if
     * Valentine One has custom sweeps defined and custom modes will be used if operating in Euro
     * Mode.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isCustomSweep].
     *
     * @see [displayData]
     * @see [DisplayData.isCustomSweep]
     * @see [Aux0.customSweep]
     */
    val isCustomSweep: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents if the Valentine One's currently time slicing. This flow
     * emits `false` if no accessories on the ESP bus are to have a time slice following the receipt
     * of the [DisplayData] this flag belongs. `true` is emitted if all accessories are allowed to
     * have a time slice.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isTimeSlicing].
     *
     * @see [displayData]
     * @see [DisplayData.isTimeSlicing]
     * @see [Aux0.tsHoldOff]
     */
    val isTimeSlicing: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents if the Valentine One's is actively searching for alerts.
     * This flow emits `false` if the Valentine One is not actively searching for alerts. `true` is
     * emitted if the Valentine One has successfully signed on and is actively searching for alerts.
     *
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isTimeSlicing].
     *
     * @see [displayData]
     * @see [DisplayData.isSearchingForAlerts]
     * @see [Aux0.systemStatus]
     */
    val isSearchingForAlerts: Flow<Boolean>

    /**
     * A **hot** [Flow] that represents the [V1Mode] in which the Valentine One's is currently
     * operating.
     * This flow is derived from [displayData] by applying a [Flow.map] operator on the source flow
     * and returning the value of [DisplayData.isTimeSlicing].
     *
     * @see [displayData]
     * @see [DisplayData.mode]
     * @since V4.1028
     */
    val infDisplayDataMode: Flow<V1Mode>

    /**
     * A **hot** [Flow] alert tables. This flow will emit a list of [AlertData] that belong to
     * "table" of alerts actively being detected by the Valentine One.
     *
     * This flow is derived from [packets] by subsequent applications of the [Flow.filter] and
     * [Flow.map] operators on the source flow.
     *
     * @see [packets]
     * @see [AlertData]
     */
    val alertTable: Flow<List<AlertData>>

    /**
     * A **hot** [Flow] that represents the __**priority**__ [AlertData] contained in the Valentine
     * One's alert table. This flow will not emit if the Valentine One's alert table is empty.
     *
     * This flow is derived from [alertTable] by subsequent applications of the [Flow.filter] and
     * [Flow.map] operators on the source flow and returning the first alert with
     * [AlertData.isPriority] == `true`.
     *
     * @see [packets]
     * @see [alertTable]
     * @see [AlertData]
     * @see [AlertData.isPriority]
     */
    val priorityAlert: Flow<AlertData>

    /**
     * A **hot** [Flow] that represents the  __**junk**__ [AlertData] contained in the Valentine
     * One's alert table. This flow will not emit if the Valentine One's alert table is empty or
     * there are no "junk" alerts detected.
     *
     * This flow is derived from [alertTable] by subsequent applications of the [Flow.filter] and
     * [Flow.map] operators on the source flow and returning the first alert with
     * [AlertData.isJunk] == `true`.
     *
     * @see [packets]
     * @see [alertTable]
     * @see [AlertData]
     * @see [AlertData.isJunk]
     */
    val junkAlerts: Flow<List<AlertData>>

    /**
     * A **hot** [Flow] alert tables. This flow will emit a list of [AlertData] that belong to
     * "table" of alerts actively being detected by the Valentine One. The distinguishing feature
     * between this flow and [alertTable] is that collection will request the Valentine One to start
     * sending alert data. On "completion" the flow will attempt to request the Valentine One to
     * stop sending alert data (not guaranteed).
     *
     * This flow is derived from [packets] by subsequent applications of the [Flow.filter] and
     * [Flow.map] operators on the source flow.
     *
     * @see [packets]
     * @see [AlertData]
     * @see [enableAlertTable]
     */
    val alertTableClosable: Flow<List<AlertData>>

    /**
     * Indicates if the client is currently connected to a [V1connection].
     *
     * This value is derived by comparing the the current value of [connectionStatus] [StateFlow]
     * `==` [ESPConnectionStatus.Connected].
     *
     * @see connectionStatus
     */
    val isConnected: Boolean

    val connectionType: V1cType
    //endregion

    //region Connection & Device Store
    /**
     * Attempts to connect to a Valentine One device using the specified connection strategy and
     * scan duration.
     *
     * @param connectionStrategy The strategy to use when selecting a device to connect to
     * (e.g., [ConnectionStrategy.LastThenStrongest], [ConnectionStrategy.First]). Defaults to
     * [ConnectionStrategy.LastThenStrongest].
     * @param scanDurationMillis The maximum duration to scan for devices. Defaults to
     * [defaultScanDuration].
     * @return `true` if a connection was successfully established, `false` otherwise.
     * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
     * supported on the device.
     * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
     * not supported on the device.
     *
     * @see ConnectionStrategy For different approaches to selecting a device.
     * @see defaultScanDuration For the default scanning time.
     */
    suspend fun connect(
        connectionStrategy: ConnectionStrategy = ConnectionStrategy.LastThenStrongest,
        scanDurationMillis: Duration = defaultScanDuration,
    ): Boolean

    /**
     * Attempts to connect to the specified [V1connection].
     *
     * @param v1c The [V1connection] object representing the device to connect to.
     * @param directConnect `true` if the an "immediate" connection should be attempted with [v1c];
     * attempts will timeout after a number of seconds. `false` if a connection should be
     * established as soon as the [V1connection] becomes available.
     * This argument is intended for clients wishing to attempt a connection that will not timeout
     * such as background re/connections. Under normal circumstances, the default value
     * (`true`) is the desired behavior. This argument only has an effect for [V1cType.LE]
     * connections on the **Android** platform. On iOS, Bluetooth connections attempts do not
     * timeout. To abort a connection attempt call [disconnect].
     *
     * @return `true` if the connection was successfully established, `false` otherwise.
     * @throws BTUnsupported If the connection type of `v1c` requires Bluetooth Classic and it's not
     * supported on the device.
     * @throws LeUnsupported If the connection type of `v1c` requires Bluetooth Low Energy and it's
     * not supported on the device.
     *
     * @see V1connection For details on the connection object.
     */
    suspend fun connect(v1c: V1connection, directConnect: Boolean = true): Boolean

    /**
     * Disconnects from the currently connected [V1connection].
     */
    suspend fun disconnect()

    /**
     * Asynchronously attempts to connect to a [V1connection] using the specified connection
     * strategy and scan duration. This function returns immediately with a [Job] that represents
     * the asynchronous connection attempt.
     *
     * @param connectionStrategy The strategy to use when selecting a device to connect to
     * (e.g., [ConnectionStrategy.LastThenStrongest], [ConnectionStrategy.First]). Defaults to
     * [ConnectionStrategy.LastThenStrongest].
     * @param scanDurationMillis The maximum duration to scan for devices. Defaults to
     * [defaultScanDuration].
     * @return A [Job] representing the asynchronous connection operation. You can use this job to
     * cancel the connection attempt or await its completion (though the connection status is
     * typically observed via [connectionStatus] flow).
     * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
     * supported on the device.
     * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
     * not supported on the device.
     *
     * @see ConnectionStrategy For different approaches to selecting a device.
     * @see defaultScanDuration For the default scanning time.
     * @see connectionStatus To observe the outcome of the connection attempt.
     */
    fun connectAsync(
        connectionStrategy: ConnectionStrategy = ConnectionStrategy.LastThenStrongest,
        scanDurationMillis: Duration = defaultScanDuration,
    ): Job

    /**
     * Asynchronously attempts to connect to the specified [V1connection]. This function returns
     * immediately with a [Deferred] that will complete with the connection result.
     *
     * @param v1c The [V1connection] object representing the device to connect to.
     * @param directConnect `true` if the an "immediate" connection should be attempted with [v1c];
     * attempts will timeout after a number of seconds. `false` if a connection should be
     * established as soon as the [V1connection] becomes available.
     * This argument is intended for clients wishing to attempt a connection that will not timeout
     * such as background re/connections. Under normal circumstances, the default value
     * (`true`) is the desired behavior. This argument only has an effect for [V1cType.LE]
     * connections on the **Android** platform. On iOS, Bluetooth connections attempts do not
     * timeout. To abort a connection attempt call [disconnect].
     *
     * @return A [Deferred] which will resolve to `true` if the connection was successfully
     * established, or `false` otherwise. You can `await()` this Deferred to get the result, or
     * handle its completion in other ways.
     * @throws BTUnsupported If the connection type of `v1c` requires Bluetooth Classic and it's not
     * supported on the device.
     * @throws LeUnsupported If the connection type of `v1c` requires Bluetooth Low Energy and it's
     * not supported on the device.
     *
     * @see V1connection For details on the connection object.
     * @see connectionStatus To observe the ongoing status of the connection attempt.
     */
    fun connectAsync(v1c: V1connection, directConnect: Boolean = true): Deferred<Boolean>

    /**
     * Asynchronously disconnects from the currently connected [V1connection]. This function returns
     * immediately with a [Job] that represents the asynchronous disconnection attempt.
     *
     * @return A [Job] representing the asynchronous disconnection operation.
     * The outcome of this operation can also be observed via [connectionStatus] flow).
     *
     * @see connectionStatus To observe the outcome of the disconnection attempt.
     */
    fun disconnectAsync(): Job

    /**
     * Sets whether the client should persist the last connected [V1connection].
     * When `persist` is `true`, the details of successfully connected devices will be saved,
     * allowing for quicker reconnections using [ConnectionStrategy.Last] or
     * [ConnectionStrategy.LastThenStrongest].
     *
     * If `persist` is `false`, any previously stored last device information will be cleared,
     * and future connections will not save their details.
     *
     * This setting is useful for scenarios where a user frequently connects to the same device
     * and wants to minimize the connection setup time.
     *
     * @param persist `true` to enable persisting of the last connected device, `false` to disable
     * and clear any existing persisted device.
     *
     * @see clearPersistedLastDevices To explicitly clear stored device information without changing
     * the persistence setting.
     * @see ConnectionStrategy.Last For connecting only to the last known device.
     * @see ConnectionStrategy.LastThenStrongest For trying the last known device first.
     */
    suspend fun canPersistLastDevices(persist: Boolean)

    /**
     * Clears any persisted information about the last connected [V1connection].
     *
     * This is useful if the user wishes to remove the stored device details without necessarily
     * disabling the persistence feature itself (controlled by [canPersistLastDevices]). After
     * calling this, connection strategies like [ConnectionStrategy.Last] or
     * [ConnectionStrategy.LastThenStrongest] will behave as if no device was previously connected
     * until a new connection is successfully established and persisted (if persistence is enabled).
     *
     * @see canPersistLastDevices To control whether the last device is persisted.
     */
    suspend fun clearPersistedLastDevices()

    /**
     * Checks if there is a previously connected [V1connection] persisted.
     *
     * This method can be used to determine if connection strategies like [ConnectionStrategy.Last]
     * or [ConnectionStrategy.LastThenStrongest] are likely to succeed in finding a previously
     * connected device.
     *
     * The persistence of the last connected device is controlled by [canPersistLastDevices].
     *
     * @return `true` if a previous [V1connection] is stored, `false` otherwise.
     *
     * @see canPersistLastDevices To control whether the last device is persisted.
     * @see clearPersistedLastDevices To clear any stored device information.
     * @see ConnectionStrategy.Last For connecting only to the last known device.
     * @see ConnectionStrategy.LastThenStrongest For trying the last known device first.
     */
    suspend fun hasPreviousV1connection(): Boolean
    //endregion

    //region Device Information
    /**
     * Requests the version information from the connected Valentine One.
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One. Defaults
     * to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [Version] information if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected]) if the request fails or times out.
     *
     * @see Version For the structure of the version information returned.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see requestDeviceVersion To request version from a specific ESP device.
     */
    suspend fun requestV1Version(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Version, ESPFailure>

    /**
     * Requests the version information from the specified ESP device.
     *
     * @param destination The [ESPDevice] from which to request the version information.
     * Supported devices typically include [ESPDevice.ValentineOne],[ESPDevice.RemoteDisplay], etc.
     * @param timeout The maximum duration to wait for a response from the device. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [Version] information if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails, times
     * out, or the device does not support this request.
     *
     * @see Version For the structure of the version information returned.
     * @see ESPDevice For the different types of ESP devices.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see requestV1Version For a convenience function specifically for the Valentine One.
     */
    suspend fun requestDeviceVersion(
        destination: ESPDevice,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Version, ESPFailure>

    /**
     * Requests the serial number from the connected Valentine One.
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One. Defaults
     * to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [SerialNumber] if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected]) if the request fails or times out.
     *
     * @see SerialNumber For the structure of the serial number information returned.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see requestDeviceSerialNumber To request serial number from a specific ESP device.
     */
    suspend fun requestV1SerialNumber(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<SerialNumber, ESPFailure>

    /**
     * Requests the serial number from the specified ESP device.
     *
     * @param destination The [ESPDevice] from which to request the serial number. Supported devices
     * typically include [ESPDevice.ValentineOne], [ESPDevice.RemoteDisplay], etc.
     * @param timeout The maximum duration to wait for a response from the device. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [SerialNumber] if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or the device does not support this request.
     *
     * @see SerialNumber For the structure of the serial number information returned.
     * @see ESPDevice For the different types of ESP devices.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see requestV1SerialNumber For a convenience function specifically for the Valentine One.
     */
    suspend fun requestDeviceSerialNumber(
        destination: ESPDevice,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<SerialNumber, ESPFailure>
    //endregion

    //region User Setup Options

    /**
     * Requests the user configuration settings (user bytes) from the connected Valentine One.
     *
     * @param forceVersionRequest If `true`, the client will request the V1's version
     * before requesting the user bytes. Defaults to `false`.
     * @param timeout The maximum duration to wait for a response from the Valentine One. Defaults
     * to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [UserSettings] if the request is successful. The
     * specific type of [UserSettings] (e.g., [V18UserSettings]) will depend on the version of the
     * connected Valentine One.
     *
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails, times
     * out, or the V1 does not support user byte requests.
     *
     * @see UserSettings For the base class of user settings information.
     * @see V18UserSettings For user settings specific to V1.8 and later models.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see requestUserSettings To request user bytes from a specific ESP device (not just V1).
     * @see requestV1Version To understand how the V1 version is determined.
     */
    suspend fun requestV1UserSettings(
        forceVersionRequest: Boolean = false,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<UserSettings, ESPFailure>


    /**
     * Requests the user configuration settings (user bytes) from the specified ESP device.
     *
     * @param destination The [ESPDevice] from which to request the user settings.
     * Only [ESPDevice.RemoteDisplay] and [ESPDevice.ValentineOne] support
     * [ESPPacketId.ReqUserBytes].
     * @param forceVersionRequest If `true`, the client will attempt to determine the version of the
     * [destination] device before requesting its user bytes. This setting is only necessary
     * when [destination] is [ESPDevice.ValentineOne]; all other [ESPDevice] types that support
     * user byte requests will always have their version checked first. Defaults to `false`.
     * @param timeout The maximum duration to wait for a response from the device. Defaults to
     * [defaultRequestTimeout].
     *
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [UserSettings] if the request is successful.
     * The actual type of [UserSettings] will depend on the `destination` device.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     * times out, or the device does not support user byte requests.
     *
     * @see UserSettings For the base class of user settings information.
     * @see ESPDevice For the different types of ESP devices.
     * @see ESPPacketId.ReqUserBytes For the underlying ESP request.
     * @see requestV1UserSettings For requesting user bytes specifically from a Valentine One.
     * @see defaultRequestTimeout For the default timeout duration.
     */
    suspend fun requestUserSettings(
        destination: ESPDevice,
        forceVersionRequest: Boolean = false,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<UserSettings, ESPFailure>

    /**
     * Requests the raw user configuration settings (user bytes) as a [ByteArray] from the
     * specified ESP device.
     *
     * This function is similar to [requestUserSettings] but returns the raw byte array
     * directly without parsing it into a [UserSettings] object.
     *
     * @param destination The [ESPDevice] from which to request the raw user bytes. Typically
     * [ESPDevice.ValentineOne] or [ESPDevice.RemoteDisplay]. Support for [ESPPacketId.ReqUserBytes]
     * varies by device.
     * @param timeout The maximum duration to wait for a response from the device. Defaults to
     * [defaultRequestTimeout].
     *
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the raw [ByteArray] of user settings if the request is
     * successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails, times
     * out, or the device does not support user byte requests.
     *
     * @see requestUserSettings For requesting parsed [UserSettings].
     * @see ESPDevice For the different types of ESP devices.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     */
    suspend fun requestUserBytes(
        destination: ESPDevice,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<ByteArray, ESPFailure>

    /**
     * Attempts to write [userBytes] to update the user configuration settings inside the Valentine
     * One.
     *
     * @param userBytes Desired Valentine One user configuration.
     * @param verifyBytes `true` if the client should read back the Valentine One's user bytes to
     * verify the values written were set.
     * NOTE: This will fail if the [V18UserSettings.UserByte2.fForceLegacyDisplayDisabled] bit is set.
     * @param timeout The timeout the client should wait before timing out this operation.
     *
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] containing the updated [UserSettings] if the operation was
     * successful (and verification passed, if enabled).
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed. If `verifyBytes`
     * is `true` and the user bytes read back from the Valentine One don't match `userBytes`,
     * an [ESPFailure.ESPOperationFailed] will be returned. Other failures include
     * [ESPFailure.TimedOut], [ESPFailure.NotConnected], etc.
     *
     * @see UserSettings For the structure of user settings.
     * @see V18UserSettings.UserByte2.fForceLegacyDisplayDisabled For the specific bit that can cause verification
     * failure.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     */
    suspend fun writeV1UserBytes(
        userBytes: ByteArray,
        verifyBytes: Boolean,
        timeout: Duration = defaultLongRequestTimeout,
    ): ESPResponse<UserSettings, ESPFailure>

    /**
     * Attempts to write [userBytes] to update the user configuration settings inside target
     * [ESPDevice].
     *
     * @param destination The [ESPDevice] who's user configuration settings you would like to write.
     * Only [ESPDevice.ValentineOne] and [ESPDevice.RemoteDisplay] support
     * [ESPPacketId.ReqWriteUserBytes].
     * @param userBytes Desired user configuration byte array. For [ESPDevice.ValentineOne], this
     * typically represents the Valentine One's user settings.
     * @param verifyBytes `true` if the client should read back the device's user bytes to
     * verify the values written were successfully set.
     * NOTE: When `destination` is [ESPDevice.ValentineOne], this verification may fail if the
     * [V18UserSettings.UserByte2.fForceLegacyDisplayDisabled] bit is set in the `userBytes`.
     * @param timeout The maximum duration the client should wait before timing out this operation.
     * Defaults to [defaultLongRequestTimeout] as V1 operations can sometimes take longer.
     *
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] containing the updated [UserSettings] if the operation was
     * successful (and verification passed, if enabled). The actual type of [UserSettings]
     * returned will depend on the `destination` device.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   - If `verifyBytes` is `true` and the user bytes read back from `destination` do not match
     *     `userBytes`, an [ESPFailure.ESPOperationFailed] will be returned.
     *   - Other possible failures include [ESPFailure.TimedOut], [ESPFailure.NotConnected],
     *     [ESPFailure.NotSupported], etc.
     *
     * @see UserSettings For the structure of user settings.
     * @see V18UserSettings.UserByte2.fForceLegacyDisplayDisabled For the specific bit that can cause verification
     * failure with Valentine One devices.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     */
    suspend fun writeUserBytes(
        destination: ESPDevice,
        userBytes: ByteArray,
        verifyBytes: Boolean = true,
        // When the V1 is busy this could potentially take a long time to take effect
        timeout: Duration = defaultLongRequestTimeout,
    ): ESPResponse<UserSettings, ESPFailure>

    /**
     * Requests that the the specified ESP device restore its factory default settings.
     * This operation typically resets user-configurable parameters to their original,
     * out-of-the-box state.
     *
     * Note: Not all ESP devices may support this command.
     *
     * @param destination The [ESPDevice] to which the restore factory defaults command should be
     * sent. Common targets might include [ESPDevice.ValentineOne] or other configurable
     * accessories.
     * @param verify `true` if the client should read back the Valentine One's user settings and
     * verify they were set back to default values.
     * @param timeout The maximum duration to wait for the device to acknowledge the command.
     * Defaults to [defaultRequestTimeout].
     *
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the device.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed. Possible
     *   failures include:
     *   - [ESPFailure.TimedOut]: The device did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: No ESP device is currently connected.
     *   - [ESPFailure.NotSupported]: The `destination` device does not support the restore
     *   factory defaults command.
     *   - Other communication errors.
     *
     * @see ESPDevice For the different types of ESP devices.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqFactoryDefault For the underlying ESP request.
     */
    suspend fun restoreFactoryDefaults(
        destination: ESPDevice,
        verify: Boolean = true,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>
    //endregion

    //region Custom Sweep
    /**
     * Attempts to write specified list of [SweepDefinition] to the Valentine One.
     *
     * @param sweepDefinitions The list of sweep definitions to write.
     * @param timeout The timeout duration for the request. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `0` if the custom sweeps write was successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.V1NotDetermined]: The client has not determined the Valentine One in charge
     *   of the ESP bus.
     *   - [ESPFailure.InvalidSweep]: with the sweep number of the first invalid [SweepDefinition].
     *
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqWriteSweepDefinition For the underlying ESP request.
     * @see ESPPacketId.RespSweepWriteResult For the underlying ESP request.
     */
    suspend fun writeSweepDefinitions(
        sweepDefinitions: List<SweepDefinition>,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Int, ESPFailure>

    /**
     * Requests the current list of [SweepDefinition] from the connected Valentine One.
     *
     * Sweep definitions allow users to define specific frequency ranges within a [SweepSection]
     * that the Valentine One will report detected alerts.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities
     * (e.g., V1 Gen2 or V1.8 with appropriate firmware).
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One.
     *                Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing a list of [SweepDefinition] objects if the request is
     *   successful. The list may be empty if no custom sweeps are defined.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting sweep
     *   definitions (e.g., older models).
     *
     * @see SweepDefinition For the structure of each custom sweep definition.
     * @see writeSweepDefinitions To program new custom sweeps to the V1.
     * @see restoreDefaultSweeps To revert to the factory default sweep settings.
     * @see requestDefaultSweepDefinitions To get the factory default sweep definitions.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqAllSweepDefinitions For the underlying ESP request.
     */
    suspend fun requestSweepDefinitions(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<List<SweepDefinition>, ESPFailure>

    /**
     * Requests the connected Valentine One to restore its default sweep settings.
     *
     * This command instructs the V1 to revert any custom sweep configurations to their
     * factory default values. After this operation, the V1 will use its standard,
     * out-of-the-box frequency sweep patterns.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities
     * (e.g., V1 Gen2 or V1.8 with appropriate firmware). However, not all V1 versions that
     * support custom sweeps may support this specific command. Library users should consult
     * [V1CapabilityInfo.supportsDefaultSweepRequest] to determine if the connected V1
     * supports this functionality before calling this function.
     *
     * @param verify `true` if the client should read back the Valentine One's current sweep
     * definitions to verify they were set back to default values.
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the V1.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support restoring default
     *   sweeps (e.g., older models, models without custom sweep capabilities, or models that
     *   support custom sweeps but not this specific command).
     *
     * @see V1CapabilityInfo.supportsDefaultSweepRequest To check if the connected V1 supports this
     * command.
     * @see requestSweepDefinitions To get the current custom sweep definitions.
     * @see writeSweepDefinitions To program new custom sweeps to the V1.
     * @see requestDefaultSweepDefinitions To get the factory default sweep definitions without
     * actually restoring them.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     */
    suspend fun restoreDefaultSweeps(
        verify: Boolean = true,
        timeout: Duration = defaultLongRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the maximum sweep index supported by the connected Valentine One.
     *
     * The maximum sweep index indicates the largest index in the "array" of [SweepDefinition] that
     * can be programmed into the V1. This value can vary depending on the V1 model and its
     * firmware.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities.
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One. Defaults
     * to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing an [Int] representing the maximum sweep index if the
     *   request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting the
     *   maximum sweep index.
     *
     * @see requestSweepDefinitions To get the current custom sweep definitions.
     * @see writeSweepDefinitions To program new custom sweeps.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqMaxSweepIndex For the underlying ESP request.
     */
    suspend fun requestMaxSweepIndex(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Int, ESPFailure>

    /**
     * Requests the current list of custom [SweepSection] from the connected Valentine One.
     *
     * Sweep sections are the fundamental building blocks of custom sweeps. Each section defines a
     * frequency range that the Valentine One can sweep. A [SweepDefinition] is a user configurable
     * ranges within the sweep sections (end-inclusive) that the Valentine One will
     * report detected alerts.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities
     * (e.g., V1 Gen2 or V1.8 with appropriate firmware).
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One.
     *                Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing a list of [SweepSection] objects if the request is
     *   successful. The list may be empty if no custom sweeps are defined or if the V1
     *   doesn't support this specific request type (some older custom sweep V1s might only
     *   support [requestSweepDefinitions]).
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting sweep
     *   sections (e.g., older models or models without custom sweep capabilities).
     *
     * @see SweepSection For the structure of each sweep section.
     * @see requestSweepDefinitions To get the higher-level custom sweep definitions.
     * @see writeSweepDefinitions To program new custom sweeps to the V1.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqSweepSections For the underlying ESP request.
     */
    suspend fun requestSweepSections(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<List<SweepSection>, ESPFailure>

    /**
     * Requests the factory default [SweepDefinition]s from the connected Valentine One.
     *
     * This command retrieves the set of sweep definitions that the V1 would use if its custom
     * sweeps were reset to factory defaults. This is useful for understanding the baseline
     * behavior or for providing a starting point for users creating their own custom sweeps.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities
     * (e.g., V1 Gen2 or V1.8 with appropriate firmware).
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One. Defaults
     * to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing a list of [SweepDefinition] objects representing the
     *   factory default sweeps if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting default
     *   sweep definitions.
     *
     * @see SweepDefinition For the structure of each sweep definition.
     * @see requestSweepDefinitions To get the currently programmed custom sweep definitions.
     * @see restoreDefaultSweeps To actually reset the V1's sweeps to their factory defaults.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqDefaultSweepDefinitions For the underlying ESP request.
     */
    suspend fun requestDefaultSweepDefinitions(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<List<SweepDefinition>, ESPFailure>

    /**
     * Requests the current [SweepData] from the connected Valentine One.
     *
     * This command is typically only supported by V1 devices that have custom sweep capabilities
     * (e.g., V1 Gen2 or V1.8 with appropriate firmware).
     *
     * Due to the potentially larger size of sweep data and the time it might take for the V1 to
     * compile and send it, this request uses a longer default timeout
     * ([defaultLongRequestTimeout]).
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One.
     * Defaults to [defaultLongRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [SweepData] if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting sweep data.
     *
     * @see SweepData For the structure of the sweep data information.
     * @see requestSweepDefinitions To get the custom sweep definitions.
     * @see requestSweepSections To get the underlying sweep sections.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultLongRequestTimeout For the default timeout value used for this request.
     */
    suspend fun requestSweepData(
        timeout: Duration = defaultLongRequestTimeout,
    ): ESPResponse<SweepData, ESPFailure>
    //endregion

    //region Display and Audio
    /**
     * Set the display to be on or off.
     *
     * _Important information about changing the display state:_ By default, all Valentine Ones turn
     * off the main display when a Remote Display is connected and turn it back on when the
     * Concealed Display is disconnected. The reqTurnOffMainDisplay and reqTurnOnMainDisplay packets
     * change the current display state, but do not disable this feature.
     *
     * @param on `true` to turn the display on, `false` to turn it off.
     * @param timeout The maximum duration to wait for a response from the ESP device. Defaults
     *   to [defaultRequestTimeout].
     * @return An [ESPResponse] object. If the operation is successful, the `success` field will
     *   contain `Unit`. If it fails, the `failure` field will hold an [ESPFailure] object detailing
     *   the error.
     *
     * @see turnOnMainDisplay For a convenience function to specifically turn on the Valentine One's
     * main display.
     * @see turnOffMainDisplay For a convenience function to specifically turn off the Valentine
     * One's main display.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqTurnOnMainDisplay For the underlying ESP request.
     * @see ESPPacketId.ReqTurnOffMainDisplay For the underlying ESP request.
     */
    suspend fun setMainDisplay(
        on: Boolean,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Request the connected Valentine One to turn on it's main display.
     *
     * _Important information about changing the display state:_ By default, all Valentine Ones turn
     * off the main display when a Remote Display is connected and turn it back on when the
     * Concealed Display is disconnected. The reqTurnOffMainDisplay and reqTurnOnMainDisplay packets
     * change the current display state, but do not disable this feature.
     *
     * It's a convenience function that internally calls [setMainDisplay] with `on = true`.
     *
     * @param timeout The maximum duration to wait for a response from the V1. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the V1 display was successfully turned on.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support toggling it's display state.
     *
     * @see setMainDisplay For a convenience function to specifically turn off the Valentine One's
     * main display.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqTurnOnMainDisplay For the underlying ESP request.
     * @see ESPPacketId.ReqTurnOffMainDisplay For the underlying ESP request.
     */
    suspend fun turnOnMainDisplay(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Request the connected Valentine One to turn off it's main display.
     *
     * _Important information about changing the display state:_ By default, all Valentine Ones turn
     * off the main display when a Remote Display is connected and turn it back on when the
     * Concealed Display is disconnected. The reqTurnOffMainDisplay and reqTurnOnMainDisplay packets
     * change the current display state, but do not disable this feature.
     *
     * It's a convenience function that internally calls [setMainDisplay] with `on = false`.
     *
     * @param timeout The maximum duration to wait for a response from the V1. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the V1 display was successfully turned off.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support toggling its display state.
     *
     * @see turnOnMainDisplay For a convenience function to specifically turn on the Valentine One's
     * main display.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqTurnOnMainDisplay For the underlying ESP request.
     * @see ESPPacketId.ReqTurnOffMainDisplay For the underlying ESP request.
     */
    suspend fun turnOffMainDisplay(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the connected Valentine One mutes or unmutes it's audio.
     *
     * This function sends a request to the Valentine One to either mute all alerts in its alert
     * table (including Laser) or unmute alerts not muted by its internal logic.
     *
     * @param muted `true` to mute the Valentine One, `false` to unmute it.
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the V1 was successfully muted or unmuted.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support remote muting/unmuting.
     *
     * @see unmute For a convenience function to specifically unmute the Valentine One.
     * @see isSoft For a flow indicating the V1's current mute status.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqMuteOn For the underlying ESP request.
     * @see ESPPacketId.ReqMuteOff For the underlying ESP request.
     */
    suspend fun mute(
        muted: Boolean,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the connected Valentine One mutes it's audio.
     *
     * This function sends a request to the Valentine One to mute all alerts in its alert table
     * (including Laser).
     *
     * It's a convenience function that internally calls [mute] with `muted = true`.
     *
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the V1 was successfully muted.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support remote muting.
     *
     * @see mute To control both muting and unmuting with a boolean parameter.
     * @see unmute To explicitly unmute the Valentine One.
     * @see isSoft For a flow indicating the V1's current mute status.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqMuteOn For the underlying ESP request.
     */
    suspend fun mute(timeout: Duration = defaultRequestTimeout): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the connected Valentine One unmutes it's audio.
     *
     * This function sends a request to the Valentine One to mute all alerts in its alert table
     * (including Laser) that were not muted by it's internal logic.
     *
     * This is a convenience function that calls [mute] with `muted = false`.
     *
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the V1 was successfully unmuted.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed (e.g.,
     *   timeout, device not connected).
     *
     * @see mute To control both muting and unmuting with a boolean parameter.
     * @see isSoft For a flow indicating the V1's current mute status.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqMuteOff For the underlying ESP request.
     */
    suspend fun unmute(timeout: Duration = defaultRequestTimeout): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the connected Valentine One changes it's operating mode (e.g., All Bogeys, Logic,
     * Advanced Logic).
     *
     * This function sends a request to the V1 to switch to the specified `mode`.
     * It can optionally verify that the mode change was successful by reading back the V1's
     * current mode from its display data.
     *
     * @param mode The desired [V1Mode] to set on the Valentine One.
     * @param timeout The maximum duration to wait for the mode change to complete (and be
     * verified, if `verifyMode` is `true`). Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the mode was successfully changed (and verified,
     *   if applicable).
     * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed. Possible
     *   failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond or confirm the mode change within
     *     the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support mode changes via ESP.
     *   - [ESPFailure.ESPOperationFailed]: If `verifyMode` is `true` and the V1's mode did not
     *     change to the requested `mode`.
     *
     * @see V1Mode For the different operating modes available.
     * @see infDisplayDataMode For a flow of the V1's current operating mode.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqChangeMode For the underlying ESP request.
     */
    suspend fun changeMode(
        mode: V1Mode,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests the current main and muted volume levels from the Valentine One.
     *
     * This function is only intended for use with the V1 Gen2. For older V1 models, this
     * request is not be supported.
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One.
     *                Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing a [V1Volume] object with the current main and muted
     *   volume levels if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting volume levels
     *   (e.g., older V1 models).
     *
     * @see V1Volume For the structure of the volume information returned.
     * @see writeVolume To set new volume levels on the V1.
     * @see displayCurrentVolume To command the V1 to display its current volume.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqCurrentVolume For the underlying ESP request.
     * @since V4.1026
     */
    suspend fun requestCurrentVolume(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<V1Volume, ESPFailure>

    /**
     * Request both the current and saved volume levels from the Valentine One.
     *
     * This function is only intended for use with the V1 Gen2. For older V1 models, this
     * request is not be supported.
     *
     * @param timeout The maximum duration to wait for a response from the Valentine One.
     *                Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing a [V1Volumes] object with the current & saved main and
     * muted volume levels if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support requesting volume levels
     *   (e.g., older V1 models).
     *
     * @see V1Volume For the structure of the volume information.
     * @see V1Volumes For the structure of the current and saved volume information returned.
     * @see writeVolume To set new volume levels on the V1.
     * @see displayCurrentVolume To command the V1 to display its current volume.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqCurrentVolume For the underlying ESP request.
     * @since V4.1026
     */
    suspend fun requestAllVolumes(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<V1Volumes, ESPFailure>

    /**
     * Updates the main and muted volume levels inside of the Valentine One Gen2.
     *
     * This function is safe to call in rapid succession. The Valentine One will overwrite any
     * existing [ESPPacketId.ReqWriteVolume] commands in its processing queue to ensure that
     * only the last requested volume setting is applied. This prevents unintended intermediate
     * volume changes if multiple requests are sent quickly.
     *
     * The behavior of audio and visual feedback from the Valentine One Gen2 during this operation
     * depends on the `provideUserFeedback` and `skipFeedbackWhenNoChange` parameters, as well as
     * the current alert status:
     *
     * - **Feedback Control ([provideUserFeedback]):**
     *   - If `true`, the V1 will provide audio and visual feedback for the volume change.
     *   - If both main and muted volume levels are changed, the V1's feedback will typically
     *     toggle to indicate both new levels (e.g., displaying main volume then mute volume).
     *   - If `false`, no feedback will be provided, regardless of volume changes (unless an alert
     *     is active, see below).
     *
     * - **Skipping Feedback on No Change ([skipFeedbackWhenNoChange]):**
     *   - If `true` and `provideUserFeedback` is also `true`, feedback will be skipped if the new
     *     `volume` levels are identical to the V1's current volume levels.
     *   - If `false` (or if `provideUserFeedback` is `false`), this parameter has no effect.
     *
     * - **Behavior During an Alert:**
     *   - If an alert is actively being displayed by the Valentine One, the `provideUserFeedback`
     *     flag is ignored. The audio level will change *immediately* to reflect the new `volume`
     *     setting.
     *
     * @param volume The [V1Volume] object containing the desired main and muted volume levels
     *               to write to the Valentine One.
     * @param provideUserFeedback Enables visual & audio feedback.
     * @param skipFeedbackWhenNoChange Skips visual & audio feedback when written volume is the same
     * as the current volume.
     * @param saveVolume `true` if the written value should be persisted through power cycling the
     * V1. If `false` the last saved volume will be used after the next power cycle.
     * @see ESPPacketId.ReqWriteVolume For the underlying ESP request.
     * @see requestCurrentVolume To retrieve the current volume levels programmatically.
     */
    suspend fun writeVolume(
        volume: V1Volume,
        provideUserFeedback: Boolean,
        skipFeedbackWhenNoChange: Boolean,
        saveVolume: Boolean = false,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests that the connected Valentine One stops waiting for silent period. This will
     * result in the Valentine One playing audio for the primary alert sooner than it normally
     * would.
     *
     * This function is only intended for use with the V1 Gen2. For older V1 models, this
     * request is not be supported.
     *
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the V1.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support this command
     *   (e.g., older V1 models).
     *
     * @since V4.1035
     * @see ESPPacketId.ReqAbortAudioDelay For the underlying ESP request.
     */
    suspend fun abortAudioDelay(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Requests that the connected Valentine One Gen2 display is current volume.
     *
     * This action is equivalent to tapping one of the volume buttons on a Valentine One Gen2.
     * The behavior depends on the V1's current state:
     * - If no alert is present, the main volume level will be displayed.
     * - If an alert is currently active, the volume displayed will be either the main volume or
     *   the mute volume, depending on the V1's mute status at that moment.
     *
     * This function is only intended for use with the V1 Gen2. For older V1 models, this
     * request is not be supported.
     *
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the V1.
     * - [ESPResponse.Failure] containing an [ESPFailure] if the request fails or times out.
     *   Possible failures include:
     *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
     *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
     *   - [ESPFailure.NotSupported]: The connected V1 does not support this command
     *   (e.g., older V1 models).
     *
     * @since V4.1036
     * @see requestCurrentVolume To retrieve the current volume levels programmatically.
     * @see writeVolume To set new volume levels on the V1.
     * @see ESPPacketId.ReqDisplayCurrentVolume For the underlying ESP request.
     */
    suspend fun displayCurrentVolume(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>
    //endregion

    //region Alert Output
    /**
     * Enables or disables the transmission of alert table data from the Valentine One.
     *
     * When enabled, the V1 will send [ESPPacketId.RespAlertData] packets containing detailed
     * information about detected alerts. This data can be observed via the [alertTable] or
     * [alertTableClosable] flows.
     *
     * @param enable `true` to enable alert tables transmission, `false` to disable them.
     * @param timeout The maximum duration to wait for the Valentine One to acknowledge
     *                the command. Defaults to [defaultRequestTimeout].
     *
     * @return An [ESPResponse] indicating the outcome:
     * - [ESPResponse.Success] with `Unit` on success.
     * - [ESPResponse.Failure] with an [ESPFailure] on error (e.g., timeout, device not connected).
     *
     * @see alertTable For a flow of the current alert data.
     * @see alertTableClosable For a closable flow of the current alert data.
     * @see ESPPacketId.ReqStartAlertData For the underlying ESP request to enable alert tables.
     * @see ESPPacketId.ReqStopAlertData For the underlying ESP request to disable alert tables.
     */
    suspend fun enableAlertTable(
        enable: Boolean,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>
    //endregion

    //region Miscellaneous
    /**
     * Requests the battery voltage from the connected Valentine One.
     * This command is typically supported by these accessories, not directly by the Valentine One.
     *
     * The voltage is returned as a formatted string (e.g., "12.5V").
     *
     * @param timeout The maximum duration to wait for a response from the accessory. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the battery voltage as a [String] if the request is
     * successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     * [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails or
     * times out.
     *
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqBatteryVoltage For the underlying ESP request.
     */
    suspend fun requestBatteryVoltage(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<String, ESPFailure>
    //endregion

    //region SAVVY Specific
    /**
     * Requests the current status of the connected SAVVY.
     *
     * SAVVY is an accessory for the Valentine One that automatically adjusts the mute threshold
     * based on vehicle speed. This function retrieves its current operational status,
     * including whether it's enabled, the current speed threshold, and if it's currently
     * overriding the V1's mute.
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the [SAVVYStatus] if the request is successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present/responding.
     *
     * @see SAVVYStatus For the structure of the status information returned.
     * @see requestVehicleSpeed To get the current vehicle speed as reported by SAVVY.
     * @see overrideSAVVYThumbWheel To manually set the SAVVY's speed threshold.
     * @see unmuteSAVVY To control SAVVY's unmuting behavior.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqSavvyStatus For the underlying ESP request.
     */
    suspend fun requestSAVVYStatus(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<SAVVYStatus, ESPFailure>

    /**
     * Requests the current vehicle speed from the connected SAVVY.
     *
     * The speed is typically reported in MPH or KM/H, depending on the SAVVY's configuration,
     * but this function returns it as a raw integer value.
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] containing the vehicle speed as an [Int] if the request is
     *   successful.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present.
     *
     * @see requestSAVVYStatus To get the overall status of the SAVVY.
     * @see overrideSAVVYThumbWheel To manually set the SAVVY's speed threshold.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see ESPPacketId.ReqVehicleSpeed For the underlying ESP request.
     */
    suspend fun requestVehicleSpeed(
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Int, ESPFailure>

    /**
     * Overrides the SAVVY's thumbwheel setting with a specific speed value.
     *
     * The SAVVY device expects the speed threshold to be in **Kilometers Per Hour (KPH)**.
     *
     * - A `speed` value of `0` (**None**) disables SAVVY muting functionality, bypassing the SAVVY
     * thumbwheel value. Equivalent to [SAVVYThumbwheelOverride.None])
     * - A `speed` value of `255`  (**Auto**) enables muting at all speeds, bypassing the SAVVY
     * thumbwheel value. Equivalent to [SAVVYThumbwheelOverride.Auto])
     * - Any other `speed` value (`1-254` KPH) sets a custom speed threshold. Equivalent to
     * [SAVVYThumbwheelOverride.Custom])
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param speed The desired speed threshold in KPH.
     *  - `0` (****): Disables SAVVY override (equivalent to `None`) and disables SAVVY muting.
     *  - `255` (**Auto**): Sets SAVVY to Auto mode.
     *  - `1-254`: Sets a custom speed threshold in KPH. Values outside this range for custom speed
     *  might lead to unexpected behavior.
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the SAVVY.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present.
     *
     * @see SAVVYThumbwheelOverride For the different override states.
     * @see requestSAVVYStatus To get the current status of SAVVY, including its thumbwheel setting.
     */
    suspend fun overrideSAVVYThumbWheel(
        speed: Int,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure> = overrideSAVVYThumbWheel(
        speed = speed.toByte(),
        timeout = timeout,
    )

    /**
     * Overrides the SAVVY's thumbwheel setting with a specific speed value.
     *
     * The SAVVY device expects the speed threshold to be in **Kilometers Per Hour (KPH)**.
     *
     * - A `speed` value of `0` (**None**) disables SAVVY muting functionality, bypassing the SAVVY
     * thumbwheel value. Equivalent to [SAVVYThumbwheelOverride.None])
     * - A `speed` value of `255`  (**Auto**) enables muting at all speeds, bypassing the SAVVY
     * thumbwheel value. Equivalent to [SAVVYThumbwheelOverride.Auto])
     * - Any other `speed` value (`1-254` KPH) sets a custom speed threshold. Equivalent to
     * [SAVVYThumbwheelOverride.Custom])
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param speed The desired speed threshold in KPH.
     *  - `0` (****): Disables SAVVY override (equivalent to `None`) and disables SAVVY muting.
     *  - `255` (**Auto**): Sets SAVVY to Auto mode.
     *  - `1-254`: Sets a custom speed threshold in KPH. Values outside this range for custom speed
     *  might lead to unexpected behavior.
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the SAVVY.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present.
     *
     * @see SAVVYThumbwheelOverride For the different override states.
     * @see requestSAVVYStatus To get the current status of SAVVY, including its thumbwheel setting.
     */
    suspend fun overrideSAVVYThumbWheel(
        speed: Byte,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure> = overrideSAVVYThumbWheel(
        override = when (speed) {
            emptyByte -> SAVVYThumbwheelOverride.None
            fullByte -> SAVVYThumbwheelOverride.Auto
            else -> SAVVYThumbwheelOverride.Custom(speed)
        },
        timeout = timeout,
    )

    /**
     * Overrides the SAVVY's thumbwheel setting based on the provided [SAVVYThumbwheelOverride].
     *
     * The SAVVY device expects the speed threshold to be in **Kilometers Per Hour (KPH)**
     * when a custom value is provided.
     *
     * - [SAVVYThumbwheelOverride.None]: Disables SAVVY muting functionality, bypassing the
     *   SAVVY thumbwheel value. Corresponds to a speed value of `0`.
     * - [SAVVYThumbwheelOverride.Auto]: Enables muting at all speeds, bypassing the SAVVY
     *   thumbwheel value. Corresponds to a speed value of `255`.
     * - [SAVVYThumbwheelOverride.Custom]: Sets a specific speed threshold. The provided `speed`
     *   value (1-254 KPH) will be used.
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param override The [SAVVYThumbwheelOverride] state to set on the SAVVY.
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the SAVVY.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present.
     *
     * @see SAVVYThumbwheelOverride For the different override states and their meanings.
     * @see requestSAVVYStatus To get the current status of SAVVY, including its thumbwheel setting.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     */
    suspend fun overrideSAVVYThumbWheel(
        override: SAVVYThumbwheelOverride,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Controls the unmuting behavior of an attached SAVVY device.
     *
     * SAVVY can automatically unmute the Valentine One when the vehicle's speed drops below
     * a certain threshold. This function allows enabling or disabling this automatic unmuting
     * feature of the attached SAVVY.
     *
     * This command is only relevant if a SAVVY is attached to the ESP bus.
     *
     * @param enableUnmuting `true` to enable the SAVVY's unmuting functionality, `false` to disable
     * it.
     * @param timeout The maximum duration to wait for a response from the SAVVY. Defaults to
     * [defaultRequestTimeout].
     * @return An [ESPResponse] which will be:
     * - [ESPResponse.Success] with `Unit` if the command was successfully sent and acknowledged
     *   by the SAVVY.
     * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
     *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
     *   times out, or no SAVVY is present/responding.
     *
     * @see requestSAVVYStatus To get the current status of SAVVY, including its unmuting state.
     * @see overrideSAVVYThumbWheel To set the speed threshold for SAVVY.
     * @see ESPResponse For the sealed class representing the outcome of ESP operations.
     * @see ESPFailure For possible error types.
     * @see defaultRequestTimeout For the default timeout value.
     * @see [ESPPacketId.ReqSetSavvyUnmuteEnable] For the underlying ESP request.
     */
    suspend fun unmuteSAVVY(
        enableUnmuting: Boolean,
        timeout: Duration = defaultRequestTimeout,
    ): ESPResponse<Unit, ESPFailure>
    //endregion

    companion object {
        /**
         * Initializes the ESP Library. This should be called as soon as possible.
         * Safe to call multiple times.
         */
        fun init(
            espContext: ESPContext,
            loggingEnabled: Boolean = false,
        ): Unit =
            ESPIsolatedKoinContext.init(espContext = espContext, loggingEnabled = loggingEnabled)

        fun enableLogging(enabled: Boolean) {
            ESPIsolatedKoinContext
                .koin
                .get<PlatformLogger>()
                .enabled = enabled
        }

        /**
         * This is a helper function to get an ESPClient instance
         *
         * @param connection The connection to use
         * @param scope The scope to use
         *
         * @return The ESPClient instance
         */
        internal fun getClient(
            connection: IConnection,
            scope: CoroutineScope = defaultESPScope,
        ): IESPClient = ESPClient(
            initialConnection = connection,
            espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
            flowController = ESPIsolatedKoinContext.koin.get<ESPFlowController>(),
            preferencesManager = ESPIsolatedKoinContext.koin.get<IESPPreferencesManager>(),
            scope = scope,
        )

        /**
         * Creates an ESPClient for the given [ESPContext] using a default implementation of
         * [IConnection] determined by [connectionType].
         *
         * This version of the function is intended for internal use only and uses the default scope
         * [defaultESPScope].
         *
         * @param connectionType The type of connection to establish (e.g., V1cType).
         * @param connectionScope The CoroutineScope in which the client will operate. Defaults to
         * [defaultESPScope].
         * @return An instance of [IESPClient] configured for the specified context and connection
         * type.
         */
        internal fun getClient(
            connectionType: V1cType,
            connectionScope: CoroutineScope = defaultESPScope,
        ): IESPClient = getClient(
            scope = connectionScope,
            connection = getConnection(
                espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
                connectionScope = connectionScope,
                connType = connectionType
            ),
        )

        /**
         * Get the client for the given [ESPContext] and [V1ConnectionTypePreference].
         *
         * This is a helper function that will create the client based on the preference.
         *
         * @param preference The preference to use.
         * @param connectionScope The CoroutineScope to use.
         * @return The client.
         */
        fun getClient(
            preference: V1ConnectionTypePreference,
            connectionScope: CoroutineScope = defaultESPScope,
        ): IESPClient = getClient(
            connectionType = preference.resolveConnectionType(
                espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
            ),
            connectionScope = connectionScope,
        )

        /**
         * Get a demo client.
         *
         * @param scope The coroutine scope to use.
         * @return The demo client.
         */
        @Suppress("unused")
        fun getDemoClient(
            scope: CoroutineScope = defaultESPScope,
        ): IESPClient = getClient(
            connection = getConnection(
                espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
                connectionScope = scope,
                connType = V1cType.Demo,
            ),
            scope = scope,
        )

        fun isBluetoothSupported(): Boolean = ESPIsolatedKoinContext.koin.get<ESPContext>()
            .isBluetoothSupported()

        suspend fun querySystemBluetoothSupport(): Boolean =
            ESPIsolatedKoinContext.koin.get<IBluetoothManager>().checkIsBluetoothSupported()

        suspend fun querySystemBluetoothLESupport(): Boolean =
            ESPIsolatedKoinContext.koin.get<IBluetoothManager>().checkIsBluetoothLESupported()
    }
}

/**
 * This enum determines if the client should attempt a connection using a LE or Legacy (RFCOMM/SPP)
 * [V1connection]. This enum
 *
 *
 * @see V1cType
 */
enum class V1ConnectionTypePreference {
    /**
     *  Auto connect with best connection type based on available Bluetooth technology
     */
    Auto,

    /**
     * Auto connect using Bluetooth LE technology (V1c LE).
     */
    LE,

    /**
     *  Auto connect using Bluetooth SPP technology (V1c).
     */
    Legacy
}

/**
 * This enum determines how to establish a connection with the V1connection.
 */
enum class ConnectionStrategy {
    /**
     * Attempts to connect to the first [V1connection] we find. Minimizes connection delays.
     */
    First,

    /**
     * Attempts to connect to the [V1connection] with the strongest RSSI (average).
     * NOTE: V1cakes at least 5 seconds to establish a connection.
     */
    Strongest,

    /**
     * Attempts to connect to the last connected [V1connection]. If that device is not available,
     * the connection process fails.
     */
    Last,

    /**
     * Attempts to reconnect to the last [V1connection]. If the last [V1connection] is not available,
     * or there isn't one a connection with the [V1connection] with the strongest RSSI ( (average))
     * is attempted.
     */
    LastThenStrongest,
}

/**
 * Determines the V1 connection type ([V1cType]) based on the current
 * [V1ConnectionTypePreference] and the device's Bluetooth capabilities.
 *
 * This function is an extension on [V1ConnectionTypePreference].
 *
 * @param context The [ESPContext] used to check Bluetooth support (BT classic and LE).
 * @return The resolved [V1cType] (e.g., [V1cType.LE], [V1cType.Legacy], or [V1cType.Demo] if BT is
 * not supported).
 *
 * @see V1ConnectionTypePreference for possible preference values.
 * @see V1cType for possible connection type outcomes.
 * @see ESPContext.isBluetoothSupported for checking Bluetooth classic support.
 * @see ESPContext.isBluetoothLESupported for checking Bluetooth Low Energy support.
 */
internal fun V1ConnectionTypePreference.resolveConnectionType(espContext: ESPContext): V1cType =
    with(espContext) {
        when (this@resolveConnectionType) {
            V1ConnectionTypePreference.Auto -> {
                when {
                    !isBluetoothSupported() -> V1cType.Demo
                    !isBluetoothLESupported() -> V1cType.Legacy
                    else -> V1cType.LE
                }
            }

            V1ConnectionTypePreference.LE -> V1cType.LE
            V1ConnectionTypePreference.Legacy -> V1cType.Legacy
        }
    }


/**
 * Retrieves an appropriate [IConnection] instance based on the specified [V1cType]
 * and the current device's Bluetooth capabilities.
 *
 * This function is an extension on [CoroutineScope] and is intended to be called
 * within a coroutine. The provided [CoroutineScope] (`this`) is passed down to the
 * connection creation methods.
 *
 * @param connType The desired V1 connection type ([V1cType]) to establish.
 * @param connectionScope
 * @param connType
 * @return An [IConnection] implementation corresponding to the requested `connType`
 *         and supported device capabilities.
 * @throws BTUnsupported If [V1cType.Legacy] is requested but Bluetooth Classic is not supported
 *                       on the device.
 * @throws LeUnsupported If [V1cType.LE] is requested but Bluetooth Low Energy is not supported
 *                       on the device.
 *
 * @see V1cType For the different types of connections that can be requested.
 * @see IConnection The interface for the connection that will be returned.
 */
internal expect fun getConnection(
    espContext: ESPContext,
    connectionScope: CoroutineScope,
    connType: V1cType,
): IConnection

val defaultScanDuration: Duration = 5000.milliseconds
val defaultRequestTimeout: Duration = 2500.milliseconds
val defaultLongRequestTimeout: Duration = 5000.milliseconds