package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.connection.demo.DemoConnection
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Collection of [Flow]s used for instrumenting ESP data communication.
 */
internal interface ESPFlowController {

    /**
     * Stream of 'No Data' notifications that are emitted after the library has detected a 'No Data'
     * state.
     */
    val noData: Flow<Unit>

    /**
     * Stream of "ESP" notification messages.
     * Note: this is presently only used by [DemoConnection].
     */
    val notificationData: Flow<String>

    val valentineOneType: StateFlow<ESPDevice.ValentineOne>

    val v1Version: StateFlow<Double>

    val connectionStatus: StateFlow<ESPConnectionStatus>

    /**
     * Stream of ESP data.
     * Note: use [Flow.map] operator to convert to
     * [io.github.developrofthings.kespl.packet.ESPPacket].
     */
    val espData: SharedFlow<ByteArray>

    val isTimeSlicing: StateFlow<Boolean>
}

/**
 * Mutable collection of [Flow]s used for instrumenting ESP data communication.
 */
internal interface MutableESPFlowController {

    val notificationData: MutableSharedFlow<String>

    val valentineOneType: MutableStateFlow<ESPDevice.ValentineOne>

    val v1Version: MutableStateFlow<Double>

    val connectionStatus: MutableStateFlow<ESPConnectionStatus>

    val espData: MutableSharedFlow<ByteArray>

    val noData: MutableSharedFlow<Unit>

    val isTimeSlicing: MutableStateFlow<Boolean>

    fun forceStatefulDefaults() {
        v1Version.tryEmit(value = 0.0)
        valentineOneType.tryEmit(value = ESPDevice.ValentineOne.Unknown)
        connectionStatus.tryEmit(value = ESPConnectionStatus.Disconnected)
        isTimeSlicing.tryEmit(value = false)
    }
}

internal class ImplESPFlowController(
    defaultV1Type: ESPDevice.ValentineOne = ESPDevice.ValentineOne.Unknown,
    defaultV1Version: Double = 0.0,
    defaultConnectionStatus: ESPConnectionStatus = ESPConnectionStatus.Disconnected,
    defaultIsTimeSlicing: Boolean = false,
) : ESPFlowController, MutableESPFlowController {

    override val notificationData = MutableSharedFlow<String>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val valentineOneType = MutableStateFlow(value = defaultV1Type)

    override val v1Version = MutableStateFlow(value = defaultV1Version)

    override val connectionStatus = MutableStateFlow(value = defaultConnectionStatus)

    override val espData = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val noData = MutableSharedFlow<Unit>()

    override val isTimeSlicing: MutableStateFlow<Boolean> =
        MutableStateFlow(value = defaultIsTimeSlicing)
}

@Single(binds = [ESPFlowController::class, MutableESPFlowController::class])
internal fun getFlowController(): ESPFlowController = ImplESPFlowController()

