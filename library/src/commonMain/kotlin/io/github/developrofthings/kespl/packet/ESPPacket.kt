package io.github.developrofthings.kespl.packet

import io.github.developrofthings.kespl.DEST_IDX
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ORIG_IDX
import io.github.developrofthings.kespl.PACK_ID_IDX
import io.github.developrofthings.kespl.PAYLOAD_LEN_IDX
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.getPacketId
import kotlin.jvm.JvmInline

/**
 * Well structured data packet used for all ESP communication. A packet consist of 6 framing bytes
 * and `0-N` payload bytes.
 *
 * There are two possible packet formats that all ESP-capable devices must
 * support: **Checksum** and **Non-Checksum**. Packet format is established by the Valentine One
 * controlling the bus.
 *
 * **Non-Checksum Packet Format**
 *
 * | Byte #          | Name | Value         | Description                           |
 * |-----------------|------|---------------|---------------------------------------|
 * | 0               | SOF  | $AA           | Start of Frame                        |
 * | 1               | DI   | $D0 + Dest ID | Destination Identifier                |
 * | 2               | OI   | $E0 + Send ID | Originator Identifier                 |
 * | 3               | PI   | $XX           | Packet Identifier                     |
 * | 4               | PL   | $XX           | Payload Length                        |
 * | 5 : PL + 5 - 1  | PD   | $XX           | Payload Data (Not present if PL = 0)  |
 * | 5 + PL          | EOF  | $AB           | End of Frame                          |
 *
 *
 * **Checksum Packet Format**
 *
 * | Byte #         | Name | Value         | Description                          |
 * |----------------|------|---------------|--------------------------------------|
 * | 0              | SOF  | $AA           | Start of Frame                       |
 * | 1              | DI   | $D0 + Dest ID | Destination Identifier               |
 * | 2              | OI   | $E0 + Send ID | Originator Identifier                |
 * | 3              | PI   | $XX           | Packet Identifier                    |
 * | 4              | PL   | $XX           | Payload Length                       |
 * | 5 : PL + 5 - 2 | PD   | $XX           | Payload Data (Not present if PL = 0) |
 * | 5 + PL - 1     | CS   | $XX           | Packet Checksum                      |
 * | 5 + PL         | EOF  | $AB           | End of Frame                         |
 *
 * __Thin-wrapper around [ByteArray]__
 */
@JvmInline
value class ESPPacket(val bytes: ByteArray) {

    /**
     * 8-bit value indicating the length of this packet's payload (includes checksum).
     */
    val payloadLength: Int get() = this[PAYLOAD_LEN_IDX].toInt()

    /**
     * [Byte] representation of the this packet's packet identifier
     *
     * @see ESPPacketId
     */
    val packetIdentifierByte: Byte get() = this[PACK_ID_IDX]

    /**
     * The [ESPPacketId] for this packet.
     */
    val packetIdentifier: ESPPacketId get() = getPacketId(packetIdentifierByte)

    /**
     * [Byte] representation of this packet's originator identifier.
     *
     * @see ESPDevice
     */
    val originatorIdentifierByte: Byte get() = this[ORIG_IDX]

    /**
     * The [ESPDevice] of the sender of this packet.
     */
    val originatorIdentifier: ESPDevice get() = bytes.originId

    /**
     * [Byte] representation of this packet's destination identifier.
     *
     * @see ESPDevice
     */
    val destinationIdentifierByte: Byte get() = this[DEST_IDX]

    /**
     * The [ESPDevice] for which this packet is destined.
     */
    val destinationIdentifier: ESPDevice get() = bytes.destinationId

    /**
     * 8 bit summations of all bytes from Start of Frame to Payload Data length minus 1.
     *
     * __NOTE:__ [ESPPacket] cannot itself determine if the ESP bus is
     * operating in checksum mode so the value of this field can be inaccurate. It is the job of
     * callers to determine if the attached Valentine One is operating in "checksum mode" before
     * calling.
     */
    val checksum: Byte get() = this[bytes.size - 2]

    /**
     * Returns the data ([Byte]) at the specified index.
     *
     * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [index] is out of range
     * of this array indices.
     *
     * @return data at [index].
     */
    operator fun get(index: Int): Byte = bytes[index]

    /**
     * Copies the specified range of data from this packet into the [destination].
     *
     * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or
     * `startIndex + length` is out of range of this array indices.
     * @throws IndexOutOfBoundsException when the subrange doesn't fit into the [destination] array
     * starting at the specified [destinationOffset], or when that index is out of the [destination]
     * array indices range.
     *
     * @return the destination array.
     */
    fun copyInto(
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = PAYLOAD_START_IDX,
        length: Int = destination.size,
    ): ByteArray {
        bytes.copyInto(
            destination = destination,
            destinationOffset = destinationOffset,
            startIndex = startIndex,
            endIndex = startIndex + length
        )
        return destination
    }

    /**
     * Payload data of this packet (includes checksum).
     *
     * @return payload data.
     */
    fun payload(): ByteArray = ByteArray(size = payloadLength).apply {
        bytes.copyInto(destination = this)
    }

    /**
     * Returns a deep-copy of ourself.
     */
    fun copy(): ESPPacket = ESPPacket(bytes.copyOf())

    override fun toString(): String = bytes.toHexString()
}