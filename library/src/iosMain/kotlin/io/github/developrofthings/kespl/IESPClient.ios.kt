package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.connection.DEMO_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.connection.LE_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import io.github.developrofthings.kespl.packet.data.SAVVYStatus
import io.github.developrofthings.kespl.packet.data.SAVVYThumbwheelOverride
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.V1Volume
import io.github.developrofthings.kespl.packet.data.V1Volumes
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.packet.data.sweep.SweepData
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import io.github.developrofthings.kespl.packet.data.user.V18UserSettings
import io.github.developrofthings.kespl.utilities.defaultESPScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import kotlin.time.Duration

//region IESPClient default arg extensions
//region Connect

/**
 * Attempts to connect to a Valentine One device using the specified connection strategy and
 * scan duration.
 *
 * @return `true` if a connection was successfully established, `false` otherwise.
 * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see ConnectionStrategy For different approaches to selecting a device.
 * @see defaultScanDuration For the default scanning time.
 */
suspend fun IESPClient.connect(): Boolean = connect(
    connectionStrategy = ConnectionStrategy.LastThenStrongest,
    scanDurationMillis = defaultScanDuration,
)

/**
 * Attempts to connect to a Valentine One device using the specified connection strategy and
 * scan duration.
 *
 * @param connectionStrategy The strategy to use when selecting a device to connect to
 * (e.g., [ConnectionStrategy.LastThenStrongest], [ConnectionStrategy.First]).
 * @return `true` if a connection was successfully established, `false` otherwise.
 * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see ConnectionStrategy For different approaches to selecting a device.
 * @see defaultScanDuration For the default scanning time.
 */
suspend fun IESPClient.connect(connectionStrategy: ConnectionStrategy): Boolean = connect(
    connectionStrategy = connectionStrategy,
    scanDurationMillis = defaultScanDuration,
)

/**
 * Attempts to connect to a Valentine One device using the specified connection strategy and
 * scan duration.
 *
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
suspend fun IESPClient.connect(scanDurationMillis: Duration): Boolean = connect(
    connectionStrategy = ConnectionStrategy.LastThenStrongest,
    scanDurationMillis = scanDurationMillis,
)

/**
 * Asynchronously attempts to connect to a [V1connection] using the specified connection
 * strategy and scan duration. This function returns immediately with a [Job] that represents
 * the asynchronous connection attempt.
 *
 * @return A [Deferred] which will resolve to `true` if the connection was successfully
 * established, or `false` otherwise. You can `await()` this Deferred to get the result, or
 * handle its completion in other ways.
 * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see ConnectionStrategy For different approaches to selecting a device.
 * @see defaultScanDuration For the default scanning time.
 * @see io.github.developrofthings.kespl.IESPClient.connectionStatus To observe the outcome of the connection attempt.
 */
fun IESPClient.connectAsync(): Deferred<Boolean> = connectAsync(
    connectionStrategy = ConnectionStrategy.LastThenStrongest,
    scanDurationMillis = defaultScanDuration,
)

/**
 * Asynchronously attempts to connect to a [V1connection] using the specified connection
 * strategy and scan duration. This function returns immediately with a [Job] that represents
 * the asynchronous connection attempt.
 *
 * @param connectionStrategy The strategy to use when selecting a device to connect to
 * (e.g., [ConnectionStrategy.LastThenStrongest], [ConnectionStrategy.First]). Defaults to
 * [ConnectionStrategy.LastThenStrongest].
 * @return A [Deferred] which will resolve to `true` if the connection was successfully
 * established, or `false` otherwise. You can `await()` this Deferred to get the result, or
 * handle its completion in other ways.
 * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see ConnectionStrategy For different approaches to selecting a device.
 * @see defaultScanDuration For the default scanning time.
 * @see io.github.developrofthings.kespl.IESPClient.connectionStatus To observe the outcome of the
 * connection attempt.
 */
