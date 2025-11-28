package io.github.developrofthings.kespl.utilities.extensions.flow

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.asFailure
import io.github.developrofthings.kespl.asSuccess
import io.github.developrofthings.kespl.packet.data.busyPacketIdBytes
import io.github.developrofthings.kespl.packet.destinationIdByte
import io.github.developrofthings.kespl.packet.isDataError
import io.github.developrofthings.kespl.packet.isRequestNotProcessed
import io.github.developrofthings.kespl.packet.isUnsupportedPacket
import io.github.developrofthings.kespl.packet.isV1Busy
import io.github.developrofthings.kespl.packet.originIdByte
import io.github.developrofthings.kespl.packet.packetIdByte
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/**
 * Checks if the current [ByteArray] (representing a received ESP packet) indicates that a
 * specific target request was not processed by the V1.
 *
 * This function is used to identify responses like "Request Not Processed", "Data Error",
 * or "Unsupported Packet" that pertain to a particular original request.
 *
 * @param targetPacketId The [ESPPacketId] of the original request packet that we are checking
 *                       the status for.
 * @return `true` if the current packet is a "Request Not Processed", "Data Error", or
 * "Unsupported Packet" response and its payload indicates it's related to the
 * [targetPacketId]. Returns `false` otherwise.
 *
 * @see ESPPacketId.RespRequestNotProcessed
 * @see ESPPacketId.RespDataError
 * @see ESPPacketId.RespUnsupportedPacket
 */
private fun ByteArray.isRequestNotProcessed(targetPacketId: ESPPacketId): Boolean =
    // Check to see if the target packet was not processed by the V1
    when (packetIdByte) {
        ESPPacketId.RespRequestNotProcessed.id,
        ESPPacketId.RespDataError.id,
        ESPPacketId.RespUnsupportedPacket.id,
            -> {
            this[PAYLOAD_START_IDX] == targetPacketId.id
        }

        else -> false
    }

private fun ByteArray.isV1BusyWithRequest(targetPacket: ESPPacketId): Boolean =
    if (isV1Busy) {
        targetPacket.id in this.busyPacketIdBytes()
    } else false


/**
 * Filters a [Flow] of [ByteArray] representing ESP packets to find a specific response
 * to a request, or an indication that the request was busy or not processed.
 *
 * This function first checks if the packet is intended for the current device (`isPacketForMe`).
 * Then, it determines if the packet is either:
 * 1. The target response, by matching the [responseOrigin] and [responsePacketId].
 * 2. A "busy" or "request not processed" status for the original request, by checking against
 *    the [requestOrigin] and [requestPacketId].
 *
 * @param requestDestination
 * @param requestPacketId The [ESPPacketId] of the original request packet.
 * @param responseOrigin The [ESPDevice] expected to send the response.
 * @param responsePacketId The [ESPPacketId] of the expected response packet.
 * @return A [Flow] of [ByteArray] containing only the packets that match the filter criteria.
 */
internal fun Flow<ByteArray>.filterForResponse(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    responseOrigin: ESPDevice,
    responsePacketId: ESPPacketId,
) = filter {
    if (!it.isPacketForMe()) return@filter false
    return@filter it.isTargetResponse(
        responseOrigin,
        responsePacketId
    ) || it.checkForBusyOrUnProcessedRequest(
        requestDestination = requestDestination,
        requestPacketId = requestPacketId,
    )
}

/**
 * Checks if the packet's destination ID matches the V1 connection or general broadcast.
 *
 * @return `true` if the packet is intended for the V1 connection or is a general broadcast, `false`
 * otherwise.
 */
internal fun ByteArray.isPacketForMe(): Boolean =
    (this.destinationIdByte == ESPDevice.V1connection.destinationIdentifier ||
            this.destinationIdByte == ESPDevice.GeneralBroadcast.destinationIdentifier)

/**
 * Checks if the current [ByteArray] (representing an ESP packet) is the target response
 * based on the expected origin and packet ID.
 *
 * @param responseOrigin The [ESPDevice] expected to be the originator of the response packet.
 * @param responsePacketId The [ESPPacketId] of the expected response packet.
 * @return `true` if the packet's origin ID matches [responseOrigin]'s originator identifier
 *         and the packet's ID byte matches [responsePacketId]'s ID; `false` otherwise.
 */
