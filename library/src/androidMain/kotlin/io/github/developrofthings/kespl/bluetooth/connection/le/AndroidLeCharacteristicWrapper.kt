/**
 * The core logic in this file for "wrapping" a "BluetoothGattCharacteristic" is heavily inspired by
 * and based on the implementation in the "Kotlin-BLE-Library" project, which is licensed under the
 * BSD-3-Clause license.
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
@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresPermission
import io.github.developrofthings.kespl.bluetooth.ESPBluetoothStatusCode
import io.github.developrofthings.kespl.bluetooth.EspUUID
import io.github.developrofthings.kespl.packet.toHexString
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.extensions.toStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class AndroidLeCharacteristicWrapper(
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val isConnected: suspend () -> Boolean,
    private val logger: PlatformLogger,
    private val gattEvents: SharedFlow<ESPGattEvent>,
    private val mutex: Mutex,
) : PlatformLeCharacteristicWrapper {

    override val uuid: Uuid get() = characteristic.uuid.toKotlinUuid()

    private var descriptors: List<AndroidLeDescriptorWrapper> = characteristic.descriptors.map {
        AndroidLeDescriptorWrapper(
            gatt = gatt,
            descriptor = it,
            isConnected = isConnected,
            logger = logger,
            gattEvents = gattEvents,
            mutex = mutex,
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun notifications(): Flow<ByteArray> {
        if (!isConnected()) return flow { throw GattNotConnectedException() }

        return gattEvents
            .onSubscription { enableNotifications(enable = true) }
            .takeWhile { !it.isConnectionInterrupted() }
            // We only care about the characteristic changed event
            .filterIsInstance<ESPGattEvent.CharacteristicChangedEvent>()
            // We only care about changes to "this" characteristic
            .filter { it.matches(characteristic) }
            .map { it.value }
    }

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun performWrite(
        data: ByteArray,
    ): ESPBluetoothStatusCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        gatt.writeCharacteristic(
            /* characteristic = */ characteristic,
            /* value = */ data,
            /* writeType = */ characteristic.writeType,
        ).toStatusCode()
    } else when (
        gatt.writeCharacteristic(
            characteristic.also {
                it.value = data
            }
        )
    ) {
        true -> ESPBluetoothStatusCode.Success
        // Since on older versions gatt.writeCharacteristic was treated as boolean if not
        // true return unknown
        false -> ESPBluetoothStatusCode.ErrorUnknown
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun write(data: ByteArray) {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeCharacteristicWrapper",
                message = """Failed to write ${data.toHexString()} to characteristic: $uuid because 
                    |there is no connection established."""
                    .trimMargin()
            )
            throw GattNotConnectedException()
        }

        mutex.withLock {
            gattEvents
                .onSubscription {
                    when (performWrite(data)) {
                        ESPBluetoothStatusCode.ErrorGattWriteNotAllowed -> throw CharacteristicWriteNotAllowedException()
                        ESPBluetoothStatusCode.ErrorProfileServiceNotBound -> throw ProfileServiceNotBound()
                        ESPBluetoothStatusCode.ErrorGattWriteRequestBusy -> throw CharacteristicWriteBusyException()
                        ESPBluetoothStatusCode.ErrorUnknown -> throw UnknownException(GattOperation.CharacteristicWrite)

                        ESPBluetoothStatusCode.Success -> { /*NO-OP*/
                        }

                        ESPBluetoothStatusCode.ErrorMissingBluetoothConnectPermission -> { /*NO-OP*/
                        }

                        ESPBluetoothStatusCode.ErrorDeviceNotConnected -> { /*NO-OP*/
                        }
                    }
                }
                .takeWhile { !it.isConnectionInterrupted() }
                .filterIsInstance<ESPGattEvent.CharacteristicWriteEvent>()
                .filter { it.matches(characteristic) }
                .firstOrNull()?.let {
                    // If the write wasn't successfully treat this exceptionally since the library
                    // doesn't currently have a retry mechanism and the assumption this would only
                    // happen do to programmer error
                    if (!it.isSuccessful()) throw CharacteristicWriteException()
                } ?: throw UnknownException(operation = GattOperation.CharacteristicWrite)
        }
    }

    private fun getDescriptor(uuid: Uuid): AndroidLeDescriptorWrapper? = descriptors
        .firstOrNull { it.uuid == uuid }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal suspend fun enableNotifications(enable: Boolean) {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeCharacteristicWrapper",
                message = """Failed to ${if (enable) "enable" else "disable"} notifications for 
                    |characteristic: $uuid because there is no connection established."""
                    .trimMargin()
            )
            return
        }
        logger.info(
            tag = "AndroidLeCharacteristicWrapper",
            message = """${if (enable) "Enabling" else "Disabling"} notifications for 
                |characteristic: $uuid""".trimMargin()
        )

        gatt.setCharacteristicNotification(
            /* characteristic = */ this.characteristic,
            /* enable = */ enable
        )

        // Grab the notification descriptor
        val descriptor = getDescriptor(
            uuid = EspUUID.CLIENT_CHARACTERISTIC_CONFIG_CHARACTERISTIC_UUID
        )
            ?: throw DescriptorNotFoundException(
                uuid = EspUUID.CLIENT_CHARACTERISTIC_CONFIG_CHARACTERISTIC_UUID
            )

        descriptor.write(
            value = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE,
        )
    }
}