fun IESPClient.connectAsync(connectionStrategy: ConnectionStrategy): Deferred<Boolean> = connectAsync(
    connectionStrategy = connectionStrategy,
    scanDurationMillis = defaultScanDuration,
)

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
 * @return A [Deferred] which will resolve to `true` if the connection was successfully
 * established, or `false` otherwise. You can `await()` this Deferred to get the result, or
 * handle its completion in other ways.
 * @throws BTUnsupported If the selected connection type requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the selected connection type requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see ConnectionStrategy For different approaches to selecting a device.
 * @see defaultScanDuration For the default scanning time.
 * @see connectionStatus To observe the outcome of the connection attempt.
 */
fun IESPClient.connectAsync(scanDurationMillis: Duration): Deferred<Boolean> = connectAsync(
    connectionStrategy = ConnectionStrategy.LastThenStrongest,
    scanDurationMillis = scanDurationMillis,
)

/**
 * Asynchronously attempts to connect to the specified [V1connection]. This function returns
 * immediately with a [Deferred] that will complete with the connection result.
 *
 * @param v1c The [V1connection] object representing the device to connect to.
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
 * @see io.github.developrofthings.kespl.IESPClient.connectionStatus To observe the ongoing status
 * of the connection attempt.
 */
fun IESPClient.connectAsync(v1c: V1connection): Deferred<Boolean> =
    connectAsync(v1c = v1c, directConnect = true)

/**
 * Attempts to connect to the specified [V1connection].
 *
 * @param v1c The [V1connection] object representing the device to connect to.
 *
 * @return `true` if the connection was successfully established, `false` otherwise.
 * @throws BTUnsupported If the connection type of `v1c` requires Bluetooth Classic and it's not
 * supported on the device.
 * @throws LeUnsupported If the connection type of `v1c` requires Bluetooth Low Energy and it's
 * not supported on the device.
 *
 * @see V1connection For details on the connection object.
 */
suspend fun IESPClient.connect(v1c: V1connection): Boolean = connect(
    v1c = v1c,
    directConnect = true,
)
//endregion

//region Device Information
/**
 * Requests the version information from the connected Valentine One.
 *
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
suspend fun IESPClient.requestV1Version(): ESPResponse<Version, ESPFailure> =
    requestV1Version(timeout = defaultRequestTimeout)

/**
 * Requests the version information from the specified ESP device.
 *
 * @param destination The [ESPDevice] from which to request the version information.
 * Supported devices typically include [ESPDevice.ValentineOne],[ESPDevice.RemoteDisplay], etc.
 *
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
suspend fun IESPClient.requestDeviceVersion(destination: ESPDevice): ESPResponse<Version, ESPFailure> =
    requestDeviceVersion(
        destination = destination,
        timeout = defaultRequestTimeout,
    )

/**
 * Requests the serial number from the connected Valentine One.
 *
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
suspend fun IESPClient.requestV1SerialNumber(): ESPResponse<SerialNumber, ESPFailure> =
    requestV1SerialNumber(
        timeout = defaultRequestTimeout
    )

/**
 * Requests the serial number from the specified ESP device.
 *
 * @param destination The [ESPDevice] from which to request the serial number. Supported devices
 * typically include [ESPDevice.ValentineOne], [ESPDevice.RemoteDisplay], etc.
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
suspend fun IESPClient.requestDeviceSerialNumber(destination: ESPDevice): ESPResponse<SerialNumber, ESPFailure> =
    requestDeviceSerialNumber(
        destination = destination,
        timeout = defaultRequestTimeout,
    )
//endregion

//region User Setup Options
/**
 * Requests the user configuration settings (user bytes) from the connected Valentine One.
 *
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
suspend fun IESPClient.requestV1UserSettings(): ESPResponse<UserSettings, ESPFailure> =
    requestV1UserSettings(
        forceVersionRequest = false,
        timeout = defaultRequestTimeout,
    )

/**
 * Requests the user configuration settings (user bytes) from the connected Valentine One.
 *
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
suspend fun IESPClient.requestV1UserSettings(timeout: Duration): ESPResponse<UserSettings, ESPFailure> =
    requestV1UserSettings(
        forceVersionRequest = false,
        timeout = defaultRequestTimeout,
    )

/**
 * Requests the user configuration settings (user bytes) from the connected Valentine One.
 *
 * @param forceVersionRequest If `true`, the client will request the V1's version
 * before requesting the user bytes. Defaults to `false`.
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
suspend fun IESPClient.requestV1UserSettings(forceVersionRequest: Boolean): ESPResponse<UserSettings, ESPFailure> =
    requestV1UserSettings(
        forceVersionRequest = forceVersionRequest,
        timeout = defaultRequestTimeout,
    )

/**
 * Requests the user configuration settings (user bytes) from the specified ESP device.
 *
 * @param destination The [ESPDevice] from which to request the user settings.
 * Only [ESPDevice.RemoteDisplay] and [ESPDevice.ValentineOne] support
 * [ESPPacketId.ReqUserBytes].
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
suspend fun IESPClient.requestUserSettings(destination: ESPDevice): ESPResponse<UserSettings, ESPFailure> =
    requestUserSettings(
        destination = destination,
        forceVersionRequest = false,
        timeout = defaultRequestTimeout,
    )

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
suspend fun IESPClient.requestUserSettings(
    destination: ESPDevice,
    forceVersionRequest: Boolean,
): ESPResponse<UserSettings, ESPFailure> = requestUserSettings(
    destination = destination,
    forceVersionRequest = forceVersionRequest,
    timeout = defaultRequestTimeout,
)

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
suspend fun IESPClient.requestUserSettings(
    destination: ESPDevice,
    timeout: Duration,
): ESPResponse<UserSettings, ESPFailure> = requestUserSettings(
    destination = destination,
    forceVersionRequest = false,
    timeout = timeout,
)

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
suspend fun IESPClient.requestUserBytes(destination: ESPDevice): ESPResponse<ByteArray, ESPFailure> =
    requestUserBytes(
        destination = destination,
        timeout = defaultRequestTimeout,
    )

/**
 * Attempts to write [userBytes] to update the user configuration settings inside the Valentine
 * One.
 *
 * @param userBytes Desired Valentine One user configuration.
 * @param verifyBytes `true` if the client should read back the Valentine One's user bytes to
 * verify the values written were set.
 * NOTE: This will fail if the [V18UserSettings.UserByte2.forceLegacyDisplayDisabled] bit is set.
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
 * @see V18UserSettings.UserByte2.forceLegacyDisplayDisabled For the specific bit that can cause verification
 * failure.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 */
