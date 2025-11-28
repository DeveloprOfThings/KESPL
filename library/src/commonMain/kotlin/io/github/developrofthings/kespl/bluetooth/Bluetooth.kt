package io.github.developrofthings.kespl.bluetooth

/**
 * Helper method for determining if a BluetoothDevice is a V1.
 *
 * @param name  The name of the BluetoothDevices to scan if it a V1.
 *
 * @return  Returns true if the BluetoothDevices is a V1.
 */
fun isV1Connection(name: String): Boolean =
    name.startsWith(LEGACY_V1C_NAME_PREFIX) || name.startsWith(V1C_NAME_PREFIX)


const val LEGACY_V1C_NAME_PREFIX: String = "V1connection-"
const val V1C_NAME_PREFIX: String = "V1c-LE-"

expect fun checkBluetoothAddress(address: String): Boolean

private fun String.trailingAddressOctets(): String {
    require(checkBluetoothAddress(this)) { "$this is not a valid Bluetooth address" }
    return this
        .substring(this.length - 5)
        .replace(":", "")
        .uppercase()
}

fun getLEV1CName(address: String): String = "${V1C_NAME_PREFIX}${address.trailingAddressOctets()}"

fun getLegacyV1CName(address: String): String =
    "${LEGACY_V1C_NAME_PREFIX}${address.trailingAddressOctets()}"

fun V1connection.computeDeviceName(): String = when (this) {
    is V1connection.Demo -> this.name
    is V1connection.Remote -> {
        if (this.type == V1cType.Legacy) getLegacyV1CName(id)
        else getLEV1CName(id)
    }
}