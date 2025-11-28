@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GattNotConnectedException : ESPLeException("No GATT connection with a V1c-LE established")

class DescriptorWriteException(
    descriptorUUID: Uuid,
    status: Int,
) : ESPLeException("Descriptor($descriptorUUID) write failed \t GATT STATUS: $status")

class CharacteristicWriteException : ESPLeException()

class DescriptorNotFoundException(
    uuid: Uuid,
) : ESPLeException("Characteristic descriptor (uuid: $uuid )not found")


class ReadRemoteRssiException :
    ESPLeException("Something when wrong while attempting to read the remote Rssi value")

class ProfileServiceNotBound : ESPLeException()

class CharacteristicWriteNotAllowedException :
    ESPLeException("Writing to this characteristic is not supported!")

class CharacteristicWriteBusyException :
    ESPLeException("Attempting a characteristic write while remote device is busy!")

class FailedToDiscoverServices : ESPLeException()

class UnknownException(operation: GattOperation) :
    ESPLeException("An error occurred while performing operation: $operation")

enum class GattOperation {
    DiscoverServices,
    RemoteRssi,
    CharacteristicWrite,
    CharacteristicNotifications
}