suspend fun IESPClient.writeV1UserBytes(
    userBytes: ByteArray,
    verifyBytes: Boolean,
): ESPResponse<UserSettings, ESPFailure> = writeV1UserBytes(
    userBytes = userBytes,
    verifyBytes = verifyBytes,
    timeout = defaultLongRequestTimeout,
)

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
 * [V18UserSettings.UserByte2.forceLegacyDisplayDisabled] bit is set in the `userBytes`.
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
 * @see V18UserSettings.UserByte2.forceLegacyDisplayDisabled For the specific bit that can cause verification
 * failure with Valentine One devices.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 */
suspend fun IESPClient.writeUserBytes(
    destination: ESPDevice,
    userBytes: ByteArray,
): ESPResponse<UserSettings, ESPFailure> = writeUserBytes(
    destination = destination,
    userBytes = userBytes,
    verifyBytes = true,
    timeout = defaultLongRequestTimeout,
)

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
 * [V18UserSettings.UserByte2.forceLegacyDisplayDisabled] bit is set in the `userBytes`.
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
 * @see V18UserSettings.UserByte2.forceLegacyDisplayDisabled For the specific bit that can cause verification
 * failure with Valentine One devices.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 */
suspend fun IESPClient.writeUserBytes(
    destination: ESPDevice,
    userBytes: ByteArray,
    verifyBytes: Boolean,
): ESPResponse<UserSettings, ESPFailure> = writeUserBytes(
    destination = destination,
    userBytes = userBytes,
    verifyBytes = verifyBytes,
    timeout = defaultLongRequestTimeout,
)

