package io.github.developrofthings.kespl.utilities

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.packet.assemblePayload
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.packet.data.sweep.toPayload

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

fun createPacketArray(
    useChecksum: Boolean,
    destination: ESPDevice,
    packetId: ESPPacketId,
    payload: String,
    origin: ESPDevice = ESPDevice.V1connection,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = destination,
    origin = origin,
    packetId = packetId,
    payload = payload.encodeToByteArray(),
)

fun createV1BusyPacket(
    useChecksum: Boolean,
    vararg busyIds: Byte,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = ESPDevice.V1connection,
    packetId = ESPPacketId.InfV1Busy,
    origin = if (useChecksum) {
        ESPDevice.ValentineOne.Checksum
    } else ESPDevice.ValentineOne.NoChecksum,
    payload = busyIds
)

fun createV1BusyPacket(
    useChecksum: Boolean,
    vararg busyIds: ESPPacketId,
): ByteArray = createV1BusyPacket(
    useChecksum = useChecksum,
    busyIds = busyIds.map { it.id }.toByteArray()
)

fun createNotProcessedPacket(
    useChecksum: Boolean,
    vararg unprocessedIds: Byte,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = ESPDevice.V1connection,
    packetId = ESPPacketId.RespRequestNotProcessed,
    origin = if (useChecksum) {
        ESPDevice.ValentineOne.Checksum
    } else ESPDevice.ValentineOne.NoChecksum,
    payload = unprocessedIds
)

fun createNotProcessedPacket(
    useChecksum: Boolean,
    vararg unprocessedIds: ESPPacketId,
): ByteArray = createNotProcessedPacket(
    useChecksum = useChecksum,
    unprocessedIds = unprocessedIds.map { it.id }.toByteArray()
)

fun createDataErrorPacket(
    useChecksum: Boolean,
    packetId: Byte,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = ESPDevice.V1connection,
    packetId = ESPPacketId.RespDataError,
    origin = if (useChecksum) {
        ESPDevice.ValentineOne.Checksum
    } else ESPDevice.ValentineOne.NoChecksum,
    packetId
)

fun createDataErrorPacket(
    useChecksum: Boolean,
    packetId: ESPPacketId,
): ByteArray = createDataErrorPacket(
    useChecksum = useChecksum,
    packetId = packetId.id,
)

fun createUnsupportedPacket(
    useChecksum: Boolean,
    packetId: Byte,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = ESPDevice.V1connection,
    packetId = ESPPacketId.RespUnsupportedPacket,
    origin = if (useChecksum) {
        ESPDevice.ValentineOne.Checksum
    } else ESPDevice.ValentineOne.NoChecksum,
    packetId
)

fun createUnsupportedPacket(
    useChecksum: Boolean,
    packetId: ESPPacketId,
): ByteArray = createUnsupportedPacket(
    useChecksum = useChecksum,
    packetId = packetId.id,
)

fun createInfDisplayDataPacket(
    useChecksum: Boolean,
    displayData: ByteArray = defaultDisplayPayload,
): ByteArray = assemblePayload(
    useChecksum = useChecksum,
    destinationByte = ESPDevice.GeneralBroadcast.destinationIdentifier,
    originIdByte = if(useChecksum) {
        ESPDevice.ValentineOne.Checksum.originatorIdentifier
    } else ESPDevice.ValentineOne.NoChecksum.originatorIdentifier,
    packetIdByte = ESPPacketId.InfDisplayData.id,
    payload = displayData,
)


fun createRespSweepSectionsPacket(
    useChecksum: Boolean,
    sections: List<SweepSection> = defaultSections,
): ByteArray = createPacketArray(
    useChecksum = useChecksum,
    destination = ESPDevice.V1connection,
    origin = if (useChecksum) ESPDevice.ValentineOne.Checksum
    else ESPDevice.ValentineOne.NoChecksum,
    packetId = ESPPacketId.RespSweepSections,
    payload = sections.toPayload(),
)

fun createRespSweepDefinitionPackets(
    useChecksum: Boolean,
    default: Boolean = false,
    sweepsDefinitions: List<SweepDefinition> = defaultSweeps,
): List<ByteArray> = sweepsDefinitions.map {
    createPacketArray(
        useChecksum = useChecksum,
        destination = ESPDevice.V1connection,
        origin = if (useChecksum) ESPDevice.ValentineOne.Checksum
        else ESPDevice.ValentineOne.NoChecksum,
        packetId = if (default) ESPPacketId.RespDefaultSweepDefinitions else ESPPacketId.RespSweepDefinition,
        payload = it.toPayload(commit = false)
    )
}

internal val defaultSweeps: List<SweepDefinition> = listOf(
    SweepDefinition(index = 0, lowerEdge = 33400, upperEdge = 36002),
    SweepDefinition(index = 1, lowerEdge = 23910, upperEdge = 24250),
    SweepDefinition(index = 2, lowerEdge = 0, upperEdge = 0),
    SweepDefinition(index = 3, lowerEdge = 0, upperEdge = 0),
    SweepDefinition(index = 4, lowerEdge = 0, upperEdge = 0),
    SweepDefinition(index = 5, lowerEdge = 0, upperEdge = 0),
)

internal val defaultSections: List<SweepSection> = listOf(
    SweepSection(indexCount = 0x12, lowerEdge = 33400, upperEdge = 36002),
    SweepSection(indexCount = 0x22, lowerEdge = 33360, upperEdge = 36051),
)

internal val defaultDisplayPayload: ByteArray = byteArrayOf(0x77, 0x77, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00)
internal val displayOffDisplayPayload: ByteArray = byteArrayOf(0x77, 0x77, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00)