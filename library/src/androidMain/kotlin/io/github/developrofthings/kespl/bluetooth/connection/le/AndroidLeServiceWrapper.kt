/**
 * The core logic in this file for "wrapping" a "BluetoothGattService" is heavily inspired by and
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

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

internal class AndroidLeServiceWrapper(
    gatt: BluetoothGatt,
    private val service: BluetoothGattService,
    private val isConnected: suspend () -> Boolean,
    private val logger: PlatformLogger,
    private val gattEvents: SharedFlow<ESPGattEvent>,
    private val mutex: Mutex,
): PlatformLeServiceWrapper {
    override val uuid: Uuid get() = service.uuid.toKotlinUuid()

    override val characteristics: List<PlatformLeCharacteristicWrapper> = service.characteristics.map {
        AndroidLeCharacteristicWrapper(
            gatt = gatt,
            characteristic = it,
            isConnected = isConnected,
            logger = logger,
            gattEvents = gattEvents,
            mutex = mutex,
        )
    }
}