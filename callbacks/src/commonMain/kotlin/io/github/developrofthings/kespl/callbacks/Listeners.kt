package io.github.developrofthings.kespl.callbacks

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.alert.AlertData
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData

// Marker interface
interface ESPCallback

fun interface ESPPacketListener: ESPCallback {
    fun onPacket(packet: ESPPacket)
}

fun interface DisplayDataListener: ESPCallback {
    fun onDisplayData(display: DisplayData)
}

fun interface AlertTableListener: ESPCallback {
    fun onAlertTable(table: List<AlertData>)
}

interface NoDataListener: ESPCallback {
    fun onNoData(): Unit
}

fun interface NotificationListener: ESPCallback  {
    fun onNotification(notification: String)
}

fun interface ESPConnectionStatusListener: ESPCallback  {
    fun onConnectionStatusChange(status: ESPConnectionStatus)
}