/**
 * Attempts to write [userBytes] to update the user configuration settings inside target
 * [ESPDevice].
 *
 * @param destination The [ESPDevice] who's user configuration settings you would like to write.
 * Only [ESPDevice.ValentineOne] and [ESPDevice.RemoteDisplay] support
 * [ESPPacketId.ReqWriteUserBytes].
 * @param userBytes Desired user configuration byte array. For [ESPDevice.ValentineOne], this
 * typically represents the Valentine One's user settings.
 * NOTE: When `destination` is [ESPDevice.ValentineOne], this verification may fail if the
 * [V18UserSettings.UserByte2.forceLegacyDisplayDisabled] bit is set in the `userBytes`.
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
 * @see V18UserSettings.UserByte2.forceLegacyDisplayDisabled For the specific bit that can cause verification
 * failure with Valentine One devices.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 */
suspend fun IESPClient.writeUserBytes(
    destination: ESPDevice,
    userBytes: ByteArray,
    timeout: Duration,
): ESPResponse<UserSettings, ESPFailure> = writeUserBytes(
    destination = destination,
    userBytes = userBytes,
    verifyBytes = true,
    timeout = timeout,
)

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
suspend fun IESPClient.restoreFactoryDefaults(destination: ESPDevice): ESPResponse<Unit, ESPFailure> =
    restoreFactoryDefaults(
        destination = destination,
        verify = true,
        timeout = defaultRequestTimeout,
    )

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
suspend fun IESPClient.restoreFactoryDefaults(
    destination: ESPDevice,
    timeout: Duration,
): ESPResponse<Unit, ESPFailure> = restoreFactoryDefaults(
    destination = destination,
    verify = true,
    timeout = timeout,
)

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
suspend fun IESPClient.restoreFactoryDefaults(
    destination: ESPDevice,
    verify: Boolean,
): ESPResponse<Unit, ESPFailure> = restoreFactoryDefaults(
    destination = destination,
    verify = verify,
    timeout = defaultRequestTimeout,
)
//endregion

//region Custom Sweep
/**
 * Attempts to write specified list of [SweepDefinition] to the Valentine One.
 *
 * @param sweepDefinitions The list of sweep definitions to write.
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
suspend fun IESPClient.writeSweepDefinitions(sweepDefinitions: List<SweepDefinition>): ESPResponse<Int, ESPFailure> =
    writeSweepDefinitions(
        sweepDefinitions = sweepDefinitions,
        timeout = defaultRequestTimeout,
    )

/**
 * Requests the current list of [SweepDefinition] from the connected Valentine One.
 *
 * Sweep definitions allow users to define specific frequency ranges within a [SweepSection]
 * that the Valentine One will report detected alerts.
 *
 * This command is typically only supported by V1 devices that have custom sweep capabilities
 * (e.g., V1 Gen2 or V1.8 with appropriate firmware).
 *
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
suspend fun IESPClient.requestSweepDefinitions(): ESPResponse<List<SweepDefinition>, ESPFailure> =
    requestSweepDefinitions(timeout = defaultRequestTimeout)

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
suspend fun IESPClient.restoreDefaultSweeps(): ESPResponse<Unit, ESPFailure> = restoreDefaultSweeps(
    verify = true,
    timeout = defaultLongRequestTimeout,
)

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
suspend fun IESPClient.restoreDefaultSweeps(verify: Boolean): ESPResponse<Unit, ESPFailure> =
    restoreDefaultSweeps(
        verify = verify,
        timeout = defaultLongRequestTimeout,
    )

/**
 * Requests the maximum sweep index supported by the connected Valentine One.
 *
 * The maximum sweep index indicates the largest index in the "array" of [SweepDefinition] that
 * can be programmed into the V1. This value can vary depending on the V1 model and its
 * firmware.
 *
 * This command is typically only supported by V1 devices that have custom sweep capabilities.
 *
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
suspend fun IESPClient.requestMaxSweepIndex(): ESPResponse<Int, ESPFailure> =
    requestMaxSweepIndex(timeout = defaultRequestTimeout)

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
suspend fun IESPClient.requestSweepSections(): ESPResponse<List<SweepSection>, ESPFailure> =
    requestSweepSections(timeout = defaultRequestTimeout)

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
suspend fun IESPClient.requestDefaultSweepDefinitions(): ESPResponse<List<SweepDefinition>, ESPFailure> =
    requestDefaultSweepDefinitions(timeout = defaultRequestTimeout)

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
suspend fun IESPClient.requestSweepData(): ESPResponse<SweepData, ESPFailure> =
    requestSweepData(timeout = defaultLongRequestTimeout)
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
suspend fun IESPClient.setMainDisplay(on: Boolean): ESPResponse<Unit, ESPFailure> =
    setMainDisplay(on = on, timeout = defaultRequestTimeout)

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
suspend fun IESPClient.turnOnMainDisplay(): ESPResponse<Unit, ESPFailure> =
    turnOnMainDisplay(defaultRequestTimeout)

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
suspend fun IESPClient.turnOffMainDisplay(): ESPResponse<Unit, ESPFailure> =
    turnOffMainDisplay(defaultRequestTimeout)

/**
 * Requests the connected Valentine One mutes or unmutes it's audio.
 *
 * This function sends a request to the Valentine One to either mute all alerts in its alert
 * table (including Laser) or unmute alerts not muted by its internal logic.
 *
 * @param muted `true` to mute the Valentine One, `false` to unmute it.
 * @return An [ESPResponse] indicating the outcome:
 * - [ESPResponse.Success] with `Unit` if the V1 was successfully muted or unmuted.
 * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed.
 *   Possible failures include:
 *   - [ESPFailure.TimedOut]: The V1 did not respond within the specified `timeout`.
 *   - [ESPFailure.NotConnected]: The client isn't connected to a [V1connection].
 *   - [ESPFailure.NotSupported]: The connected V1 does not support remote muting/unmuting.
 *
 * @see unmute For a convenience function to specifically unmute the Valentine One.
 * @see io.github.developrofthings.kespl.IESPClient.isSoft For a flow indicating the V1's current mute status.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 * @see defaultRequestTimeout For the default timeout value.
 * @see ESPPacketId.ReqMuteOn For the underlying ESP request.
 * @see ESPPacketId.ReqMuteOff For the underlying ESP request.
 */
