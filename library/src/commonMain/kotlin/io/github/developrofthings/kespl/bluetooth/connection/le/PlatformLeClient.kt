package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal interface PlatformLeClient {
    
    val connectionStatus: StateFlow<ESPConnectionStatus>

    suspend fun isConnected(): Boolean

    suspend fun establishConnection()

    suspend fun disconnectWaitForConfirmation()

    fun cancelConnection()

    fun close()

    suspend fun discoveryV1CLEServiceAndCharacteristics(): PlatformLeServiceWrapper

    suspend fun readRemoteRssi(): Int
}

internal expect suspend fun getPlatformLeClient(
    v1c: V1connection.Remote,
    directConnect: Boolean,
    bluetoothManager: IBluetoothManager,
    logger: PlatformLogger,
    coroutineScope: CoroutineScope,
): PlatformLeClient