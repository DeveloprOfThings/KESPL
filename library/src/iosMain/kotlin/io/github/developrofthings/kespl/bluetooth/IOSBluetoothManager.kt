@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.bluetooth.BTDevice
import io.github.developrofthings.kespl.bluetooth.connection.le.ESPCentralManagerDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSUUID
import platform.darwin.dispatch_queue_create
import kotlin.collections.firstOrNull
import kotlin.uuid.ExperimentalUuidApi

@Single
internal class IOSBluetoothManager() : IBluetoothManager {

    override val espContext: ESPContext =
        ESPContext()

    private val _delegate =
        ESPCentralManagerDelegate()

    val delegate: ESPCentralManagerDelegate get() = _delegate

    internal val _cbCentralManager = CBCentralManager(
        delegate = _delegate,
        queue = dispatch_queue_create(label = DISPATCHER_LABEL, null),
    )

    val cbCentralManager: CBCentralManager get() = _cbCentralManager

    suspend fun waitUntilManagerIsReady(): Boolean = _delegate
        .managerState
        // We don't care about these intermediate states... Block until the CentralManager we are in
        // a "settled" state such as `PoweredOn`, `PoweredOff`, `Unauthorized` or `Unsupported`.
        .filterNot {
            it == IOSCentralManagerState.Unknown ||
            it == IOSCentralManagerState.Resetting
        }.firstOrNull()?.let {
            // `PoweredOn` should be treated as a ready state...
            it == IOSCentralManagerState.PoweredOn
        } ?: false

    override suspend fun checkIsBluetoothSupported(): Boolean {
        if(!waitUntilManagerIsReady())  {
            // Bluetooth is not ready but that doesn't mean it's not supported of the device, it
            // could be `PoweredOn`, `PoweredOff`, `Unauthorized`
            return _delegate.managerState.value != IOSCentralManagerState.Unsupported
        }
        return true
    }

    override suspend fun checkIsBluetoothLESupported(): Boolean {
        if(!waitUntilManagerIsReady())  {
            // Bluetooth is not ready but that doesn't mean it's not supported of the device, it
            // could be `PoweredOn`, `PoweredOff`, `Unauthorized`
            return _delegate.managerState.value != IOSCentralManagerState.Unsupported
        }
        return true
    }

    override suspend fun checkIsBluetoothEnabled(): Boolean = waitUntilManagerIsReady()

    override suspend fun checkHasBluetoothPermission(): Boolean {
        if(!waitUntilManagerIsReady())  {
            // Bluetooth is not ready, check to see if its in the unauthorized state
            return _delegate.managerState.value != IOSCentralManagerState.Unauthorized
        }
        return true
    }

    override val bluetoothEnabled: Flow<Boolean>
        get() = _delegate.managerState.map { it == IOSCentralManagerState.PoweredOn }

    override suspend fun tryAcquireBTDevice(identifier: String): BTDevice? {
        val peripheral: CBPeripheral? = _cbCentralManager.retrievePeripheralsWithIdentifiers(
            identifiers = listOf(
                NSUUID(uUIDString = identifier)
            )
        ).firstOrNull() as CBPeripheral?

        return peripheral?.let(::BTDevice)
    }
}

private const val DISPATCHER_LABEL: String = "IOS_ESP_CENTRA_MANAGER_DISPATCHER"