suspend fun IESPClient.mute(muted: Boolean): ESPResponse<Unit, ESPFailure> =
    mute(muted = muted, timeout = defaultRequestTimeout)

/**
 * Requests the connected Valentine One mutes it's audio.
 *
 * This function sends a request to the Valentine One to mute all alerts in its alert table
 * (including Laser).
 *
 * It's a convenience function that internally calls [mute] with `muted = true`.
 *
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
 * @see io.github.developrofthings.kespl.IESPClient.isSoft For a flow indicating the V1's current mute status.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 * @see defaultRequestTimeout For the default timeout value.
 * @see ESPPacketId.ReqMuteOn For the underlying ESP request.
 */
suspend fun IESPClient.mute(): ESPResponse<Unit, ESPFailure> = mute(defaultRequestTimeout)

/**
 * Requests the connected Valentine One unmutes it's audio.
 *
 * This function sends a request to the Valentine One to mute all alerts in its alert table
 * (including Laser) that were not muted by it's internal logic.
 *
 * This is a convenience function that calls [mute] with `muted = false`.
 *
 * @return An [ESPResponse] indicating the outcome:
 * - [ESPResponse.Success] with `Unit` if the V1 was successfully unmuted.
 * - [ESPResponse.Failure] containing an [ESPFailure] if the operation failed (e.g.,
 *   timeout, device not connected).
 *
 * @see mute To control both muting and unmuting with a boolean parameter.
 * @see io.github.developrofthings.kespl.IESPClient.isSoft For a flow indicating the V1's current mute status.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 * @see defaultRequestTimeout For the default timeout value.
 * @see ESPPacketId.ReqMuteOff For the underlying ESP request.
 */
suspend fun IESPClient.unmute(): ESPResponse<Unit, ESPFailure> = unmute(defaultRequestTimeout)

/**
 * Requests the connected Valentine One changes it's operating mode (e.g., All Bogeys, Logic,
 * Advanced Logic).
 *
 * This function sends a request to the V1 to switch to the specified `mode`.
 * It can optionally verify that the mode change was successful by reading back the V1's
 * current mode from its display data.
 *
 * @param mode The desired [V1Mode] to set on the Valentine One.
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
 * @see io.github.developrofthings.kespl.IESPClient.infDisplayDataMode For a flow of the V1's current operating mode.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 * @see defaultRequestTimeout For the default timeout value.
 * @see ESPPacketId.ReqChangeMode For the underlying ESP request.
 */
