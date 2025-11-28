package io.github.developrofthings.kespl.bluetooth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readString

/**
 * Sealed interface representing a `physical` V1connection (RFCOMM/SPP or LE).
 * This can either be a [Remote] connection to a physical Bluetooth device or a [Demo] connection.
 */
sealed interface V1connection {

    val id: String

    val name: String

    val type: V1cType

    /**
     * Indicates if this [V1connection] can be persisted.
     */
    val canSave: Boolean

    /**
     * Represents a remote V1connection backed by a bluetooth connection to transmit ESP data.
     *
     * @property device The underlying [BluetoothDevice] for this connection.
     * @property type The [V1cType] of this connection.
     */
    data class Remote(
        internal val device: BTDevice,
        override val type: V1cType,
    ) : V1connection {

        override val id: String
            get() = device.id

        override val name: String
            get() = device.name.takeIf { !it.isNullOrEmpty() } ?: computeDeviceName()

        override val canSave: Boolean
            get() = true
    }


    /**
     * Represents a 'mock' V1connection where all ESP data is provided by [demoESP].
     *
     * @property demoESP String containing Hex ESP data.
     */
    data class Demo(val demoESP: String) : V1connection {
        override val id: String
            get() = demoDeviceAddress

        override val name: String
            get() = demoDeviceName

        override val type: V1cType
            get() = V1cType.Demo

        override val canSave: Boolean
            get() = false

        companion object {

            suspend fun fromInputStream(`is`: RawSource): V1connection = Demo(
                demoESP = withContext(Dispatchers.IO) {
                    `is`.buffered().readString()
                },
            )
        }
    }
}

/**
 * Represents the result of a [V1connection] scan.
 *
 * @property rssi The received signal strength indicator (RSSI) of the scanned device.
 * @property device The [V1connection] that was scanned.
 */
class V1connectionScanResult(
    val rssi: Int,
    val device: V1connection,
) {

    val id: String get() = device.id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as V1connectionScanResult

        return id == other.id
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}


private const val demoDeviceName: String = "V1Connection Demo"
private const val demoDeviceAddress: String = "A1:B2:C3:D4:E5:F6"