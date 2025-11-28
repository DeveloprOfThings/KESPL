package io.github.developrofthings.kespl.bluetooth.connection.le

import CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_CBUUID
import V1CONNECTION_LE_SERVICE_CBUUID
import V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_CBUUID
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.EspUUID
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.IOSBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.bluetooth.connection.le.filterForPeripheral
import io.github.developrofthings.kespl.bluetooth.connection.le.getV1connectionLeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID

internal class IOSLeClient(
    internal val peripheral: CBPeripheral,
    private val centralManager: CBCentralManager,
    private val centralManagerDelegate: io.github.developrofthings.kespl.bluetooth.connection.le.ESPCentralManagerDelegate,
    private val peripheralDelegate: io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothPeripheralDelegate,
    private val logger: PlatformLogger,
    scope: CoroutineScope,
) : PlatformLeClient {

    private val _mutex = Mutex()

    private val clientScope =
        CoroutineScope(
            context = Dispatchers.Default + SupervisorJob(scope.coroutineContext.job)
        )

    override val connectionStatus: StateFlow<ESPConnectionStatus> = centralManagerDelegate
        .events
        .filterForPeripheral<io.github.developrofthings.kespl.bluetooth.connection.le.CentralManagerConnectionEvent>(peripheral)
        .map { it.status }
        .stateIn(
            scope = clientScope,
            started = SharingStarted.Eagerly,
            initialValue = ESPConnectionStatus.Connecting,
        )

    override suspend fun isConnected(): Boolean =
        connectionStatus.firstOrNull() == ESPConnectionStatus.Connected

    private suspend fun isConnecting(): Boolean =
        connectionStatus.firstOrNull() == ESPConnectionStatus.Connecting

    suspend fun waitForConnection(): ESPConnectionStatus {
        if (isConnected()) {
            logger.warn(
                tag = "IOSLeClient",
                message = """Connection already established, returning ESPConnectionStatus.Connected"""
                    .trimMargin()
            )
            return ESPConnectionStatus.Connected
        }
        return _mutex.withLock {
            connectionStatus
                .onSubscription {
                    centralManager.connectPeripheral(
                        peripheral = peripheral,
                        options = null,
                    )
                }
                .dropWhile { it == ESPConnectionStatus.Connecting }
                .firstOrNull()
                ?.let {
                    it.takeIf { it == ESPConnectionStatus.Connected }
                        ?: ESPConnectionStatus.ConnectionFailed
                } ?: ESPConnectionStatus.ConnectionFailed
        }
    }

    override suspend fun establishConnection() {
        if (waitForConnection() != ESPConnectionStatus.Connected) {
            // This is not explicitly necessary like it is for Android but should not cause
            // it any issues to be extra safe
            cancelConnection()
            throw LeConnectionFailed(peripheral.name ?: "V1c-LE")
        }
    }

    override fun cancelConnection() {
        centralManager.cancelPeripheralConnection(peripheral = peripheral)
    }

    override suspend fun disconnectWaitForConfirmation() {
        // If we are connecting we want to close the peripheral connection and return
        if(isConnecting()) {
            logger.info(
                tag = "IOSLeClient",
                message = "Cancelling the connection attempt in progress",
            )
            // close the open connection and return
            cancelConnection()
            return
        }
        else if (!isConnected()) {
            logger.warn(
                tag = "IOSLeClient",
                message = "No connection established",
            )
            return
        }
        _mutex.withLock {
            centralManagerDelegate
                .events
                .onSubscription { cancelConnection() }
                .filterForPeripheral<io.github.developrofthings.kespl.bluetooth.connection.le.CentralManagerConnectionEvent>(peripheral)
                .filter { it.isDisconnected }
                .first()
            // Make sure to clear the delegate after we've detected a disconnection... NEVER BEFORE
            peripheral.delegate = null
        }
    }

    override fun close() {
        cancelConnection()
        clientScope.cancel()
    }

    override suspend fun discoveryV1CLEServiceAndCharacteristics(): PlatformLeServiceWrapper {
        if (!isConnected()) {
            logger.error(
                tag = "IOSLeClient",
                message = "Attempting to discover services and characteristics while not connected"
            )
            throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralNotConnectedException()
        }

        /*
        Defined locally so that we don't need to perform explicit mutex locking inside of this
        function.
         */
        suspend fun discoverCharacteristics(
            peripheral: CBPeripheral,
            service: CBService
        ): Boolean = peripheralDelegate.events
            .onSubscription {
                peripheral.discoverCharacteristics(
                    characteristicUUIDs = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.v1connectionLECharacteristics,
                    forService = service,
                )
            }
            .filterForPeripheral<io.github.developrofthings.kespl.bluetooth.connection.le.ServiceCharacteristicsDiscoveredEvent>(peripheral)
            .map { event ->
                val characteristics = event.service.characteristics
                if (!event.isSuccessful() || characteristics.isNullOrEmpty()) {
                    // We failed to discover characteristics for service
                    throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.FailedToDiscoverServiceCharacteristics()
                }

                // We successfully discovered characteristics for service return true
                true
            }.firstOrNull()
        // If we reach this point we don't know exactly what has gone wrong but we know we
        // failed to discovery characteristics for a service
            ?: throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.UnknownException(
                operation = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CoreBluetoothOperation.DiscoverServiceCharacteristics,
            )

        return _mutex.withLock {
            peripheralDelegate.events
                .onSubscription {
                    peripheral.discoverServices(
                        serviceUUIDs = listOf(EspUUID.V1CONNECTION_LE_SERVICE_CBUUID)
                    )
                }
                .filterForPeripheral<io.github.developrofthings.kespl.bluetooth.connection.le.ServicesDiscoveredEvent>(peripheral)
                .map { event ->
                    val services = event.services
                    // An empty services list is considered exceptional because a V1connection
                    // should ALWAYS report the V1c LE service
                    if (!event.isSuccessful() || services.isEmpty()) {
                        throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.FailedToDiscoverServices()
                    }
                    // We've successfully discover the services now we must discover the
                    // ESP characteristics for the ESP Service

                    val leService = services
                        .getV1connectionLeService()

                    // If we failed to discover the services characteristics return null
                    if (!discoverCharacteristics(
                            peripheral = event.peripheral,
                            service = leService
                        )
                    ) {
                        // If we failed to discover the V1C LE Services characteristics we want to
                        // return null so we can fail exceptionally because the something terrible
                        // has gone wrong
                        return@map null
                    }

                    _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.IOSLeServiceWrapper(
                        peripheral = peripheral,
                        service = leService,
                        isConnected = ::isConnected,
                        logger = logger,
                        cbEvents = peripheralDelegate.events,
                        mutex = _mutex,
                    )
                }.firstOrNull()
            // If we reach this point we don't know exactly what has gone wrong but we know we
            // failed to discover services
                ?: throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.UnknownException(
                    operation = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CoreBluetoothOperation.DiscoverServices
                )
        }
    }

    override suspend fun readRemoteRssi(): Int {
        if (!isConnected()) {
            logger.error(
                tag = "IOSLeClient",
                message = "Attempting to read Rssi while not connected",
            )
            throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralNotConnectedException()
        }

        return _mutex.withLock {
            peripheralDelegate.events
                .onSubscription { peripheral.readRSSI() }
                .filterForPeripheral<io.github.developrofthings.kespl.bluetooth.connection.le.ReadRemoteRssiEvent>(peripheral)
                .map { event ->
                    if (!event.isSuccessful()) throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.ReadRemoteRssiException()
                    event.rssi
                }.firstOrNull()
                ?: throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.UnknownException(
                    _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CoreBluetoothOperation.RemoteRssi
                )
        }
    }
}

