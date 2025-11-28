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
 * Interface defining the contract for a connection to an ESP device.
 * This interface provides properties and methods to manage the connection lifecycle,
 * send and receive data, and observe connection status and device information.
 */
interface IConnection {
    /**
     * Controls if the connection should track echo ESP data.
     */
    var canEchoQueue: Boolean

    val connectionScope: CoroutineScope

    val connectionStatus: StateFlow<ESPConnectionStatus>

    /**
     * Stream of ESP data received from the connected [V1connection].
     */
    val espData: Flow<ByteArray>

    /**
     * Stream of 'No Data' notifications that are emitted after [staleDataWatchDogTimeout]
     * milliseconds has passed without any ESP data being received from the connected [V1connection].
     */
    val noData: Flow<Unit>

    /**
     * Stream of notifications received from the connected [V1connection].
     * Note: this is presently only used by [DemoConnection].
     */
    val notificationData: Flow<String>

    val isTimeSlicing: StateFlow<Boolean>

    val connectionType: V1cType

    val hasV1Version: Boolean

    val v1Version: StateFlow<Double>

    val hasDeterminedV1Type: Boolean

    val v1Type: StateFlow<ESPDevice.ValentineOne>

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

val defaultWriteTimeout: Duration = 2000.milliseconds
val staleDataWatchDogTimeout: Duration = 1500.milliseconds

internal const val LE_CONNECTION_QUALIFIER: String = "LE_CONNECTION_QUALIFIER"

internal const val LEGACY_CONNECTION_QUALIFIER: String = "LEGACY_CONNECTION_QUALIFIER"

internal const val DEMO_CONNECTION_QUALIFIER: String = "DEMO_CONNECTION_QUALIFIER"