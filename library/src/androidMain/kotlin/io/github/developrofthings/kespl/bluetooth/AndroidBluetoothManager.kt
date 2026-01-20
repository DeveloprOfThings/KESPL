package io.github.developrofthings.kespl.bluetooth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.utilities.extensions.acquireBtAdapter
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
internal class AndroidBluetoothManager(
    override val espContext: ESPContext,
    private val bluetoothStateManager: AndroidBluetoothStateManager,
): IBluetoothManager {
    override suspend fun checkIsBluetoothSupported(): Boolean {
        return espContext.isBluetoothSupported()
    }

    override suspend fun checkIsBluetoothLESupported(): Boolean = espContext.isBluetoothLESupported()

    override suspend fun checkIsBluetoothEnabled(): Boolean {
        if(!checkIsBluetoothSupported()) return false
        val btAdapter = espContext.acquireBtAdapter()
        return btAdapter.isEnabled
    }

    override suspend fun checkHasBluetoothPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            /* context = */ espContext.appContext,
            /* permission = */ Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }
    else {
        ContextCompat.checkSelfPermission(
            /* context = */ espContext.appContext,
            /* permission = */ Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

    override val bluetoothEnabled: Flow<Boolean>
        get() = bluetoothStateManager.enabled()

    override suspend fun tryAcquireBTDevice(identifier: String): BTDevice? = BTDevice(
        realDevice = espContext.acquireBtAdapter()
            .getRemoteDevice(
                identifier
            )
    )
}