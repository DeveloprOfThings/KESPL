/**
 * The core logic in this file for massaging "GATT Callbacks" into events that can be observed via
 * Kotlin's Flow API is heavily inspired by and based on the implementation in the
 * "Kotlin-BLE-Library" project, which is licensed under the BSD-3-Clause license.
 *
 * While this implementation was written from scratch, it follows the same
 * fundamental approach.
 *
 * You can find the original project here: https://github.com/NordicSemiconductor/Kotlin-BLE-Library
 *
 * -----------------------------------------------------------------------------
 *
 * Original project's copyright notice:
 *
 * Copyright (c) 2024, Nordic Semiconductor
 * All rights reserved.
 *
 * For the full license text, see the NOTICE file in the project root.
 */
package io.github.developrofthings.kespl.bluetooth.connection.le

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class ESPGattCallback : BluetoothGattCallback() {

    private val _events: MutableSharedFlow<ESPGattEvent> = MutableSharedFlow(
        extraBufferCapacity = 6,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    val events: SharedFlow<ESPGattEvent>
        get() = _events

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        _events.tryEmit(ESPGattEvent.ConnectionStateChangeEvent(status, newState))
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        _events.tryEmit(
            ESPGattEvent.ServicesDiscoveredEvent(
                services = if (status == BluetoothGatt.GATT_SUCCESS) gatt.services
                else emptyList(),
                status = status,
            )
        )
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        _events.tryEmit(ESPGattEvent.DescriptorWriteEvent(descriptor, status))
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        _events.tryEmit(ESPGattEvent.CharacteristicWriteEvent(characteristic, status))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        _events.tryEmit(ESPGattEvent.CharacteristicChangedEvent(characteristic, value))
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        _events.tryEmit(ESPGattEvent.ReadRemoteRssiEvent(rssi, status))
    }
}


sealed interface ESPGattEvent {

    class ConnectionStateChangeEvent(
        val status: Int,
        val newState: Int
    ) : ESPGattEvent {

        val isDisconnected get() = newState == BluetoothProfile.STATE_DISCONNECTED
    }

    class ServicesDiscoveredEvent(
        val services: List<BluetoothGattService>,
        val status: Int,
    ) : ESPGattEvent {
        fun isSuccessful(): Boolean = status == BluetoothGatt.GATT_SUCCESS
    }

    sealed interface GattServiceEvent : ESPGattEvent

    sealed interface GattCharacteristicEvent : GattServiceEvent {
        val characteristic: BluetoothGattCharacteristic

        fun matches(other: BluetoothGattCharacteristic): Boolean =
            characteristic.uuid == other.uuid &&
                    characteristic.instanceId == other.instanceId
    }

    sealed interface GattDescriptorEvent : GattServiceEvent {
        val descriptor: BluetoothGattDescriptor
        val parentCharacteristic: BluetoothGattCharacteristic

        fun matches(other: BluetoothGattDescriptor): Boolean =
            descriptor.uuid == other.uuid &&
                    parentCharacteristic.uuid == other.uuid ||
                    parentCharacteristic.instanceId == other.characteristic.instanceId
    }

    class DescriptorWriteEvent(
        override val descriptor: BluetoothGattDescriptor,
        val status: Int
    ) : GattDescriptorEvent {
        fun isSuccessful(): Boolean = status == BluetoothGatt.GATT_SUCCESS

        override val parentCharacteristic: BluetoothGattCharacteristic
            get() = descriptor.characteristic
    }

    class CharacteristicWriteEvent(
        override val characteristic: BluetoothGattCharacteristic,
        val status: Int
    ) : GattCharacteristicEvent {
        fun isSuccessful(): Boolean = status == BluetoothGatt.GATT_SUCCESS
    }

    class CharacteristicChangedEvent(
        override val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray,
    ) : GattCharacteristicEvent

    class ReadRemoteRssiEvent(
        val rssi: Int,
        val status: Int,
    ) : ESPGattEvent {
        fun isSuccessful(): Boolean = status == BluetoothGatt.GATT_SUCCESS
    }
}

fun ESPGattEvent.isConnectionInterrupted(): Boolean =
    if (this is ESPGattEvent.ConnectionStateChangeEvent) this.isDisconnected
    else false
