package io.github.developrofthings.kespl.utilities.extensions

import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import io.github.developrofthings.kespl.bluetooth.ESPBluetoothStatusCode

internal fun Int.toStatusCode(): ESPBluetoothStatusCode {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) when (this) {
        BluetoothStatusCodes.SUCCESS -> ESPBluetoothStatusCode.Success
        BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> ESPBluetoothStatusCode.ErrorMissingBluetoothConnectPermission
        // For whatever reason this constant is 'hidden' so we will default to our constant
        /*BluetoothStatusCodes.ERROR_DEVICE_NOT_CONNECTED -> ESPBluetoothStatusCode.ERROR_DEVICE_NOT_CONNECTED*/
        ERROR_DEVICE_NOT_CONNECTED -> ESPBluetoothStatusCode.ErrorDeviceNotConnected
        BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND -> ESPBluetoothStatusCode.ErrorProfileServiceNotBound
        BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED -> ESPBluetoothStatusCode.ErrorGattWriteNotAllowed
        BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY -> ESPBluetoothStatusCode.ErrorGattWriteRequestBusy
        BluetoothStatusCodes.ERROR_UNKNOWN -> ESPBluetoothStatusCode.ErrorUnknown
        else -> ESPBluetoothStatusCode.ErrorUnknown
    }
    else when (this) {
        SUCCESS -> ESPBluetoothStatusCode.Success
        ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> ESPBluetoothStatusCode.ErrorMissingBluetoothConnectPermission
        ERROR_DEVICE_NOT_CONNECTED -> ESPBluetoothStatusCode.ErrorDeviceNotConnected
        ERROR_PROFILE_SERVICE_NOT_BOUND -> ESPBluetoothStatusCode.ErrorProfileServiceNotBound
        ERROR_GATT_WRITE_NOT_ALLOWED -> ESPBluetoothStatusCode.ErrorGattWriteNotAllowed
        ERROR_GATT_WRITE_REQUEST_BUSY -> ESPBluetoothStatusCode.ErrorGattWriteRequestBusy
        ERROR_UNKNOWN -> ESPBluetoothStatusCode.ErrorUnknown
        else -> ESPBluetoothStatusCode.ErrorUnknown
    }
}

private const val SUCCESS: Int = 0
private const val ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION: Int = 6
private const val ERROR_DEVICE_NOT_CONNECTED: Int = 4
private const val ERROR_PROFILE_SERVICE_NOT_BOUND: Int = 9
private const val ERROR_GATT_WRITE_NOT_ALLOWED: Int = 200
private const val ERROR_GATT_WRITE_REQUEST_BUSY: Int = 201
private const val ERROR_UNKNOWN: Int = Int.MAX_VALUE