suspend fun IESPClient.changeMode(mode: V1Mode): ESPResponse<Unit, ESPFailure> =
    changeMode(mode = mode, timeout = defaultRequestTimeout)

/**
 * Requests the current main and muted volume levels from the Valentine One.
 *
 * This function is only intended for use with the V1 Gen2. For older V1 models, this
 * request is not be supported.
 *
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
suspend fun IESPClient.requestCurrentVolume(): ESPResponse<V1Volume, ESPFailure> =
    requestCurrentVolume(defaultRequestTimeout)

/**
 * Request both the current and saved volume levels from the Valentine One.
 *
 * This function is only intended for use with the V1 Gen2. For older V1 models, this
 * request is not be supported.
 *
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
suspend fun IESPClient.requestAllVolumes(): ESPResponse<V1Volumes, ESPFailure> =
    requestAllVolumes(defaultRequestTimeout)

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
 * @see ESPPacketId.ReqWriteVolume For the underlying ESP request.
 * @see requestCurrentVolume To retrieve the current volume levels programmatically.
 */
suspend fun IESPClient.writeVolume(
    volume: V1Volume,
    provideUserFeedback: Boolean,
    skipFeedbackWhenNoChange: Boolean,
): ESPResponse<Unit, ESPFailure> = writeVolume(
    volume = volume,
    provideUserFeedback = provideUserFeedback,
    skipFeedbackWhenNoChange = skipFeedbackWhenNoChange,
    saveVolume = false,
    timeout = defaultRequestTimeout
)

/**
 * Requests that the connected Valentine One stops waiting for silent period. This will
 * result in the Valentine One playing audio for the primary alert sooner than it normally
 * would.
 *
 * This function is only intended for use with the V1 Gen2. For older V1 models, this
 * request is not be supported.
 *
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
suspend fun IESPClient.abortAudioDelay(): ESPResponse<Unit, ESPFailure> =
    abortAudioDelay(defaultRequestTimeout)

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
suspend fun IESPClient.displayCurrentVolume(): ESPResponse<Unit, ESPFailure> =
    displayCurrentVolume(defaultRequestTimeout)
//endregion

//region Alert Output
/**
 * Enables or disables the transmission of alert table data from the Valentine One.
 *
 * When enabled, the V1 will send [ESPPacketId.RespAlertData] packets containing detailed
 * information about detected alerts. This data can be observed via the
 * [io.github.developrofthings.kespl.IESPClient.alertTable] or
 * [io.github.developrofthings.kespl.IESPClient.alertTableClosable] flows.
 *
 * @param enable `true` to enable alert tables transmission, `false` to disable them.
 *
 * @return An [ESPResponse] indicating the outcome:
 * - [ESPResponse.Success] with `Unit` on success.
 * - [ESPResponse.Failure] with an [ESPFailure] on error (e.g., timeout, device not connected).
 *
 * @see io.github.developrofthings.kespl.IESPClient.alertTable For a flow of the current alert data.
 * @see io.github.developrofthings.kespl.IESPClient.alertTableClosable For a closable flow of the current alert data.
 * @see ESPPacketId.ReqStartAlertData For the underlying ESP request to enable alert tables.
 * @see ESPPacketId.ReqStopAlertData For the underlying ESP request to disable alert tables.
 */
suspend fun IESPClient.enableAlertTable(
    enable: Boolean,
): ESPResponse<Unit, ESPFailure> = enableAlertTable(
    enable = enable,
    timeout = defaultRequestTimeout,
)
//endregion

