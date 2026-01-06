package io.github.developrofthings.kespl.packet

import io.github.developrofthings.kespl.CHECKSUM_FRAMING_BYTES
import io.github.developrofthings.kespl.DATA_LINK_ESCAPE_BYTE_5D
import io.github.developrofthings.kespl.DATA_LINK_ESCAPE_BYTE_5F
import io.github.developrofthings.kespl.DATA_LINK_ESCAPE_BYTE_7D
import io.github.developrofthings.kespl.DEST_IDX
import io.github.developrofthings.kespl.DEST_INDENTIFIER_BASE_CONST
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ESP_PACKET_EOF
import io.github.developrofthings.kespl.ESP_PACKET_SOF
import io.github.developrofthings.kespl.LEGACY_FRAMING_BYTES
import io.github.developrofthings.kespl.NO_CHECKSUM_FRAMING_BYTES
import io.github.developrofthings.kespl.ORIG_IDX
import io.github.developrofthings.kespl.ORIG_INDENTIFIER_BASE_CONST
import io.github.developrofthings.kespl.PACKET_DELIMITER_BYTE
import io.github.developrofthings.kespl.PACK_ID_IDX
import io.github.developrofthings.kespl.PAYLOAD_LEN_IDX
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.SOF_IDX
import io.github.developrofthings.kespl.collection.MutableByteList
import io.github.developrofthings.kespl.getPacketId
import io.github.developrofthings.kespl.isV1
import io.github.developrofthings.kespl.packet.data.displayData.AUX_0_INDEX
import io.github.developrofthings.kespl.packet.data.displayData.AUX_1_INDEX
import io.github.developrofthings.kespl.packet.data.displayData.BOGEY_COUNTER_IMG_1_INDEX
import io.github.developrofthings.kespl.packet.data.displayData.V1Mode
import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import kotlin.experimental.and

fun ByteArray.espPayloadToString(
    stringLength: Int,
): String = decodeToString(
    startIndex = PAYLOAD_START_IDX,
    endIndex = (PAYLOAD_START_IDX + stringLength),
)

/**
 * Indicates if the Valentine One is 'time slicing' (accessories will be given a "time slice" to
 * communicate on the ESP bus).
 */
val ByteArray.isTimeSlicing: Boolean get() = !this[PAYLOAD_START_IDX + AUX_0_INDEX].isBitSet(1)

/**
 * Indicates if the Valentine One is operating in Legacy mode.
 */
val ByteArray.isLegacy: Boolean get() = this[PAYLOAD_START_IDX + AUX_0_INDEX].isBitSet(6)


/**
 * Indicates if the Valentine One's is turned on.
 */
val ByteArray.isDisplayOn: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX].isBitSet(3)

/**
 * Indicates if the Valentine One's audio is muted.
 */
val ByteArray.isSoft: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX].isBitSet(0)

/**
 * Current [V1Mode] the Valentine One is operating in
 *
 * @since V4.1028
 */
val ByteArray.mode: V1Mode get() = V1Mode.from(aux1 = this[PAYLOAD_START_IDX + AUX_1_INDEX])

val ByteArray.bogeyCounterMode: V1Mode
    get() = V1Mode.fromBogeyCounter(
        bogeyCounter = this[PAYLOAD_START_IDX + BOGEY_COUNTER_IMG_1_INDEX]
    )

val ByteArray.payloadLength: Int get() = this[PAYLOAD_LEN_IDX].toInt()

val ByteArray.packetIdByte: Byte get() = this[PACK_ID_IDX]

val ByteArray.packetId: ESPPacketId get() = getPacketId(packetIdByte)

val ByteArray.originIdByte: Byte get() = this[ORIG_IDX]

fun ByteArray.isFromV1(): Boolean = originIdByte.let { origin ->
    origin == ESPDevice.ValentineOne.Checksum.originatorIdentifier ||
            origin == ESPDevice.ValentineOne.NoChecksum.originatorIdentifier ||
            origin == ESPDevice.ValentineOne.Legacy.originatorIdentifier
}

