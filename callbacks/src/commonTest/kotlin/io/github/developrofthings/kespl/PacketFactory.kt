package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.packet.assemblePayload

fun createPacketArray(
    useChecksum: Boolean,
    destinationByte: Byte,
    packetIdByte: Byte,
    originIdByte: Byte = ESPDevice.ValentineOne.Checksum.originatorIdentifier,
    vararg payload: Byte,
): ByteArray = assemblePayload(
    useChecksum = useChecksum,
    destinationByte = destinationByte,
    originIdByte = originIdByte,
    packetIdByte = packetIdByte,
    payload = payload,
)

fun createPacketArray(
    useChecksum: Boolean,
    destination: ESPDevice,
    packetId: ESPPacketId,
    origin: ESPDevice = ESPDevice.ValentineOne.Checksum,
    vararg payload: Byte,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destinationByte = destination.destinationIdentifier,
    packetIdByte = packetId.id,
    originIdByte = origin.originatorIdentifier,
    payload = payload,
)

fun createAlertDataPacket(alert: ByteArray): ByteArray = assemblePayload(
    useChecksum = true,
    destinationByte = ESPDevice.GeneralBroadcast.destinationIdentifier,
    originIdByte = ESPDevice.ValentineOne.Checksum.originatorIdentifier,
    packetIdByte = ESPPacketId.RespAlertData.id,
    payload = alert,
)

fun create3AlertTable(): List<ByteArray> = listOf(
    createAlertDataPacket(alert = alertTableFirstAlert),
    createAlertDataPacket(alert = alertTableSecondAlert),
    createAlertDataPacket(alert = alertTableThirdAlert)
)

fun createInfDisplayDataPacket(
    payload: ByteArray = defaultDisplayPayload
): ByteArray = assemblePayload(
    useChecksum = true,
    destinationByte = ESPDevice.GeneralBroadcast.destinationIdentifier,
    originIdByte = ESPDevice.ValentineOne.Checksum.originatorIdentifier,
    packetIdByte = ESPPacketId.InfDisplayData.id,
    payload = payload,
)

internal val defaultDisplayPayload: ByteArray = byteArrayOf(0x77, 0x77, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00)
private val alertTableFirstAlert: ByteArray = byteArrayOf(
    0x13.toByte(),
    0x29.toByte(),
    0x2A.toByte(),
    0x9B.toByte(),
    0x3F.toByte(),
    0x28.toByte(),
    0x0.toByte(),
)
private val alertTableSecondAlert: ByteArray = byteArrayOf(
    0x23.toByte(),
    0x5E.toByte(),
    0x6B.toByte(),
    0x8B.toByte(),
    0x3F.toByte(),
    0x24.toByte(),
    0x01.toByte(),
)
private val alertTableThirdAlert: ByteArray = byteArrayOf(
    0x33.toByte(),
    0x83.toByte(),
    0xEA.toByte(),
    0xA9.toByte(),
    0x3F.toByte(),
    0x22.toByte(),
    0x81.toByte(),
)