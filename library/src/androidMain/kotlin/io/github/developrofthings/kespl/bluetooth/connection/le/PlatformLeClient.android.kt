@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import androidx.annotation.RequiresPermission
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.ExperimentalESPApi
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.EspUUID
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

internal class AndroidLeGattClient(
    private val gatt: BluetoothGatt,
    private val gattCallback: ESPGattCallback,
    private val logger: PlatformLogger,
    scope: CoroutineScope,
) : PlatformLeClient {

    private val clientScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob(scope.coroutineContext.job))

    private val mutex = Mutex()

    override val connectionStatus: StateFlow<ESPConnectionStatus> = gattCallback
        .events
        .filterIsInstance(ESPGattEvent.ConnectionStateChangeEvent::class)
        .map(ESPGattEvent.ConnectionStateChangeEvent::toESPConnectionStatus)
        .stateIn(
            scope = clientScope,
            started = SharingStarted.Eagerly,
            initialValue = ESPConnectionStatus.Connecting,
        )

    override suspend fun isConnected(): Boolean =
        connectionStatus.firstOrNull() == ESPConnectionStatus.Connected

    private suspend fun isConnecting(): Boolean =
        connectionStatus.firstOrNull() == ESPConnectionStatus.Connecting

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun cancelConnection() {
        gatt.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @ExperimentalESPApi
    override suspend fun disconnectWaitForConfirmation() {
        // If we are connecting we want to close the peripheral connection and return
        if(isConnecting()) {
            logger.info(
                tag = "IOSLeClient",
                message = "Cancelling the connection attempt in progress",
            )
            // Since a "full" connection has not been established yet, calling gatt.disconnect() may
            // not trigger a call to BluetoothGattCallback.onConnectionStateChange(...) so we must
            // artificially invoke the callback which will cause waitForConnection() to return and
            // propagate the disconnected state to the rest of the library.
            gattCallback.onConnectionStateChange(
                gatt,
                status = BluetoothGatt.GATT_FAILURE,
                newState = BluetoothProfile.STATE_DISCONNECTED,
            )
            gatt.close()
            return
        }
        if (!isConnected()) {
            logger.warn(
                tag = "AndroidLeGattClient",
                message = "No connection established",
            )
            return
        }

        mutex.withLock {
            // Trigger a GATT disconnection then wait for the appropriate event to be event.
            gattCallback
                .events
                .onSubscription {
                    // We don't want to call `gatt.close()` because that will internally unregister
                    // our callback and we will never receive a connection state change event.
                    cancelConnection()
                }
                .filterIsInstance<ESPGattEvent.ConnectionStateChangeEvent>()
                .filter { it.isDisconnected }
                .first()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun close() {
        gatt.close()
        clientScope.cancel()
    }

    internal suspend fun waitForConnection(): ESPConnectionStatus {
        if (isConnected()) {
            logger.warn(
                tag = "AndroidLeGattClient",
                message = """Connection already established, returning ESPConnectionStatus.Connected"""
                    .trimMargin()
            )
            return ESPConnectionStatus.Connected
        }
        return mutex.withLock {
            connectionStatus
                // Connection status is initialized to Connecting and we don't care about this state
                // The immediate next state which will indicate if the connection was established
                .dropWhile { it == ESPConnectionStatus.Connecting }
                .firstOrNull()
                ?.let {
                    // If we aren't connected, report failure
                    it.takeIf { it == ESPConnectionStatus.Connected }
                        ?: ESPConnectionStatus.ConnectionFailed
                } ?: ESPConnectionStatus.ConnectionFailed
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun establishConnection() {
        if (waitForConnection() != ESPConnectionStatus.Connected) {
            // Make we call close before throwing connection failed so that we don't get an
            // unexpected GATT connection when we didn't expect one
            gatt.close()
            throw LeConnectionFailed(gatt.device.name ?: "V1c-LE")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun discoveryV1CLEServiceAndCharacteristics(): PlatformLeServiceWrapper {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeGattClient",
                message = "Attempting to discover services and characteristics while not connected"
            )
            throw GattNotConnectedException()
        }

        return mutex.withLock {
            gattCallback.events
                .onSubscription { gatt.discoverServices() }
                .takeWhile { !it.isConnectionInterrupted() }
                .filterIsInstance<ESPGattEvent.ServicesDiscoveredEvent>()
                .map { event ->
                    val services = event.services
                    if (!event.isSuccessful() || services.isEmpty()) {
                        throw FailedToDiscoverServices()
                    }

                    val leService = services
                        .getV1connectionLeService()

                    AndroidLeServiceWrapper(
                        gatt = gatt,
                        service = leService,
                        isConnected = ::isConnected,
                        logger = logger,
                        gattEvents = gattCallback.events,
                        mutex = mutex,
                    )
                }.firstOrNull()
            // If we reach this point we don't know exactly what has gone wrong but we know we
            // failed to discover services
                ?: throw UnknownException(GattOperation.DiscoverServices)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun readRemoteRssi(): Int {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeGattClient",
                message = "Attempting to read Rssi while not connected",
            )
            throw GattNotConnectedException()
        }

        return mutex.withLock {
            gattCallback.events
                .onSubscription { gatt.readRemoteRssi() }
                .takeWhile { !it.isConnectionInterrupted() }
                .filterIsInstance<ESPGattEvent.ReadRemoteRssiEvent>()
                .map { event ->
                    if (!event.isSuccessful()) throw ReadRemoteRssiException()
                    event.rssi
                } .firstOrNull() ?: throw UnknownException(GattOperation.RemoteRssi)
        }
    }
}

private fun ESPGattEvent.ConnectionStateChangeEvent.toESPConnectionStatus(): ESPConnectionStatus =
    if (status == BluetoothGatt.GATT_SUCCESS) {
        when (newState) {
            BluetoothProfile.STATE_DISCONNECTING -> ESPConnectionStatus.Disconnecting
            BluetoothProfile.STATE_CONNECTING -> ESPConnectionStatus.Connecting
            BluetoothProfile.STATE_CONNECTED -> ESPConnectionStatus.Connected
            // Treat any other state as disconnected
            else -> ESPConnectionStatus.Disconnected
        }
    } else ESPConnectionStatus.Disconnected


private fun List<BluetoothGattService>.getV1connectionLeService(): BluetoothGattService =
    first { it.uuid == EspUUID.V1CONNECTION_LE_SERVICE_UUID.toJavaUuid() }

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
private fun V1connection.Remote.establishGattConnection(
    directConnect: Boolean,
    espContext: ESPContext,
    callback: ESPGattCallback,
): BluetoothGatt = this.device.realDevice.connectGatt(
    /* context = */ espContext.appContext,
    // autoConnect if `directConnect` is false
    /* autoConnect = */ !directConnect,
    /* callback = */ callback,
    /* transport = */ BluetoothDevice.TRANSPORT_AUTO,
    /* phy = */ BluetoothDevice.PHY_LE_1M
)

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
internal actual suspend fun getPlatformLeClient(
    v1c: V1connection.Remote,
    directConnect: Boolean,
    bluetoothManager: IBluetoothManager,
    logger: PlatformLogger,
    coroutineScope: CoroutineScope
): PlatformLeClient = ESPGattCallback().let { callback ->
    AndroidLeGattClient(
        gatt = v1c.establishGattConnection(
            directConnect = directConnect,
            espContext = bluetoothManager.espContext,
            callback = callback,
        ),
        gattCallback = callback,
        logger = logger,
        scope = coroutineScope,
    )
}
