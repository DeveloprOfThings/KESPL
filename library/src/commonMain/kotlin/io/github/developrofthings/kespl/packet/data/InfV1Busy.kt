package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.getPacketId
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.originIdByte
import io.github.developrofthings.kespl.packet.payloadLength

typealias InfV1Busy = ESPPacket
private val ByteArray.valentineOneType: ESPDevice.ValentineOne
    get() = when (originIdByte) {
        ESPDevice.ValentineOne.Checksum.originatorIdentifier -> ESPDevice.ValentineOne.Checksum
        ESPDevice.ValentineOne.NoChecksum.originatorIdentifier -> ESPDevice.ValentineOne.NoChecksum
        ESPDevice.ValentineOne.Legacy.originatorIdentifier -> ESPDevice.ValentineOne.Legacy
        else -> ESPDevice.ValentineOne.Unknown
    }

fun InfV1Busy.busyPacketIdBytes(): ByteArray = this.bytes.busyPacketIdBytes()

/**
 * Returns an array of packet identifiers of pending requests to the Valentine One. An empty array
 * is returned if the Valentine One has no pending requests.
 *
 * @return packet identifiers of pending requests.
 *
 * @see ESPPacketId
 */
fun ByteArray.busyPacketIdBytes(): ByteArray = when(valentineOneType) {
    ESPDevice.ValentineOne.Checksum -> {
        ByteArray(payloadLength - 1).apply {
            this@busyPacketIdBytes.copyInto(
                destination = this,
                destinationOffset = 0,
                startIndex = PAYLOAD_START_IDX,
                endIndex = (PAYLOAD_START_IDX + this.size),
            )
        }
    }

    ESPDevice.ValentineOne.NoChecksum -> {
        ByteArray(payloadLength).apply {
            this@busyPacketIdBytes.copyInto(
                destination = this,
                destinationOffset = 0,
                startIndex = PAYLOAD_START_IDX,
                endIndex = PAYLOAD_START_IDX + this.size
            )
        }
    }

    ESPDevice.ValentineOne.Legacy -> ByteArray(0)
    ESPDevice.ValentineOne.Unknown -> ByteArray(0)
}

/**
 * Returns a [List] of [ESPPacketId] of pending requests to the Valentine One. An empty list
 * is returned if the Valentine One has no pending requests.
 */
fun InfV1Busy.busyPacketIds(): List<ESPPacketId> = busyPacketIdBytes().map { getPacketId(it) }