val ByteArray.originId: ESPDevice get() = when (originIdByte) {
    ESPDevice.RemoteDisplay.originatorIdentifier -> ESPDevice.RemoteDisplay
    ESPDevice.RemoteAudio.originatorIdentifier -> ESPDevice.RemoteAudio
    ESPDevice.SAVVY.originatorIdentifier -> ESPDevice.SAVVY
    ESPDevice.ThirdParty1.originatorIdentifier -> ESPDevice.ThirdParty1
    ESPDevice.ThirdParty2.originatorIdentifier -> ESPDevice.ThirdParty2
    ESPDevice.ThirdParty3.originatorIdentifier -> ESPDevice.ThirdParty3
    ESPDevice.V1connection.originatorIdentifier -> ESPDevice.V1connection
    ESPDevice.Reserved.originatorIdentifier -> ESPDevice.Reserved
    ESPDevice.GeneralBroadcast.originatorIdentifier -> ESPDevice.GeneralBroadcast
    ESPDevice.ValentineOne.NoChecksum.originatorIdentifier -> ESPDevice.ValentineOne.NoChecksum
    ESPDevice.ValentineOne.Checksum.originatorIdentifier -> ESPDevice.ValentineOne.Checksum
    ESPDevice.ValentineOne.Legacy.originatorIdentifier -> ESPDevice.ValentineOne.Legacy
    ESPDevice.ValentineOne.Unknown.originatorIdentifier -> ESPDevice.ValentineOne.Unknown
    else -> ESPDevice.UnknownDevice
}

val ByteArray.destinationIdByte: Byte get() = this[DEST_IDX]

val ByteArray.destinationId: ESPDevice get() = when (destinationIdByte) {
    ESPDevice.RemoteDisplay.destinationIdentifier -> ESPDevice.RemoteDisplay
    ESPDevice.RemoteAudio.destinationIdentifier -> ESPDevice.RemoteAudio
    ESPDevice.SAVVY.destinationIdentifier -> ESPDevice.SAVVY
    ESPDevice.ThirdParty1.destinationIdentifier -> ESPDevice.ThirdParty1
    ESPDevice.ThirdParty2.destinationIdentifier -> ESPDevice.ThirdParty2
    ESPDevice.ThirdParty3.destinationIdentifier -> ESPDevice.ThirdParty3
    ESPDevice.V1connection.destinationIdentifier -> ESPDevice.V1connection
    ESPDevice.Reserved.destinationIdentifier -> ESPDevice.Reserved
    ESPDevice.GeneralBroadcast.destinationIdentifier -> ESPDevice.GeneralBroadcast
    ESPDevice.ValentineOne.NoChecksum.destinationIdentifier -> ESPDevice.ValentineOne.NoChecksum
    ESPDevice.ValentineOne.Checksum.destinationIdentifier -> ESPDevice.ValentineOne.Checksum
    ESPDevice.ValentineOne.Legacy.destinationIdentifier -> ESPDevice.ValentineOne.Legacy
    ESPDevice.ValentineOne.Unknown.destinationIdentifier -> ESPDevice.ValentineOne.Unknown
    else -> ESPDevice.UnknownDevice
}

val ByteArray.isInfDisplayData: Boolean get() = packetIdByte == ESPPacketId.InfDisplayData.id

val ByteArray.isAlertData: Boolean get() = packetIdByte == ESPPacketId.RespAlertData.id

val ByteArray.isV1Version: Boolean get() = packetId == ESPPacketId.RespVersion && originId.isV1

val ByteArray.isDataError: Boolean get() = this.packetIdByte == ESPPacketId.RespDataError.id

val ByteArray.isUnsupportedPacket: Boolean get() = this.packetIdByte == ESPPacketId.RespUnsupportedPacket.id

val ByteArray.isRequestNotProcessed: Boolean get() = this.packetIdByte == ESPPacketId.RespRequestNotProcessed.id

val ByteArray.isV1Busy: Boolean get() = this.packetIdByte == ESPPacketId.InfV1Busy.id

val ByteArray.isForMe: Boolean
    get() = destinationIdByte.let {
        return@let it == ESPDevice.V1connection.destinationIdentifier
                || it == ESPDevice.GeneralBroadcast.destinationIdentifier
    }

val ByteArray.isUserBytes: Boolean get() = packetId == ESPPacketId.RespUserBytes

fun ByteArray.isValidFramingData(): Boolean {
    if ((this[SOF_IDX] != ESP_PACKET_SOF)) return false

    if ((this[lastIndex] != ESP_PACKET_EOF)) return false

    if ((this[DEST_IDX] and DEST_INDENTIFIER_BASE_CONST) != DEST_INDENTIFIER_BASE_CONST) return false

    if ((this[ORIG_IDX] and ORIG_INDENTIFIER_BASE_CONST) != ORIG_INDENTIFIER_BASE_CONST) return false

    return true
}

/**
 * Calculates an [ESPPacket] checksum ie summations of all bytes from Start of Frame to Payload Data
 * length minus 1.
 *
 * @return 8-bit summation of [ESPPacket] checksum-worthy bytes.
 */
private fun ByteArray.calculateESPChecksum(): Byte = calculateChecksum(start = 0, stop = (size - 2))
/**
 * Summation of all bytes from [start] (inclusive) to [stop] (end-exclusive) with no attention to
 * carries.
 *
 * @return 8-bit summation of bytes within the specified range.
 */
