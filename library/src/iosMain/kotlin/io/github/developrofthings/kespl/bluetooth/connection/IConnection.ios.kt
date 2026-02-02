package io.github.developrofthings.kespl.bluetooth.connection

import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.packet.ESPRequest
import kotlinx.coroutines.flow.Flow

/**
 * Attempts to establish a connection with the provided [V1connection].
 *
 * @param v1c The [V1connection] to attempt a connection.
 */
suspend fun IConnection.connect(v1c: V1connection): Boolean =
    connect(v1c = v1c, directConnect = true)


/**
 * Scans for nearby [V1connection]s based on the provided [scanMode].
 */
suspend fun IConnection.scan(): Flow<V1connectionScanResult> =
    scan(scanMode = ESPScanMode.LowPower)

/**
 * Attempts to write [request] to the connected [V1connection].
 * @param request The [ESPRequest] to be sent.
 *
 * @return [ESPResponse] indicating the success or failure of the request.
 */
suspend fun IConnection.writeRequest(request: ESPRequest): ESPResponse<Unit, ESPFailure> =
    writeRequest(request = request, waitForV1TypeDuration = defaultWriteTimeout)