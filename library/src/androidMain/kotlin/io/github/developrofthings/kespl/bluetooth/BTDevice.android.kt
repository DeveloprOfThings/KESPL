package io.github.developrofthings.kespl.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

actual class BTDevice(val realDevice: BluetoothDevice) {

    actual val name: String?
        @SuppressLint("MissingPermission")
        get() = realDevice.name

    actual val id: String
        get() = realDevice.address
}