internal fun ByteArray.isTargetResponse(
    responseOrigin: ESPDevice,
    responsePacketId: ESPPacketId,
): Boolean {
    return (this.packetIdByte == responsePacketId.id) &&
            (responseOrigin.originatorIdentifier == this.originIdByte)
}

/**
 * Checks if the current [ByteArray] (representing an ESP packet) indicates that the V1
 * is busy with the specified [requestPacketId] or if the request associated with
 * [requestPacketId] was not processed.
 *
 * This function is used to determine if a received packet is an error response related
 * to a previously sent request.
 *
 * @param requestDestination
 * @param requestPacketId The [ESPPacketId] of the original request packet. This is used to
 *                        check if the V1 is busy with this specific request or if this
 *                        specific request was not processed.
 * @return `true` if the packet indicates the V1 is busy with the given request or the
 *         request was not processed, and the packet originated from the [requestOrigin].
 *         Returns `false` otherwise.
 */
internal fun ByteArray.checkForBusyOrUnProcessedRequest(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
): Boolean = (requestDestination.originatorIdentifier == this.originIdByte) &&
        (isV1BusyWithRequest(targetPacket = requestPacketId) ||
                isRequestNotProcessed(targetPacketId = requestPacketId))

/**
 * Transforms a Flow of [ByteArray] representing ESP packets into a Flow of [ESPResponse].
 *
 * This function processes incoming byte arrays, interpreting them as ESP packets.
 * It applies a provided transformation function (`transformPacket`) to valid data packets.
 * The transformation continues as long as `transformPacket` returns `null`, indicating
 * that a complete data unit has not yet been formed, and no critical errors are encountered.
 *
 * It specifically handles common ESP communication issues:
 * - **V1 Busy**: If the Valentine One (V1) device is busy, it allows for a configurable
 *   number of busy packets (`maxBusyPackets`) before emitting an [ESPFailure.V1Busy]
 *   and stopping the flow.
 * - **Request Not Processed**: If a packet indicates the V1 did not process a request,
 *   it emits an [ESPFailure.NotProcessed] and stops the flow.
 * - **Data Error**: If a data error packet is received, it emits an [ESPFailure.DataError]
 *   and stops the flow.
 * - **Unsupported Packet**: If an unsupported packet type is received, it emits an
 *   [ESPFailure.NotSupported] and stops the flow.
 *
 * If the `transformPacket` function successfully transforms a byte array into a [ResultType]
 * (returns a non-null value), an [ESPResponse.Success] containing that result is emitted,
 * and the flow collection stops for that transformation attempt. If `transformPacket`
 * returns `null`, it signals that more data is needed or the current packet isn't the
 * target, and the flow continues to process subsequent packets.
 *
 * @param ResultType The type of the data expected from a successful transformation.
 * @param transformPacket A lambda function that takes a [ByteArray] (ESP packet) and
 *                        attempts to transform it into a [ResultType].
 *                        - Returning a non-null [ResultType] indicates successful transformation,
 *                          and an [ESPResponse.Success] is emitted. The flow then stops.
 *                        - Returning `null` indicates that the current packet was processed but
 *                          did not result in a complete [ResultType] (e.g., waiting for more
 */
internal fun <ResultType> Flow<ByteArray>.transformPacketWhileNull(
    transformPacket: (ByteArray) -> ResultType?,
    maxBusyPackets: Int,
): Flow<ESPResponse<ResultType, ESPFailure>> {
    var busyCount = 0

    return transformWhile { it: ByteArray ->
        when {
            it.isV1Busy -> {
                if (busyCount++ >= maxBusyPackets) {
                    // We no longer want to continue collecting
                    emit(ESPFailure.V1Busy.asFailure())
                    false
                } else true
            }

            it.isRequestNotProcessed -> {
                emit(ESPFailure.NotProcessed.asFailure())
                // We no longer want to continue collecting
                false
            }

            it.isDataError -> {
                emit(ESPFailure.DataError.asFailure())
                // We no longer want to continue collecting
                false
            }

            it.isUnsupportedPacket -> {
                emit(ESPFailure.NotSupported.asFailure())
                // We no longer want to continue collecting
                false
            }

            else -> {
                transformPacket(it).let { data ->
                    if (data != null) emit(data.asSuccess())
                    data == null
                }
            }
        }
    }
}


