package io.github.developrofthings.kespl.bluetooth

internal enum class ESPBluetoothStatusCode(val code: Int) {
    /**
     * API call was successful
     */
    Success(0),

    /**
     * Error code indicating that the caller does not have the [android.Manifest.permission.BLUETOOTH_CONNECT] permission.
     */
    ErrorMissingBluetoothConnectPermission(6),

    /**
     * Error code indicating that the Bluetooth Device specified is not connected, but is bonded.
     */
    ErrorDeviceNotConnected(4),

    /**
     * Error code indicating that the profile service is not bound. You can bind a profile service by calling [BluetoothAdapter.getProfileProxy].
     */
    ErrorProfileServiceNotBound(9),

    /**
     * A GATT writeCharacteristic request is not permitted on the remote device.
     */
    ErrorGattWriteNotAllowed(200),

    /**
     * A GATT writeCharacteristic request is issued to a busy remote device
     */
    ErrorGattWriteRequestBusy(201),

    /**
     * Indicates that an unknown error has occurred.
     */
    ErrorUnknown(Int.MAX_VALUE),
}