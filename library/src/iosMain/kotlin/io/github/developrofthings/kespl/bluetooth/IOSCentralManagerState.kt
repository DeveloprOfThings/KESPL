package io.github.developrofthings.kespl.bluetooth

import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported

enum class IOSCentralManagerState(val value: Long) {
    PoweredOff(value = CBManagerStatePoweredOff),
    PoweredOn(value = CBManagerStatePoweredOn),
    Resetting(value = CBManagerStateResetting),
    Unauthorized(value = CBManagerStateUnauthorized),
    Unknown(value = CBManagerStateUnknown),
    Unsupported(value = CBManagerStateUnsupported);

    companion object {
        fun fromCBManagerStateLong(value: Long): io.github.developrofthings.kespl.bluetooth.IOSCentralManagerState = when (value) {
            PoweredOff.value -> PoweredOff
            PoweredOn.value -> PoweredOn
            Resetting.value -> Resetting
            Unauthorized.value -> Unauthorized
            Unknown.value -> Unknown
            Unsupported.value -> Unsupported
            else -> Unknown
        }
    }
}