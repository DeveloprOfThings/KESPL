package io.github.developrofthings.kespl.utilities.extensions

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import io.github.developrofthings.kespl.BTUnsupported
import io.github.developrofthings.kespl.ESPContext
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContextExtTest {

    @Test
    fun `When BT is not supported, Context_isBTSupported returns false`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
            }
            every { packageManager } returns pkgManager
        }

        assertFalse(actual = androidContext.isBTSupported())
    }

    @Test
    fun `When BT is supported, Context_isBTSupported returns true`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
            }
            every { packageManager } returns pkgManager
        }

        assertTrue(actual = androidContext.isBTSupported())
    }

    @Test
    fun `When BLE is not supported, Context_isLESupported returns false`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            every { packageManager } returns pkgManager
        }

        assertFalse(actual = androidContext.isLESupported())
    }

    @Test
    fun `When BLE is supported, Context_isLESupported returns true`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }
            every { packageManager } returns pkgManager
        }

        assertTrue(actual = androidContext.isLESupported())
    }

    @Test
    fun `When BT is not supported, Context_tryAcquireBTAdapter returns null`() {
        val androidContext = mockk<Context> {
            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns null
        }

        assertNull(actual = androidContext.tryAcquireBTAdapter())
    }


    @Test
    fun `When BT is not supported, ESPContext_isBluetoothSupported false`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
            }
            every { packageManager } returns pkgManager
        }
        val espContext = ESPContext(androidContext)

        assertFalse(actual = espContext.isBluetoothSupported())
    }

    @Test
    fun `When BT is supported, ESPContext_isBluetoothSupported returns true`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
            }
            every { packageManager } returns pkgManager
        }
        val espContext = ESPContext(androidContext)

        assertTrue(actual = espContext.isBluetoothSupported())
    }

    @Test
    fun `When BLE is not supported, ESPContext_isBluetoothLESupported returns false`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            every { packageManager } returns pkgManager
        }
        val espContext = ESPContext(androidContext)

        assertFalse(actual = espContext.isBluetoothLESupported())
    }

    @Test
    fun `When BLE is supported, ESPContext_isBluetoothLESupported returns true`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }
            every { packageManager } returns pkgManager
        }
        val espContext = ESPContext(androidContext)

        assertTrue(actual = espContext.isBluetoothLESupported())
    }

    @Test
    fun `When BT is not supported, ESPContext_acquireBTAdapter throw BTUnsupported exception`() {
        val androidContext = mockk<Context> {
            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns null
        }
        val espContext = ESPContext(androidContext)

        assertFailsWith<BTUnsupported> { espContext.acquireBtAdapter() }
    }

    @Test
    fun `When BT is supported, ESPContext_acquireBTAdapter returns non-null adapter`() = runTest {
        val androidContext = mockk<Context> {
            val pkgManager = mockk<PackageManager> {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }
            val btManager = mockk<BluetoothManager> {
                val btAdapter = mockk<BluetoothAdapter>()
                every { adapter } returns btAdapter
            }

            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager

            every { packageManager } returns pkgManager
        }
        val espContext = ESPContext(androidContext)
        assertNotNull(actual = espContext.acquireBtAdapter())
    }
}