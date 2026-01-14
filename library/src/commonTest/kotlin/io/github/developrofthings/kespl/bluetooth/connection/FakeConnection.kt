package io.github.developrofthings.kespl.bluetooth.connection

import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.EmptySuccessESPResponse
import io.github.developrofthings.kespl.MutableESPFlowController
import io.github.developrofthings.kespl.asFailure
import io.github.developrofthings.kespl.asSuccess
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.isV1
import io.github.developrofthings.kespl.packet.ESPRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

internal class FakeConnection(
    initialConnectionType: V1cType = V1cType.LE,
    private val flowController: MutableESPFlowController,
    testScope: CoroutineScope,
) : IConnection {

    private var writeReturnResult: ESPResponse<Unit, ESPFailure> = EmptySuccessESPResponse

    override var canEchoQueue: Boolean = true

    override val connectionScope: CoroutineScope = testScope

    override val connectionStatus: StateFlow<ESPConnectionStatus>
        get() = flowController.connectionStatus

    override val espData: Flow<ByteArray>
        get() = flowController.espData

    override val noData: Flow<Unit>
        get() = flowController.noData

    override val notificationData: Flow<String>
        get() = flowController.notificationData

    override val isTimeSlicing: StateFlow<Boolean>
        get() = flowController.isTimeSlicing

    private val _connectionType: V1cType = initialConnectionType
    override val connectionType: V1cType get() = _connectionType

    override val hasV1Version: Boolean get() = flowController.v1Version.value != 0.0

    override val v1Version: StateFlow<Double>
        get() = flowController.v1Version

    override val hasDeterminedV1Type: Boolean
        get() = flowController.valentineOneType.value.isV1

    override val v1Type: StateFlow<ESPDevice.ValentineOne>
        get() = flowController.valentineOneType

    override suspend fun connect(
        v1c: V1connection,
        directConnect: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

    override suspend fun scan(scanMode: ESPScanMode): Flow<V1connectionScanResult> {
        TODO("Not yet implemented")
    }

    override suspend fun writeRequest(
        request: ESPRequest,
        waitForV1TypeDuration: Duration
    ): ESPResponse<Unit, ESPFailure> = writeReturnResult

    internal fun setWriteRequestFailure(
        reason: ESPFailure,
    ) {
        writeReturnResult = reason.asFailure()
    }

    internal fun clearWriteRequestFailure(
        reason: ESPFailure,
    ) {
        writeReturnResult = EmptySuccessESPResponse
    }
}