//region Miscellaneous
/**
 * Requests the battery voltage from the connected Valentine One.
 * This command is typically supported by these accessories, not directly by the Valentine One.
 *
 * The voltage is returned as a formatted string (e.g., "12.5V").
 *
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
suspend fun IESPClient.requestBatteryVoltage(): ESPResponse<String, ESPFailure> =
    requestBatteryVoltage(defaultRequestTimeout)
//endregion

//region SAVVY
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
suspend fun IESPClient.requestSAVVYStatus(): ESPResponse<SAVVYStatus, ESPFailure> =
    requestSAVVYStatus(timeout = defaultRequestTimeout)

/**
 * Requests the current vehicle speed from the connected SAVVY.
 *
 * The speed is typically reported in MPH or KM/H, depending on the SAVVY's configuration,
 * but this function returns it as a raw integer value.
 *
 * This command is only relevant if a SAVVY is attached to the ESP bus.
 *
 * @return An [ESPResponse] which will be:
 * - [ESPResponse.Success] containing the vehicle speed as an [Int] if the request is
 *   successful.
 * - [ESPResponse.Failure] containing an [ESPFailure] (e.g., [ESPFailure.TimedOut],
 *   [ESPFailure.NotConnected], [ESPFailure.NotSupported]) if the request fails,
 *   times out, or no SAVVY is present.
 *
 * @see requestSAVVYStatus To get the overall status of
 * the SAVVY.
 * @see overrideSAVVYThumbWheel To manually set the SAVVY's speed threshold.
 * @see ESPResponse For the sealed class representing the outcome of ESP operations.
 * @see ESPFailure For possible error types.
 * @see defaultRequestTimeout For the default timeout value.
 * @see ESPPacketId.ReqVehicleSpeed For the underlying ESP request.
 */
suspend fun IESPClient.requestVehicleSpeed(): ESPResponse<Int, ESPFailure> =
    requestVehicleSpeed(timeout = defaultRequestTimeout)

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
suspend fun IESPClient.overrideSAVVYThumbWheel(
    speed: Int,
): ESPResponse<Unit, ESPFailure> = overrideSAVVYThumbWheel(
    speed = speed,
    timeout = defaultRequestTimeout,
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
suspend fun IESPClient.overrideSAVVYThumbWheel(
    speed: Byte,
): ESPResponse<Unit, ESPFailure> = overrideSAVVYThumbWheel(
    speed = speed,
    timeout = defaultRequestTimeout,
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
suspend fun IESPClient.overrideSAVVYThumbWheel(
    override: SAVVYThumbwheelOverride,
): ESPResponse<Unit, ESPFailure> = overrideSAVVYThumbWheel(
    override = override,
    timeout = defaultRequestTimeout
)


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
suspend fun IESPClient.unmuteSAVVY(enableUnmuting: Boolean): ESPResponse<Unit, ESPFailure> =
    unmuteSAVVY(
        enableUnmuting = enableUnmuting,
        timeout = defaultRequestTimeout,
    )
//endregion
// endregion

/**
 * Creates an ESPClient for the given [ESPContext] using a default implementation of
 * [IConnection] determined by [connectionType].
 *
 * This version of the function is intended for internal use only and uses the default scope
 * [defaultESPScope].
 *
 * @param connectionType The type of connection to establish (e.g., [V1cType.LE]).
 * [defaultESPScope].
 * @return An instance of [IESPClient] configured for the specified context and connection
 * type.
 */
fun IESPClient.Companion.getClient(connectionType: V1cType) =
    getClient(connectionType = connectionType, connectionScope = defaultESPScope)

/**
 * Get the client for the given [ESPContext] and [V1ConnectionTypePreference].
 *
 * This is a helper function that will create the client based on the preference.
 *
 * @param preference The preference to use.
 * @return The client.
 */
fun IESPClient.Companion.getClient(preference: V1ConnectionTypePreference) =
    getClient(preference = preference, connectionScope = defaultESPScope)

/**
 * Return a client setup for a "demo" connection.
 *
 * @return The demo client.
 */
fun IESPClient.Companion.getDemoClient() = getDemoClient(scope = defaultESPScope)

internal actual fun getConnection(
    espContext: ESPContext,
    connectionScope: CoroutineScope,
    connType: V1cType,
): IConnection = ESPIsolatedKoinContext.koin.get<IConnection>(
    qualifier = named(
        name = when (connType) {
            // iOS doesn't support RFCOMM/SPP Bluetooth connections
            V1cType.Legacy -> throw IOSLegacyUnsupported()
            V1cType.LE -> LE_CONNECTION_QUALIFIER
            V1cType.Demo -> DEMO_CONNECTION_QUALIFIER
        }
    )
) {
    parametersOf(connectionScope)
}