private fun ByteArray.calculateChecksum(
    start: Int = 0,
    stop: Int = size
): Byte {
    var checksum: Byte = this[start]
    for (i in (start + 1) until  stop)
        checksum = (checksum + this[i]).toByte()
    return checksum
}

/**
 * Validates if the [ESPPacket.checksum]
 */
internal fun ByteArray.validateChecksum(): Boolean = calculateESPChecksum() == this[size - 2]

fun ByteArray.toHexString(): String {
    // To prevent any possible array resizing we wanna pre-calc. the
    // size of the final string.
    val capacity: Int = (3 * size) - 1
    return buildString(capacity) {
        this@toHexString.forEachIndexed { i, it ->
            append(it.toHexString(format = HexFormat.UpperCase))
            if (i != this@toHexString.lastIndex) append(" ")
        }
    }.trimEnd()
}

internal val ByteArray.valentineOneType: ESPDevice.ValentineOne
    get() = when (originIdByte) {
        ESPDevice.ValentineOne.Checksum.originatorIdentifier -> ESPDevice.ValentineOne.Checksum
        ESPDevice.ValentineOne.NoChecksum.originatorIdentifier -> ESPDevice.ValentineOne.NoChecksum
        ESPDevice.ValentineOne.Legacy.originatorIdentifier -> ESPDevice.ValentineOne.Legacy
        else -> ESPDevice.ValentineOne.Unknown
    }

val ESPPacket.isInfDisplayData: Boolean get() = packetIdentifierByte == ESPPacketId.InfDisplayData.id

val ESPPacket.isAlertData: Boolean get() = packetIdentifierByte == ESPPacketId.RespAlertData.id

val ESPPacket.isVersion: Boolean get() = packetIdentifier == ESPPacketId.RespVersion

val ESPPacket.isV1Version: Boolean get() = isVersion && originatorIdentifier.isV1

val ESPPacket.isDataError: Boolean get() = this.packetIdentifierByte == ESPPacketId.RespDataError.id

val ESPPacket.isUnsupportedPacket: Boolean get() = this.packetIdentifierByte == ESPPacketId.RespUnsupportedPacket.id

val ESPPacket.isRequestNotProcessed: Boolean get() = this.packetIdentifierByte == ESPPacketId.RespRequestNotProcessed.id

val ESPPacket.isV1Busy: Boolean get() = this.packetIdentifierByte == ESPPacketId.InfV1Busy.id

val ESPPacket.isForMe: Boolean
    get() = destinationIdentifierByte.let {
        return@let it == ESPDevice.V1connection.destinationIdentifier
                || it == ESPDevice.GeneralBroadcast.destinationIdentifier
    }

val ESPPacket.isUserBytes: Boolean get() = packetIdentifier == ESPPacketId.RespUserBytes

val ESPPacket.isVolume: Boolean
    get() = this.packetIdentifierByte == ESPPacketId.RespCurrentVolume.id

val ESPPacket.isAllVolume: Boolean
    get() = this.packetIdentifierByte == ESPPacketId.RespAllVolume.id

val ESPPacket.isMaxSweepIndex: Boolean
    get() = this.packetIdentifierByte == ESPPacketId.RespMaxSweepIndex.id

val ESPPacket.isSweepSection: Boolean
    get() = this.packetIdentifierByte == ESPPacketId.RespSweepSections.id

