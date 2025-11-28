package io.github.developrofthings.kespl.bluetooth.connection.le

import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import toByteArray

internal class ESPCoreBluetoothPeripheralDelegate : NSObject(), CBPeripheralDelegateProtocol {

    private val _events: MutableSharedFlow<io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent> = MutableSharedFlow(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    val events: SharedFlow<io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent> get() = _events.asSharedFlow()

    override fun peripheral(
        peripheral: CBPeripheral,
        didReadRSSI: NSNumber,
        error: NSError?
    ) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.ReadRemoteRssiEvent(
                peripheral = peripheral,
                rssi = didReadRSSI.intValue,
                error = error,
            )
        )
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverServices: NSError?
    ) {

        @Suppress("UNCHECKED_CAST")
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.ServicesDiscoveredEvent(
                peripheral = peripheral,
                services = didDiscoverServices?.let { emptyList() }
                    ?: peripheral.services?.map { it as CBService } ?: emptyList(),
                error = didDiscoverServices,
            )
        )
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.ServiceCharacteristicsDiscoveredEvent(
                peripheral = peripheral,
                service = didDiscoverCharacteristicsForService,
                error = error,
            )
        )
    }

    override fun peripheralIsReadyToSendWriteWithoutResponse(peripheral: CBPeripheral) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralIsReadyToSend(
                peripheral = peripheral,
            )
        )
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidWriteValue(
                peripheral = peripheral,
                characteristic = didWriteValueForCharacteristic,
                error = error,
            )
        )
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateNotificationState(
                peripheral = peripheral,
                characteristic = didUpdateNotificationStateForCharacteristic,
                enabled = didUpdateNotificationStateForCharacteristic.isNotifying,
                error = error,
            )
        )
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        _events.tryEmit(
            value = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateValue(
                peripheral = peripheral,
                characteristic = didUpdateValueForCharacteristic,
                value = didUpdateValueForCharacteristic.value?.toByteArray() ?: byteArrayOf(),
                error = error,
            )
        )
    }
}

class ReadRemoteRssiEvent(
    override val peripheral: CBPeripheral,
    val rssi: Int,
    val error: NSError?
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class ServicesDiscoveredEvent(
    override val peripheral: CBPeripheral,
    val services: List<CBService>,
    val error: NSError?,
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class ServiceCharacteristicsDiscoveredEvent(
    override val peripheral: CBPeripheral,
    val service: CBService,
    val error: NSError?,
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class PeripheralIsReadyToSend(
    override val peripheral: CBPeripheral,
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent

sealed interface ESPCBCharacteristicEvent :
    io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent {
    val characteristic: CBCharacteristic

    val error: NSError?

    fun isSuccessful(): Boolean = error == null
}

class CharacteristicDidWriteValue(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    override val error: NSError?
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCBCharacteristicEvent

class CharacteristicDidUpdateNotificationState(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    val enabled: Boolean,
    override val error: NSError?
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCBCharacteristicEvent

class CharacteristicDidUpdateValue(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    val value: ByteArray,
    override val error: NSError?
) : io.github.developrofthings.kespl.bluetooth.connection.le.ESPCBCharacteristicEvent