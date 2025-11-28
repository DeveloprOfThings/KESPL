package io.github.developrofthings.kespl.bluetooth

import platform.CoreBluetooth.CBPeripheral

actual class BTDevice(val realDevice: CBPeripheral) {
    actual val name: String?
        get() = realDevice.name

    actual val id: String
        get() = realDevice.identifier.UUIDString

}