package io.github.developrofthings.kespl.bluetooth.connection.le

import platform.CoreBluetooth.CBPeripheral

sealed interface ESPCoreBluetoothEvent {

    val peripheral: CBPeripheral
}