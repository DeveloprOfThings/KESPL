/**
 * The core logic in this file for "wrapping" a "BluetoothGattDescriptor" is heavily inspired by and
 * based on the implementation in the "Kotlin-BLE-Library" project, which is licensed under the
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
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresPermission
import io.github.developrofthings.kespl.packet.toHexString
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class AndroidLeDescriptorWrapper(
    private val gatt: BluetoothGatt,
    private val descriptor: BluetoothGattDescriptor,
    private val isConnected: suspend () -> Boolean,
    private val logger: PlatformLogger,
    private val gattEvents: SharedFlow<ESPGattEvent>,
    private val mutex: Mutex,
) {
    val uuid: Uuid get() = descriptor.uuid.toKotlinUuid()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray) {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeDescriptorWrapper",
                message = """Failed to write ${value.toHexString()} to characteristic: $uuid because 
                    |there is no connection established."""
                    .trimMargin()
            )
            throw GattNotConnectedException()
        }
        mutex.withLock {
            gattEvents
                .onSubscription {
                    // Perform the descriptor write
                    performWrite(value = value)
                }
                // Collect until we get a response of the connection is interrupted
                .takeWhile { !it.isConnectionInterrupted() }
                .filterIsInstance(ESPGattEvent.DescriptorWriteEvent::class)
                .filter { event -> event.matches(descriptor) }
                .firstOrNull()?.let { event ->
                    if(!event.isSuccessful()) {
                        throw DescriptorWriteException(
                            descriptorUUID = uuid,
                            status = event.status
                        )
                    }
                } ?: UnknownException(operation = GattOperation.CharacteristicNotifications)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun performWrite(value: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(
                /* descriptor = */ descriptor,
                /* value = */ value,
            )
        } else {
            @Suppress("DEPRECATION")
            descriptor.setValue(/* value = */ value)
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(/* descriptor = */ descriptor)
        }
    }
}