internal fun tryAssemblyESPByteArrayFromLegacyDataBuffer(
    buffer: MutableByteList,
    v1Type: ESPDevice.ValentineOne,
): ByteArray? {
    if(buffer.isEmpty()) return null

    var firstPDIndex = -1
    var secondPDIndex = -1
    for (i in 0..< buffer.size) {
        val b = buffer[i]
        if (b == PACKET_DELIMITER_BYTE) {
            if (firstPDIndex == -1) firstPDIndex = i
            else {
                // Since we break from the loop as soon as we find the packet index, so if we've
                // found a second packet delimiter directly after the first packetStartIdx we've got
                // partial packet data in the buffer
                if (i == firstPDIndex + 1) firstPDIndex = i
                else {
                    secondPDIndex = i
                    break
                }
            }
        }
    }

    // We didn't find a packet start or end return and wait until the buffer has more data
    if(firstPDIndex == -1) {
        buffer.clear()
        return null
    }
    else if (secondPDIndex == -1) {
        return null
    }

    val packetLengthIdx = (firstPDIndex + 1)
    val (messageDataLength, messageStartIdx) = buffer[packetLengthIdx].let {
        when (it) {
            PACKET_DELIMITER_BYTE -> PACKET_DELIMITER_BYTE to (packetLengthIdx + 2)
            DATA_LINK_ESCAPE_BYTE_7D -> DATA_LINK_ESCAPE_BYTE_7D to (packetLengthIdx + 2)
            else -> it to (packetLengthIdx + 1)
        }
    }

    var calcMessageDataChecksum: Byte = messageDataLength
    /*
        Copy over the message data and remove any escaped bytes, accumulate the checksum as we go
     */
    var i = messageStartIdx
    val messageBytes = ByteArray(messageDataLength.toInt()).apply {
        var j = 0
        while (j < messageDataLength) {
            this[j++] = buffer[i++].let {
                when (it) {
                    PACKET_DELIMITER_BYTE -> PACKET_DELIMITER_BYTE
                    DATA_LINK_ESCAPE_BYTE_7D -> DATA_LINK_ESCAPE_BYTE_7D
                    else -> it
                }
            }.also { calcMessageDataChecksum = (calcMessageDataChecksum + it).toByte() }
        }
    }
    // Compare the packet checksums
    val messageDataChecksum = buffer[i].let {
        when (it) {
            PACKET_DELIMITER_BYTE -> PACKET_DELIMITER_BYTE
            DATA_LINK_ESCAPE_BYTE_7D -> DATA_LINK_ESCAPE_BYTE_7D
            else -> it
        }
    }

    if (calcMessageDataChecksum != messageDataChecksum) return null

    // Clear the processed bytes
    buffer.removeRange(
        fromIndex = 0,
        toIndex = secondPDIndex + 1,
    )

    if (!messageBytes.isValidFramingData()) return null

    val rawOrgId = (messageBytes[ORIG_IDX] - ORIG_INDENTIFIER_BASE_CONST).toByte()
    val useChecksum =
        rawOrgId == ESPDevice.ValentineOne.Checksum.id || v1Type == ESPDevice.ValentineOne.Checksum
    if(useChecksum) {
        val espChecksum = messageBytes[messageBytes.size - 2]
        val checksumAdjust = (messageDataLength + espChecksum + ESP_PACKET_EOF).toByte()
        val calculatedChecksum = (messageDataChecksum - checksumAdjust).toByte()
        if(calculatedChecksum != espChecksum) return null
    }

    return messageBytes
}

internal fun escapeBytesForLegacyWrite(bytes: ByteArray): ByteArray {
    // Determine the number of bytes we need to escape
    var escapeBytes = 0
    // We never escape ESP framing bytes so we can skip the edges
    for (i in 1..(bytes.lastIndex - 1)) {
        val b = bytes[i]
        if (b == DATA_LINK_ESCAPE_BYTE_7D || b == PACKET_DELIMITER_BYTE) escapeBytes++
    }

    val pLength = if(escapeBytes == 0) bytes.size + LEGACY_FRAMING_BYTES
    else bytes.size + escapeBytes + LEGACY_FRAMING_BYTES
    return ByteArray(pLength).apply {
        this[0] = PACKET_DELIMITER_BYTE
        this[1] = bytes.size.toByte()
        var j = 2
        for (i in 0 until bytes.size) {
            when(val b = bytes[i]) {
                PACKET_DELIMITER_BYTE -> {
                    this[j++] = PACKET_DELIMITER_BYTE
                    this[j++] = DATA_LINK_ESCAPE_BYTE_5F
                }
                DATA_LINK_ESCAPE_BYTE_7D -> {
                    this[j++] = DATA_LINK_ESCAPE_BYTE_7D
                    this[j++] = DATA_LINK_ESCAPE_BYTE_5D
                }
                else -> this[j++] = b
            }
        }
        this[pLength - 2] = (bytes.calculateChecksum() + bytes.size).toByte()
        this[pLength - 1] = PACKET_DELIMITER_BYTE
    }
}

fun assemblePayload(
    useChecksum: Boolean,
    destinationByte: Byte,
    originIdByte: Byte,
    packetIdByte: Byte,
    vararg payload: Byte,
): ByteArray = ByteArray(
    size = if (useChecksum) CHECKSUM_FRAMING_BYTES + payload.size
    else NO_CHECKSUM_FRAMING_BYTES + payload.size
).apply {
    this[SOF_IDX] = ESP_PACKET_SOF
    this[DEST_IDX] = destinationByte
    this[ORIG_IDX] = originIdByte
    this[PACK_ID_IDX] = packetIdByte
    this[PAYLOAD_LEN_IDX] = if (useChecksum) {
        (payload.size + CHECKSUM_BYTES).toByte()
    } else payload.size.toByte()
    // Copy payload
    payload.copyInto(
        destination = this,
        destinationOffset = PAYLOAD_START_IDX,
    )
    if (useChecksum) this[lastIndex - CHECKSUM_BYTES] = this.calculateESPChecksum()
    this[lastIndex] = ESP_PACKET_EOF
}

internal const val CHECKSUM_BYTES: Int = 1