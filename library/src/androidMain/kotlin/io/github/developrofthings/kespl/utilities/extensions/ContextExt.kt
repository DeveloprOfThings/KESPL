package io.github.developrofthings.kespl.utilities.extensions

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import io.github.developrofthings.kespl.BTUnsupported
import io.github.developrofthings.kespl.ESPContext

/**
 * Attempts to acquire the default Bluetooth adapter, if available.
 *
 * This function retrieves the [BluetoothManager] system service and then accesses
 * its [adapter][BluetoothManager.getAdapter] property to get the [BluetoothAdapter].
 * It handles cases where the Bluetooth service or adapter might not be available
 * by using safe casts (`as?`) and the safe call operator (`?.`), returning `null`
 * in such scenarios.
 *
 * This is an extension function on [Context].
 *
 * @return The device's default [BluetoothAdapter] if Bluetooth is supported and available,
 *         or `null` otherwise (e.g., if the device doesn't have Bluetooth,
 *         or if Bluetooth is turned off and the system doesn't provide an adapter instance).
 * @see android.bluetooth.BluetoothManager
 * @see android.bluetooth.BluetoothAdapter
 * @see Context.getSystemService
 */
fun Context.tryAcquireBTAdapter(): BluetoothAdapter? =
    // Retrieve a reference to the BluetoothAdapter based on the Android SDK Version.
    (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

/**
 * Checks if the device supports Bluetooth Classic.
 *
 * This function attempts to acquire the default Bluetooth adapter. If an adapter
 * is successfully acquired (i.e., not null), it implies that Bluetooth Classic
 * is supported on the device.
 *
 * This is an extension function on [Context].
 *
 * @return `true` if Bluetooth Classic is supported (Bluetooth adapter exists),
 *         `false` otherwise.
 * @see tryAcquireBTAdapter
 */
fun Context.isBTSupported(): Boolean =
    packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

/**
 * Checks if the device supports Bluetooth Low Energy (LE).
 *
 * This function queries the system's package manager to determine if the
 * [PackageManager.FEATURE_BLUETOOTH_LE] system feature is declared,
 * which indicates support for Bluetooth LE.
 *
 * This is an extension function on [Context].
 *
 * @return `true` if Bluetooth Low Energy is supported, `false` otherwise.
 * @see android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE
 */
fun Context.isLESupported(): Boolean =
    packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

fun ESPContext.acquireBtAdapter(): BluetoothAdapter =
    appContext.tryAcquireBTAdapter() ?: throw BTUnsupported()