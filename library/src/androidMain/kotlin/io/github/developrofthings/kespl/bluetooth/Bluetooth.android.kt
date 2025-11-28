package io.github.developrofthings.kespl.bluetooth

import android.bluetooth.BluetoothAdapter

actual fun checkBluetoothAddress(address: String): Boolean =
    BluetoothAdapter.checkBluetoothAddress(address)