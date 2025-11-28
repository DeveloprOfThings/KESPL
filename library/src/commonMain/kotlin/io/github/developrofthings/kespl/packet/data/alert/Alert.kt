package io.github.developrofthings.kespl.packet.data.alert

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.emptyByte
import io.github.developrofthings.kespl.packet.ESPPacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

typealias ResponseAlertData = ESPPacket

fun ResponseAlertData.alertData(): AlertData = AlertData(
    bytes = ByteArray(ALERT_DATA_BYTE_COUNT).apply {
        this@alertData.copyInto(
            destination = this,
            destinationOffset = 0,
            length = this@apply.size
        )
    },
)

val emptyAlert: AlertData = AlertData(ByteArray(ALERT_DATA_BYTE_COUNT))

/**
 * Non-zero based index of this alert in the corresponding alert table.
 */
val AlertData.index: Int get() = alertIndexCount.index

/**
 * Zero based index of this alert in the corresponding alert table.
 */
val AlertData.indexZB: Int get() = (alertIndexCount.index - 1)

val AlertData.count: Int get() = alertIndexCount.count

private fun ByteArray.isAlertTableEmpty(): Boolean = this[PAYLOAD_START_IDX] == emptyByte

fun ByteArray.alertData(): AlertData = AlertData(
    bytes = ByteArray(ALERT_DATA_BYTE_COUNT).apply {
        this@alertData.copyInto(
            destination = this,
            destinationOffset = 0,
            startIndex = PAYLOAD_START_IDX,
            endIndex = PAYLOAD_START_IDX + ALERT_DATA_BYTE_COUNT
        )
    },
)

internal fun Flow<ByteArray>.alertTable(): Flow<List<AlertData>> = flow {
    val alerts = MutableList(15) { emptyAlert }
    var size = 0
    var expectedTableSize = 0
    collect {
        if (it.isAlertTableEmpty()) {
            emit(emptyList())
            return@collect
        }

        val alert = it.alertData()
        // We received an alert for different table
        if (size != 0 && expectedTableSize != alert.count) {
            // Clear old table
            alerts.fill(emptyAlert)
            size = 0
            // Set the expected table size
            expectedTableSize = alert.count
        }

        expectedTableSize = alert.count
        alerts[alert.indexZB] = alert
        size++
        if (size == alert.count) {
            emit(alerts.copy(count = size))
            alerts.fill(emptyAlert)
            size = 0
            expectedTableSize = 0
        }
    }
}

fun <T> List<T>.copy(start: Int = 0, count: Int = (size - start)): List<T> = buildList {
    for (i in start..<count) {
        add(this@copy[i])
    }
}

internal const val ALERT_DATA_BYTE_COUNT: Int = 7