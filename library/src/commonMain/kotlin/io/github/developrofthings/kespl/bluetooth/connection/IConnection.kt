package io.github.developrofthings.kespl.bluetooth.connection

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.demo.DemoConnection
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.packet.ESPRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Interface defining the contract for a connection to an ESP (Extended Serial Protocol) device.
 *
 * This interface provides properties and functions to manage the connection lifecycle,
 * facilitate data exchange, and observe the real-time status and hardware details of the
 * connected Valentine One device.
 */
interface IConnection {
    /**
     * Controls whether the connection should track echoed ESP data.
     *
     * When enabled, the connection maintains a queue of outgoing requests to match
     * against incoming echo responses from the ESP bus.
     */
    var canEchoQueue: Boolean

    /**
     * The [CoroutineScope] associated with this connection.
     * This scope is used to manage asynchronous tasks related to the connection's lifecycle,
     * such as data processing and monitoring.
     */
    val connectionScope: CoroutineScope

    /**
     * A [StateFlow] representing the current [ESPConnectionStatus] of the connection.
     */
    val connectionStatus: StateFlow<ESPConnectionStatus>

    /**
     * A [StateFlow] representing the currently connected [V1connection].
     *
     * A `null` value indicates that the [IConnection] is not currently connected to a device.
     */
    val connectedDevice: StateFlow<V1connection?>

    /**
     * A [Flow] of raw ESP data bytes received from the connected [V1connection].
     */
    val espData: Flow<ByteArray>

    /**
     * A [Flow] of 'No Data' notifications that are emitted after [staleDataWatchDogTimeout]
     * has passed without any ESP data being received from the connected [V1connection].
     */
    val noData: Flow<Unit>

    /**
     * A [Flow] of notifications received from the connected [V1connection].
     *
     * Note: this is presently only used by [DemoConnection].
     */
    val notificationData: Flow<String>

    /**
     * A [StateFlow] indicating whether the Valentine One has enabled time-slicing on the ESP bus.
     *
     * When active, devices attached to the bus are only permitted to communicate during their
     * respective assigned time-slices. This state is determined by observing the `TSHoldOff`
     * bit within `InfDisplayData` packets.
     *
     * @see io.github.developrofthings.kespl.packet.data.displayData.isTimeSlicing
     * @see io.github.developrofthings.kespl.packet.data.displayData.DisplayData.isTimeSlicing
     */
    val isTimeSlicing: StateFlow<Boolean>

    /**
     * A [StateFlow] representing the firmware version of the connected Valentine One device.
     *
     * This value is determined through the ESP protocol and provides the specific
     * version number (e.g., 3.8945) of the hardware.
     */
    val v1Version: StateFlow<Double>

    /**
     * A [StateFlow] representing the specific [ESPDevice.ValentineOne] hardware type.
     *
     * The type is determined based on the device's capabilities, such as support for ESP and or
     * checksums. __Note:__ the Valentine One may support checksums but if operating in "Legacy" the
     * library will report [ESPDevice.ValentineOne.Legacy].
     */
    val v1Type: StateFlow<ESPDevice.ValentineOne>

    /**
     * The type of the connection, indicating the underlying communication protocol.
     *
     * This property specifies whether the connection is Bluetooth LE, legacy Bluetooth,
     * or a demonstration/mock connection, as defined by the [V1cType] enum.
     */
    val connectionType: V1cType

    /**
     * Indicates whether the firmware version of the connected Valentine One device has been determined.
     */
    val hasV1Version: Boolean

    /**
     * Indicates whether the connection has successfully identified the [ESPDevice.ValentineOne] of
     * the connected Valentine One.
     */
    val hasDeterminedV1Type: Boolean

    /**
     * Attempts to establish a connection with the provided [V1connection].
     *
     * @param v1c The [V1connection] to attempt a connection.
     * @param directConnect `true` if the an "immediate" connection should be attempted with [v1c];
     * attempts will timeout after a number of seconds. `false` if a connection should be
     * established as soon as the [V1connection] becomes available.
     * This argument is intended for clients wishing to attempt a connection that will not timeout
     * such as background re/connections. Under normal circumstances, the default value
     * (`true`) is the desired behavior. This argument only has an effect for [V1cType.LE]
     * connections on the **Android** platform. On iOS, Bluetooth connections attempts do not
     * timeout. To abort a connection attempt call [disconnect].
     */
    suspend fun connect(v1c: V1connection, directConnect: Boolean = true): Boolean

    /**
     * Disconnects from the connected [V1connection].
     */
    suspend fun disconnect()

    /**
     * Scans for nearby [V1connection]s based on the provided [scanMode].
     *
     * @param scanMode The [ESPScanMode] to use for scanning. This argument only has an effect for
     * [V1cType.LE] connections on the **Android** platform. Other platforms and [V1cType] this
     * argument is ignored.
     */
    suspend fun scan(scanMode: ESPScanMode = ESPScanMode.LowPower): Flow<V1connectionScanResult>

    /**
     * Attempts to write [request] to the connected [V1connection].
     * @param request The [ESPRequest] to be sent.
     * @param waitForV1TypeDuration The duration to wait for the V1 type to be determined.
     *
     * @return [ESPResponse] indicating the success or failure of the request.
     */
    suspend fun writeRequest(
        request: ESPRequest,
        waitForV1TypeDuration: Duration = defaultWriteTimeout,
    ): ESPResponse<Unit, ESPFailure>

    /**
     * Filters a list of [V1connection]s that are compatible with [connectionType].
     */
    fun filterDevices(devices: List<V1connection>): List<V1connection> = devices.filter { v1c ->
        v1c.type == this@IConnection.connectionType
    }
}

/**
 * The default duration to wait for a write operation or prerequisite state (such as
 * determining the V1 type) to complete before timing out.
 */
val defaultWriteTimeout: Duration = 2000.milliseconds
/**
 * The duration of inactivity allowed before the connection is considered to have "no data."
 *
 * If no ESP data is received from the connected device within this timeframe,
 * a notification is emitted via the [IConnection.noData] flow.
 */
val staleDataWatchDogTimeout: Duration = 1500.milliseconds

internal const val LE_CONNECTION_QUALIFIER: String = "LE_CONNECTION_QUALIFIER"

internal const val LEGACY_CONNECTION_QUALIFIER: String = "LEGACY_CONNECTION_QUALIFIER"

internal const val DEMO_CONNECTION_QUALIFIER: String = "DEMO_CONNECTION_QUALIFIER"