/**
 * Filters a Flow of [ByteArray] representing ESP packets for a specific response or error
 * related to a request, and then transforms the matching packet into an [ESPResponse].
 *
 * This function combines the functionality of [filterForResponse] and [transformPacketWhileNull].
 * It first narrows down the stream of packets to those that are either the direct response
 * to a given request (matching [responseOrigin] and [responsePacketId]) or an error
 * packet (like "V1 Busy" or "Request Not Processed") related to the original request
 * (matching [requestDestination] and [requestPacketId]).
 *
 * Once a relevant packet is found, it attempts to transform it using the provided
 * `transformESPPacket` function.
 *
 * - If the packet is a direct response and `transformESPPacket` successfully converts it
 *   (returns a non-null [ResultType]), an [ESPResponse.Success] is emitted.
 * - If the packet indicates an error (V1 Busy, Not Processed, Data Error, Unsupported Packet),
 *   the corresponding [ESPFailure] is emitted.
 * - If `transformESPPacket` returns `null` for a non-error packet, it implies the packet
 *   was relevant but not the final piece of data needed, and the flow continues to look for
 *   more packets (unless an error like V1 Busy limit is hit).
 * - Any unexpected exceptions during the flow processing are caught and emitted as an
 *   [ESPFailure.Unknown].
 *
 * @param ResultType The type of data expected from a successful transformation of the response
 * packet.
 * @param requestDestination
 * @param requestPacketId The [ESPPacketId] of the original request packet. Used to identify
 *                        error packets and to ensure the response corresponds to this request.
 * @param responseOrigin The [ESPDevice] expected to send the response packet.
 * @param responsePacketId The [ESPPacketId] of the expected response packet.
 * @param maxBusyPackets The maximum number of consecutive "V1 Busy" packets to tolerate
 *                       before emitting an [ESPFailure.V1Busy] and stopping collection.
 */
internal fun <ResultType> Flow<ByteArray>.transformToResponseWhile(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    responseOrigin: ESPDevice,
    responsePacketId: ESPPacketId,
    maxBusyPackets: Int,
    transformESPPacket: (ByteArray) -> ResultType?,
): Flow<ESPResponse<ResultType, ESPFailure>> = filterForResponse(
    requestDestination = requestDestination,
    requestPacketId = requestPacketId,
    responseOrigin = responseOrigin,
    responsePacketId = responsePacketId,
)
    .transformPacketWhileNull(
        maxBusyPackets = maxBusyPackets,
        transformPacket = transformESPPacket,
    )
    .catch { e -> emit(ESPFailure.Unknown(e).asFailure()) }

/**
 * Suspends execution until a specific ESP response is received for a given request, or a timeout occurs.
 *
 * This function filters the incoming [Flow] of [ByteArray] (ESP packets) for a response
 * that matches the [responseOrigin] and [responsePacketId]. It also monitors for error
 * conditions related to the original [requestDestination] and [requestPacketId], such as
 * "V1 Busy" or "Request Not Processed".
 *
 * The function uses [transformToResponseWhile] to process the filtered packets. The provided
 * [transformESPPacket] lambda is applied to the target response packet to convert it into
 * the desired [ResultType].
 *
 * Execution is constrained by the [timeout] duration. If no conclusive response (success,
 * specific ESP failure, or timeout) is received within this period, an [ESPFailure.TimedOut]
 * is returned.
 *
 * @param ResultType The type of the data expected from a successful transformation of the response
 * packet.
 * @param requestDestination TODO
 * @param requestPacketId The [ESPPacketId] of the original request packet. Used to identify
 *                        error responses specific to this request.
 * @param responseOrigin The [ESPDevice] expected to send the response. Defaults to [requestDestination].
 * @param responsePacketId The [ESPPacketId] of the expected response packet.
 * @param timeout The maximum [Duration] to wait for a response.
 * @param maxBusyPackets The maximum number of consecutive "V1 Busy" packets related to the
 *                       [requestPacketId] to tolerate before considering it an [ESPFailure.V1Busy].
 *                       Defaults to 10.
 * @param transformESPPacket A lambda function that takes a [ByteArray] (the raw bytes of the ESP
 * response packet) and attempts to transform it into a [ResultType].
 *  - Returning a non-null [ResultType] indicates a successful transformation, and an
 *  [ESPResponse.Success] containing this result will be returned.
 *  - Returning `null` signifies that while the packet was the correct type
 */
