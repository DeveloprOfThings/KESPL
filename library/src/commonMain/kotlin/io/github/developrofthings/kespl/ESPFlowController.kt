package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.connection.demo.DemoConnection
import io.github.developrofthings.kespl.bluetooth.connection.staleDataWatchDogTimeout
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

/**
 * Collection of [Flow]s used for instrumenting ESP data communication.
 */
internal interface ESPFlowController {

    /**
     * A [Flow] of 'No Data' notifications that are emitted after [staleDataWatchDogTimeout]
     * has passed without any ESP data being received from the connected [V1connection].
     */
    val noData: Flow<Unit>

    /**
     * A [Flow] of notifications received from the connected [V1connection].
     *
     * Note: this is presently only used by [DemoConnection].
     */
    val notificationData: Flow<String>

    /**
     * A [StateFlow] representing the specific [ESPDevice.ValentineOne] hardware type.
     *
     * The type is determined based on the device's capabilities, such as support for ESP and or
     * checksums. __Note:__ the Valentine One may support checksums but if operating in "Legacy" the
     * library will report [ESPDevice.ValentineOne.Legacy].
     */
    val valentineOneType: StateFlow<ESPDevice.ValentineOne>

    /**
     * A [StateFlow] representing the firmware version of the connected Valentine One device.
     *
     * This value is determined through the ESP protocol and provides the specific
     * version number (e.g., 3.8945) of the hardware.
     */
    val v1Version: StateFlow<Double>

    /**
     * A [StateFlow] representing the current [ESPConnectionStatus] of the connection.
     */
    val connectionStatus: StateFlow<ESPConnectionStatus>

    /**
     * A [Flow] of raw ESP data bytes received from the connected [V1connection].
     */
    val espData: SharedFlow<ByteArray>

    /**
     * A [StateFlow] indicating whether the Valentine One has enabled time-slicing on the ESP bus.
     *
     * When active, devices attached to the bus are only permitted to communicate during their
     * respective assigned time-slices. This state is determined by observing the `TSHoldOff`
     * bit within `InfDisplayData` packets.
     *
     * @see io.github.developrofthings.kespl.packet.data.displayData.isTimeSlicing
     * @see io.github.developrofthings.kespl.packet.data.displayData.DisplayData.isTimeSlicing
     */
    val isTimeSlicing: StateFlow<Boolean>
}

/**
 * Mutable collection of [Flow]s used for instrumenting ESP data communication.
 */
internal interface MutableESPFlowController: ESPFlowController {

    override val notificationData: MutableSharedFlow<String>

    override val valentineOneType: MutableStateFlow<ESPDevice.ValentineOne>

    override val v1Version: MutableStateFlow<Double>

    override val connectionStatus: MutableStateFlow<ESPConnectionStatus>

    override val espData: MutableSharedFlow<ByteArray>

    override val noData: MutableSharedFlow<Unit>

    override val isTimeSlicing: MutableStateFlow<Boolean>

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
) : MutableESPFlowController {

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

