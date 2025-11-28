package io.github.developrofthings.kespl.packet

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId

private val emptyByteArray: ByteArray = ByteArray(0)

/**
 * Represents an ESP request to be sent to an ESP device.
 *
 * @property destination The [ESPDevice] that this request is intended for.
 * @property requestId The [ESPPacketId] of this request, identifying the type of request.
 * @property payload The data associated with this request. Defaults to an empty byte array.
 */
data class ESPRequest(
    val destination: ESPDevice,
    val requestId: ESPPacketId,
    val payload: ByteArray = emptyByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ESPRequest

        if (destination != other.destination) return false
        if (requestId != other.requestId) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + requestId.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    internal fun toWritablePayload(
        useChecksum: Boolean,
    ): ByteArray = assemblePayload(
        useChecksum = useChecksum,
        destinationByte = destination.destinationIdentifier,
        originIdByte = ESPDevice.V1connection.originatorIdentifier,
        packetIdByte = requestId.id,
        payload = payload
    )
}

/**
 * Utility function for determining if this request is for a [ESPDevice.V1connection].
 */
fun ESPRequest.isForV1c(): Boolean = this.destination == ESPDevice.V1connection