internal suspend fun <ResultType> Flow<ByteArray>.waitForESPResponseForESPRequest(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    responseOrigin: ESPDevice = requestDestination,
    responsePacketId: ESPPacketId,
    timeout: Duration,
    maxBusyPackets: Int = 10,
    transformESPPacket: (ByteArray) -> ResultType?,
): ESPResponse<ResultType, ESPFailure> {
    try {
        return withTimeoutOrNull(timeout) {
            transformToResponseWhile(
                requestDestination = requestDestination,
                responseOrigin = responseOrigin,
                requestPacketId = requestPacketId,
                responsePacketId = responsePacketId,
                maxBusyPackets = maxBusyPackets,
                transformESPPacket = transformESPPacket,
            ).first<ESPResponse<ResultType, ESPFailure>>()
        }.let {
            when (it) {
                // The only time null is returned is when the timeout is reached
                null -> ESPFailure.TimedOut.asFailure()
                is ESPResponse.Success -> it
                is ESPResponse.Failure -> it
            }
        }
    }
    // This exception is thrown if a response isn't returned by the time flow collection stop
    // (timeout expired above)
    catch (_: NoSuchElementException) {
        return ESPFailure.TimedOut.asFailure()
    }
    // This exception when be thrown when the calling coroutine is cancelled, we don't need to
    // return anything in this situation so just rethrow
    catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return ESPFailure.Unknown(e).asFailure()
    }
}

/**
 * Waits for a specific number of response packets matching a given request.
 *
 * This function is a specialized version of [waitForESPResponseForESPRequest]. It waits
 * for a sequence of [responsePacketCount] ESP packets that match the specified
 * [responseOrigin] and [responsePacketId], which are expected as a response to an
 * original request defined by [requestDestination] and [requestPacketId].
 *
 * It uses an internal counter to track the number of received valid response packets.
 * Once [responsePacketCount] packets have been received, the last received packet's
 * raw [ByteArray] is returned as an [ESPResponse.Success].
 *
 * Common ESP error conditions are handled:
 * - **Timeout**: If the expected number of packets isn't received within the [timeout]
 *   duration, an [ESPFailure.TimedOut] is returned.
 * - **V1 Busy**: If too many "V1 Busy" packets (exceeding [maxBusyPackets]) are received
 *   before the target count is met, an [ESPFailure.V1Busy] is returned.
 * - **Request Not Processed/Data Error/Unsupported Packet**: If any of these error
 *   packets related to the original request are received, the corresponding [ESPFailure]
 *   is returned.
 *
 * @param requestDestination TODO
 * @param requestPacketId The [ESPPacketId] of the original request packet.
 * @param responseOrigin The [ESPDevice] expected to send the response packets. Defaults to
 *                       [requestOrigin].
 * @param responsePacketId The [ESPPacketId] of the expected response packets.
 * @param timeout The maximum [Duration] to wait for the specified number of response packets.
 * @param maxBusyPackets The maximum number of consecutive "V1 Busy" packets to tolerate before
 *                       failing with [ESPFailure.V1Busy]. Defaults to 10.
 * @param responsePacketCount The number of consecutive response packets to wait for.
 *                            Defaults to 2.
 * @return An [ESPResponse] which is:
 *         - [ESPResponse.Success] containing the [ByteArray] of the *last* received
 */
internal suspend fun Flow<ByteArray>.waitForResponseCountForESPRequest(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    responseOrigin: ESPDevice = requestDestination,
    responsePacketId: ESPPacketId,
    timeout: Duration,
    maxBusyPackets: Int = 10,
    responsePacketCount: Int = 2,
): ESPResponse<ByteArray, ESPFailure> {
    var responseDataCount = 0
    return waitForESPResponseForESPRequest(
        requestDestination = requestDestination,
        requestPacketId = requestPacketId,
        responseOrigin = responseOrigin,
        responsePacketId = responsePacketId,
        timeout = timeout,
        maxBusyPackets = maxBusyPackets,
    ) {
        if (++responseDataCount >= responsePacketCount) it else null
    }

}

