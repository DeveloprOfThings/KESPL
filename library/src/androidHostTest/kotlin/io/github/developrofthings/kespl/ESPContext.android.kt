package io.github.developrofthings.kespl

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk

private fun androidContext(
    bluetoothSupported: Boolean,
    bleSupported: Boolean
): Context {
    return mockk<Context> {
        val pkgManager = mockk<PackageManager>().apply {
            every {
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            } returns bluetoothSupported
            every {
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            } returns (bluetoothSupported and bleSupported)
        }
        every { packageManager } returns pkgManager
    }
}

actual fun platformContext(
    bluetoothSupported: Boolean,
    bleSupported: Boolean,
): ESPContext = ESPContext(
    appContext = androidContext(
        bluetoothSupported = bluetoothSupported,
        bleSupported = bleSupported,
    )
)