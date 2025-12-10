package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.IOSCentralManagerState
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject

internal class ESPCentralManagerDelegate : NSObject(), CBCentralManagerDelegateProtocol {

    private val _managerState: MutableStateFlow<IOSCentralManagerState> = MutableStateFlow(
        // Initialize to the unknown... this is the initial value of the CBCentralManager.state
        value = IOSCentralManagerState.Unknown
    )

    val managerState: StateFlow<IOSCentralManagerState> = _managerState.asStateFlow()

    private val _events: MutableSharedFlow<ESPCoreBluetoothEvent> = MutableSharedFlow(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    val events: SharedFlow<ESPCoreBluetoothEvent> get() = _events.asSharedFlow()

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ) {
        _events.tryEmit(
            value = CentralManagerDiscoveryEvent(
                peripheral = didDiscoverPeripheral,
                rssi = RSSI.intValue,
            )
        )
    }

    override fun centralManager(
        central: CBCentralManager,
        didConnectPeripheral: CBPeripheral
    ) {
        _events.tryEmit(
            value = CentralManagerConnectionEvent(
                peripheral = didConnectPeripheral,
                status = ESPConnectionStatus.Connected,
            )
        )
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        _events.tryEmit(
            value = CentralManagerConnectionEvent(
                peripheral = didDisconnectPeripheral,
                status = ESPConnectionStatus.Disconnected,
            )
        )
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        _events.tryEmit(
            value = CentralManagerConnectionEvent(
                peripheral = didFailToConnectPeripheral,
                status = ESPConnectionStatus.ConnectionFailed,
            )
        )
    }

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        _managerState.value =
            IOSCentralManagerState.Companion.fromCBManagerStateLong(value = central.state)
    }
}

class CentralManagerDiscoveryEvent(
    override val peripheral: CBPeripheral,
    val rssi: Int,
) : ESPCoreBluetoothEvent

class CentralManagerConnectionEvent(
    override val peripheral: CBPeripheral,
    val status: ESPConnectionStatus,
) : ESPCoreBluetoothEvent {
    val isDisconnected
        get() =
            status != ESPConnectionStatus.Connected &&
                    (status != ESPConnectionStatus.Connecting) &&
                    (status != ESPConnectionStatus.Disconnecting)
}

fun ESPCoreBluetoothEvent.isConnectionInterrupted(
    peripheral: CBPeripheral,
): Boolean = if (this.peripheral.identifier == peripheral.identifier() &&
    this is CentralManagerConnectionEvent
) this.isDisconnected
else false

@Suppress("UNCHECKED_CAST")
internal inline fun <reified TEvent : ESPCoreBluetoothEvent> Flow<ESPCoreBluetoothEvent>.filterForPeripheral(
    peripheral: CBPeripheral,
): Flow<TEvent> =
    this
        .filter { event -> event is TEvent && event.peripheral.identifier == peripheral.identifier }
            as Flow<TEvent>

@Suppress("UNCHECKED_CAST")
internal inline fun <reified TEvent : ESPCBCharacteristicEvent> Flow<ESPCoreBluetoothEvent>.filterForCharacteristic(
    characteristic: CBCharacteristic,
): Flow<TEvent> =
    this
        .filter { event -> event is TEvent && event.characteristic.UUID == characteristic.UUID }
            as Flow<TEvent>