/**
 * Waits for a specific number of `InfDisplayData` packets from a Valentine One device
 * in response to an observe request.
 *
 * This function is a specialized version of [waitForResponseCountForESPRequest] tailored
 * for observing `InfDisplayData` packets. It listens on the Flow of [ByteArray] (ESP packets)
 * for packets originating from the specified Valentine One ([v1]) device with the packet ID
 * [ESPPacketId.InfDisplayData].
 *
 * The function collects these `InfDisplayData` packets until the number of received packets
 * reaches [infDisplayDataCount]. Once this count is met, the last received packet's
 * [ByteArray] is returned wrapped in an [ESPResponse.Success].
 *
 * It handles common ESP communication scenarios:
 * - **Timeout**: If the expected number of packets isn't received within the specified [timeout],
 *   an [ESPFailure.TimedOut] is returned.
 * - **V1 Busy**: If the V1 device reports being busy for more than [maxBusyPackets] consecutive
 *   times, an [ESPFailure.V1Busy] is returned.
 * - **Other Errors**: Other ESP errors like "Request Not Processed", "Data Error", or
 *   "Unsupported Packet" will result in their corresponding [ESPFailure] types.
 *
 * @param requestDestination TODO
 * @param requestPacketId The [ESPPacketId] of the original observe request packet. This is
 *                        also used for correlating error messages.
 * @param v1 The specific [ESPDevice.ValentineOne] instance that is expected to send the
 *           `InfDisplayData` packets.
 * @param timeout The maximum [Duration] to wait for the specified number of `InfDisplayData`
 *                packets.
 * @param maxBusyPackets The maximum number of consecutive "V1 Busy" responses to tolerate
 *                       before failing with [ESPFailure.V1Busy]. Defaults to 10.
 * @param infDisplayDataCount The number of `InfDisplayData` packets to wait for before
 *                            considering the operation successful. Defaults to 2.
 *
 */
internal suspend fun Flow<ByteArray>.waitForNInfDisplayDataUnitObserveRequestFeedback(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    v1: ESPDevice.ValentineOne,
    timeout: Duration,
    maxBusyPackets: Int = 10,
    infDisplayDataCount: Int = 2,
): ESPResponse<ByteArray, ESPFailure> = waitForResponseCountForESPRequest(
    requestDestination = requestDestination,
    requestPacketId = requestPacketId,
    responseOrigin = v1,
    responsePacketId = ESPPacketId.InfDisplayData,
    timeout = timeout,
    maxBusyPackets = maxBusyPackets,
    responsePacketCount = infDisplayDataCount,
)

/**
 * Waits for an [ESPPacketId.InfDisplayData] packet from the specified [v1] device
 * that matches a custom condition, in response to an initial request.
 *
 * This function is a specialized version of [waitForESPResponseForESPRequest] tailored for
 * observing [ESPPacketId.InfDisplayData] packets. It waits for a response packet that
 * originates from the given [v1] and has the packet ID [ESPPacketId.InfDisplayData].
 * The key difference is the `transformESPPacket` lambda, which allows for custom validation
 * of the received `InfDisplayData` packet.
 *
 * The function will wait for a packet that satisfies the `transformESPPacket` predicate
 * (returns `true`). If such a packet is received within the [timeout] period,
 * an [ESPResponse.Success] containing the raw [ByteArray] of that packet is returned.
 *
 * If the [timeout] is reached before a matching packet is found, an [ESPFailure.TimedOut]
 * is returned. Other [ESPFailure] types (like [ESPFailure.V1Busy], [ESPFailure.NotProcessed],
 * etc.) can also be returned if encountered during the process.
 *
 * @param requestDestination TODO
 * @param requestPacketId The [ESPPacketId] of the original request packet. This is used to
 *                        correlate error responses (like "V1 Busy") back to this specific request.
 * @param v1 The specific [ESPDevice.ValentineOne] instance from which the `InfDisplayData`
 *           is expected.
 * @param timeout The maximum [Duration] to wait for a matching `InfDisplayData` packet.
 * @param maxBusyPackets The maximum number of consecutive "V1 Busy" packets to tolerate before
 *                       considering it an error. Defaults to 10.
 * @param infDisplayDataPredicate TODO
 */
suspend fun Flow<ByteArray>.waitForMatchingInfDisplayDataObserveRequestFeedback(
    requestDestination: ESPDevice,
    requestPacketId: ESPPacketId,
    v1: ESPDevice.ValentineOne,
    timeout: Duration,
    maxBusyPackets: Int = 10,
    infDisplayDataPredicate: (ByteArray) -> Boolean,
): ESPResponse<ByteArray, ESPFailure> = waitForESPResponseForESPRequest(
    requestDestination = requestDestination,
    requestPacketId = requestPacketId,
    responseOrigin = v1,
    responsePacketId = ESPPacketId.InfDisplayData,
    timeout = timeout,
    maxBusyPackets = maxBusyPackets,
    transformESPPacket = {
        if (infDisplayDataPredicate(it)) it else null
    },
)