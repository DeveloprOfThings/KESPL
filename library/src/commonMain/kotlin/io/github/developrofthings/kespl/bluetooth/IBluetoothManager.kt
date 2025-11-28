package io.github.developrofthings.kespl.bluetooth

import io.github.developrofthings.kespl.ESPContext
import kotlinx.coroutines.flow.Flow

internal interface IBluetoothManager {

    val espContext: ESPContext

    suspend fun checkIsBluetoothSupported(): Boolean

    suspend fun checkIsBluetoothLESupported(): Boolean

    suspend fun checkIsBluetoothEnabled(): Boolean

    suspend fun checkHasBluetoothPermission(): Boolean

    val bluetoothEnabled: Flow<Boolean>

    suspend fun tryAcquireBTDevice(identifier: String): BTDevice?
}