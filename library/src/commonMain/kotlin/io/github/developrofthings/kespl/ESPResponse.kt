package io.github.developrofthings.kespl

/**
 * A sealed interface representing the result of an ESP operation.
 * It can either be a [Success] containing the response data or a [Failure] containing an error.
 *
 * @param ESPResponseType The type of the successful response data.
 * @param ErrorType The type of the error data in case of failure.
 */
sealed interface ESPResponse<out ESPResponseType, out ErrorType> {

    /**
     * Represents a successful ESP operation.
     *
     * @param ESPResponseType The type of the successful response data.
     * @property data The actual response data from the ESP operation.
     */
    data class Success<ESPResponseType>(val data: ESPResponseType) :
        ESPResponse<ESPResponseType, Nothing>

    /**
     * Represents a failed ESP operation.
     *
     * @param ErrorType The type of the error data.
     * @property data The error information associated with the failure.
     */
    data class Failure<ErrorType>(val data: ErrorType) : ESPResponse<Nothing, ErrorType>
}

fun <ESPResponseType> ESPResponseType.asSuccess(): ESPResponse.Success<ESPResponseType> =
    ESPResponse.Success(this)

fun <ErrorType> ErrorType.asFailure(): ESPResponse.Failure<ErrorType> =
    ESPResponse.Failure(this)

inline fun <reified T, R> ESPResponse<T, ESPFailure>.unWrap(
    onFailure: (ESPFailure) -> R,
    onSuccess: (T) -> R,
) = when (this) {
    is ESPResponse.Failure -> onFailure(this.data)
    is ESPResponse.Success -> onSuccess(this.data)
}

internal inline fun <reified T> ESPResponse<T, ESPFailure>.onFailureResponse(
    onFailure: (ESPResponse.Failure<ESPFailure>) -> Unit,
) {
    if(this is ESPResponse.Failure) onFailure(this)
}

inline fun <reified T, R> ESPResponse<T, ESPFailure>.onSuccessResponse(
    onSuccess: (ESPResponse.Success<T>) ->  ESPResponse<R, ESPFailure>,
): ESPResponse<R, ESPFailure> = when (this) {
    is ESPResponse.Failure -> this
    is ESPResponse.Success -> onSuccess(this)
}

inline fun <reified T, R> ESPResponse<T, ESPFailure>.onFailure(
    onFailure: (ESPFailure) -> R,
) {
    if(this is ESPResponse.Failure) onFailure(this.data)
}

inline fun <reified T, R> ESPResponse<T, ESPFailure>.onSuccess(
    onSuccess: (T) ->  ESPResponse<R, ESPFailure>,
): ESPResponse<R, ESPFailure> = when (this) {
    is ESPResponse.Failure -> this
    is ESPResponse.Success -> onSuccess(this.data)
}

/**
 * Represents the possible failure states of an ESP operation.
 *
 * This sealed interface defines a set of specific error conditions that can occur
 * when interacting with an ESP device or service.
 */
sealed interface ESPFailure {
    /**
     * The operation timed out before completing.
     */
    data object TimedOut : ESPFailure

    /**
     * The attached Valentine One is not time slicing.
     */
    data object NotTimeSlicing : ESPFailure

    /**
     * The library has not determined the Valentine one in charge of the ESP bus.
     *
     * @see ESPDevice.ValentineOne
     * @see ESPDevice.ValentineOne.Unknown
     */
    data object V1NotDetermined : ESPFailure

    /**
     * The target [ESPDevice] sent a [ESPPacketId.RespRequestNotProcessed] in response to the
     * target request.
     */
    data object NotProcessed : ESPFailure
    /**
     * The client has detected that the target request is not supported by the Valentine One.
     * This is only used when attempting to send a request to the [ESPDevice.ValentineOne.Legacy].
     *
     * @see ESPDevice.ValentineOne
     * @see ESPDevice.ValentineOne.Legacy
     */
    data object NotSupported : ESPFailure

    /**
     * The target [ESPDevice] sent a [ESPPacketId.RespDataError] in response to the
     * target request.
     */
    data object DataError : ESPFailure

    /**
     * Attempted to send a request while the client is not connected to a
     * [io.github.developrofthings.kespl.bluetooth.V1connection].
     */
    data object NotConnected : ESPFailure

    /**
     * The Valentine One sent a [ESPPacketId.InfV1Busy] containing the target [ESPPacketId] in
     * response to the request.
     */
    data object V1Busy : ESPFailure

    /**
     * Attempted to send an invalid request while the ESP bus is operating in Legacy mode
     */
    data object LegacyMode : ESPFailure

    /**
     * The target [ESPDevice] didn't return an error but otherwise the request could not be
     * confirmed to have succeeded.
     */
    data object ESPOperationFailed : ESPFailure

    /**
     * The target [ESPDevice] sent a [ESPPacketId.RespSweepWriteResult] containing the index of the
     * first invalid sweep.
     * @property sweepNumber The number of the first invalid sweep.
     */
    data class InvalidSweep(val sweepNumber: Int) : ESPFailure

    /**
     * An unknown error occurred.
     */
    data class Unknown(val e: Throwable) : ESPFailure
}

internal val EmptySuccessESPResponse = Unit.asSuccess()