private fun List<CBService>.getV1connectionLeService(): CBService =
    first { it.UUID == EspUUID.V1CONNECTION_LE_SERVICE_CBUUID }

/**
 * [CBUUID] of the [platform.CoreBluetooth.CBCharacteristic] that make up in the
 * V1connection LE Service [CBService].
 */
private val v1connectionLECharacteristics: List<CBUUID> = listOf(
    EspUUID.CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_CBUUID,
    EspUUID.V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_CBUUID,
)

internal actual suspend fun getPlatformLeClient(
    v1c: V1connection.Remote,
    directConnect: Boolean,
    bluetoothManager: IBluetoothManager,
    logger: PlatformLogger,
    coroutineScope: CoroutineScope
): PlatformLeClient = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothPeripheralDelegate()
    .let { peripheralDelegate ->
        _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.IOSLeClient(
            peripheral = v1c.device.realDevice.apply {
                // Make sure we register our delegate so we can observe Peripheral level
                // events
                this.delegate = peripheralDelegate
            },

            centralManager = (bluetoothManager as io.github.developrofthings.kespl.bluetooth.IOSBluetoothManager).cbCentralManager,
            centralManagerDelegate = bluetoothManager.delegate,
            peripheralDelegate = peripheralDelegate,
            logger = logger,
            scope = coroutineScope,
        )
}