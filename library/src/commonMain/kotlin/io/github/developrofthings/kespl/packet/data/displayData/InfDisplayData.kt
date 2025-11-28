package io.github.developrofthings.kespl.packet.data.displayData

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet

typealias InfDisplayData = ESPPacket

fun ByteArray.displayData(): DisplayData = DisplayData(
    bytes = ByteArray(displayByteCount).apply {
        this@displayData.copyInto(
            destination = this,
            destinationOffset = 0,
            startIndex = PAYLOAD_START_IDX,
            endIndex = PAYLOAD_START_IDX + this.size
        )
    },
)

fun InfDisplayData.displayData(): DisplayData = this.bytes.displayData()

/**
 * Indicates if the Valentine One's audio is muted.
 */
@Suppress("unused")
val InfDisplayData.isSoft: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(SOFT_BIT_INDEX)

/**
 * Indicates if the Valentine One is 'time slicing' (accessories will be given a "time slice" to
 * communicate on the ESP bus).
 */
@Suppress("unused")
val InfDisplayData.isTimeSlicing: Boolean
    get() = !this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(TS_HOLDOFF_BIT_INDEX)

/**
 * Indicates if the Valentine One has successfully signed on and is actively searching for alerts.
 */
@Suppress("unused")
val InfDisplayData.isSearchingForAlerts: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(SYSTEM_STATUS_BIT_INDEX)

/**
 * Indicates if the Valentine One's is turned on.
 */
@Suppress("unused")
val InfDisplayData.isDisplayOn: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(DISPLAY_MODE_BIT_INDEX)

/**
 * Indicates if the Valentine One is operating in Euro Mode.
 */
val InfDisplayData.isEuro: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(EURO_MODE_BIT_INDEX)

/**
 * Indicates if custom sweeps have been defined and will be used when the Valentine One is
 * operating in Euro mode i.e. [InfDisplayData.isEuro] `==` `true`.
 */
@Suppress("unused")
val InfDisplayData.isCustomSweep: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(CUSTOM_SWEEP_BIT_INDEX)

/**
 * Indicates if the Valentine One is operating in Legacy mode.
 */
@Suppress("unused")
val InfDisplayData.isLegacy: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(ESP_LEGACY_BIT_INDEX)

/**
 * Indicates if the Valentine One is operating in Legacy mode.
 */
@Suppress("unused")
val InfDisplayData.isDisplayActive: Boolean
    get() = this[PAYLOAD_START_IDX + AUX_0_INDEX]
        .isBitSet(DISPLAY_ACTIVE_BIT_INDEX)

/**
 * Current [V1Mode] the Valentine One is operating in
 *
 * @since V4.1028
 */
@Suppress("unused")
val InfDisplayData.mode: V1Mode
    get() = V1Mode.from(aux1 = this[PAYLOAD_START_IDX + AUX_1_INDEX])

@Suppress("unused")
val InfDisplayData.bogeyCounterMode: V1Mode
    get() = V1Mode.fromBogeyCounter(
        bogeyCounter = this[PAYLOAD_START_IDX + BOGEY_COUNTER_IMG_1_INDEX]
    )

private const val displayByteCount: Int = 8