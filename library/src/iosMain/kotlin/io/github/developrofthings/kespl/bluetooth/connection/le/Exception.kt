@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PeripheralNotConnectedException : ESPLeException("No connection with a V1c-LE established")

class ReadRemoteRssiException :
    ESPLeException("Something when wrong while attempting to read the remote Rssi value")

class CharacteristicWriteException : ESPLeException()

class UnknownException(operation: CoreBluetoothOperation) :
    ESPLeException("An error occurred while performing operation: $operation")

class FailedToDiscoverServices : ESPLeException()

class FailedToDiscoverServiceCharacteristics : ESPLeException()

class EnableNotificationsException(
    enable: Boolean,
    uuid: Uuid,
) : ESPLeException(
    "Failed to ${if(enable) "enable" else "disable"} notifications for CBCharacteristic(${uuid.toString()})"
)

enum class CoreBluetoothOperation {
    DiscoverServices,
    DiscoverServiceCharacteristics,
    RemoteRssi,
    CharacteristicWrite,
    CharacteristicNotifications,
}