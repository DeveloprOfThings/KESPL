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

    private val _events: MutableSharedFlow<ESPCoreBluetoothEvent> = MutableSharedFlow(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    val events: SharedFlow<ESPCoreBluetoothEvent> get() = _events.asSharedFlow()

    override fun peripheral(
        peripheral: CBPeripheral,
        didReadRSSI: NSNumber,
        error: NSError?
    ) {
        _events.tryEmit(
            value = ReadRemoteRssiEvent(
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
            value = ServicesDiscoveredEvent(
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
            value = ServiceCharacteristicsDiscoveredEvent(
                peripheral = peripheral,
                service = didDiscoverCharacteristicsForService,
                error = error,
            )
        )
    }

    override fun peripheralIsReadyToSendWriteWithoutResponse(peripheral: CBPeripheral) {
        _events.tryEmit(
            value = PeripheralIsReadyToSend(
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
            value = CharacteristicDidWriteValue(
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
            value = CharacteristicDidUpdateNotificationState(
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
            value = CharacteristicDidUpdateValue(
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
) : ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class ServicesDiscoveredEvent(
    override val peripheral: CBPeripheral,
    val services: List<CBService>,
    val error: NSError?,
) : ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class ServiceCharacteristicsDiscoveredEvent(
    override val peripheral: CBPeripheral,
    val service: CBService,
    val error: NSError?,
) : ESPCoreBluetoothEvent {
    fun isSuccessful(): Boolean = error == null
}

class PeripheralIsReadyToSend(
    override val peripheral: CBPeripheral,
) : ESPCoreBluetoothEvent

sealed interface ESPCBCharacteristicEvent :
    ESPCoreBluetoothEvent {
    val characteristic: CBCharacteristic

    val error: NSError?

    fun isSuccessful(): Boolean = error == null
}

class CharacteristicDidWriteValue(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    override val error: NSError?
) : ESPCBCharacteristicEvent

class CharacteristicDidUpdateNotificationState(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    val enabled: Boolean,
    override val error: NSError?
) : ESPCBCharacteristicEvent

class CharacteristicDidUpdateValue(
    override val peripheral: CBPeripheral,
    override val characteristic: CBCharacteristic,
    val value: ByteArray,
    override val error: NSError?
) : ESPCBCharacteristicEvent