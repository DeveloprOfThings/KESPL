package io.github.developrofthings.kespl.callbacks

import io.github.developrofthings.kespl.IESPClient
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.alert.AlertData
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.utilities.getDefaultScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val _legacyCallbackScope = getDefaultScope()

private data class CallbackEntry<TCallback : ESPCallback>(
    val callbacks: List<TCallback>,
    val job: Job,
)

private enum class ESPCallbackType {
    ConnectionStatus,
    NoData,
    Packet,
    Notification,
    Display,
    Alert,
}

private val _callbackRegistry = mutableMapOf<ESPCallbackType, CallbackEntry<ESPCallback>>()

@Suppress("UNCHECKED_CAST")
private inline fun <reified TCallback : ESPCallback> Map<ESPCallbackType, CallbackEntry<ESPCallback>>.getCallbacks(
    key: ESPCallbackType
): List<TCallback> = (get(key) as CallbackEntry<TCallback>)
    .callbacks

fun IESPClient.registerConnectionListener(listener: ESPConnectionStatusListener): Unit =
    registerConnectionListener(listener = listener, scope = _legacyCallbackScope)

internal fun IESPClient.registerConnectionListener(
    scope: CoroutineScope,
    listener: ESPConnectionStatusListener,
) = registerCallback<ESPConnectionStatus, ESPConnectionStatusListener>(
    client = this,
    callbackKey = ESPCallbackType.ConnectionStatus,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.connectionStatus },
) { status, callbacks ->
    callbacks.forEach { cb -> cb.onConnectionStatusChange(status = status) }
}

fun IESPClient.unregisterConnectionListener(listener: ESPConnectionStatusListener) =
    unregisterlistener(
        callbackKey = ESPCallbackType.ConnectionStatus,
        listener = listener,
    )

fun IESPClient.unregisterConnectionListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.ConnectionStatus)

fun IESPClient.registerNoDataListener(
    listener: NoDataListener,
) = registerNoDataListener(listener = listener, scope = _legacyCallbackScope)

internal fun IESPClient.registerNoDataListener(
    scope: CoroutineScope,
    listener: NoDataListener,
) = registerCallback<Unit, NoDataListener>(
    client = this,
    callbackKey = ESPCallbackType.NoData,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.noData },
) { _, callbacks ->
    callbacks.forEach { cb -> cb.onNoData() }
}

fun IESPClient.unregisterNoDataListener(listener: NoDataListener) = unregisterlistener(
    callbackKey = ESPCallbackType.NoData,
    listener = listener,
)

fun IESPClient.unregisterNoDataListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.NoData)

fun IESPClient.registerPacketListener(
    listener: ESPPacketListener,
) = registerPacketListener(
    listener = listener,
    scope = _legacyCallbackScope,
)

internal fun IESPClient.registerPacketListener(
    scope: CoroutineScope,
    listener: ESPPacketListener,
) = registerCallback<ESPPacket, ESPPacketListener>(
    client = this,
    callbackKey = ESPCallbackType.Packet,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.packets },
) { packet, callbacks ->
    callbacks.forEach { cb -> cb.onPacket(packet = packet) }
}

fun IESPClient.unregisterPacketListener(listener: ESPPacketListener) = unregisterlistener(
    callbackKey = ESPCallbackType.Packet,
    listener = listener,
)

fun IESPClient.unregisterPacketListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.Packet)


fun IESPClient.registerNotificationListener(
    listener: NotificationListener,
) = registerNotificationListener(
    listener = listener,
    scope = _legacyCallbackScope,
)

internal fun IESPClient.registerNotificationListener(
    scope: CoroutineScope,
    listener: NotificationListener,
) = registerCallback<String, NotificationListener>(
    client = this,
    callbackKey = ESPCallbackType.Notification,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.notificationData },
) { notification, callbacks ->
    callbacks.forEach { cb -> cb.onNotification(notification = notification) }
}

fun IESPClient.unregisterNotificationListener(listener: NotificationListener) = unregisterlistener(
    callbackKey = ESPCallbackType.Notification,
    listener = listener,
)

fun IESPClient.unregisterNotificationListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.Notification)

fun IESPClient.registerDisplayDataListener(
    listener: DisplayDataListener,
) = registerDisplayDataListener(
    listener = listener,
    scope = _legacyCallbackScope,
)

internal fun IESPClient.registerDisplayDataListener(
    scope: CoroutineScope,
    listener: DisplayDataListener,
) = registerCallback<DisplayData, DisplayDataListener>(
    client = this,
    callbackKey = ESPCallbackType.Display,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.displayData },
) { display, callbacks ->
    callbacks.forEach { cb -> cb.onDisplayData(display = display) }
}

fun IESPClient.unregisterDisplayDataListener(listener: DisplayDataListener) = unregisterlistener(
    callbackKey = ESPCallbackType.Display,
    listener = listener,
)

fun IESPClient.unregisterDisplayDataListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.Display)

fun IESPClient.registerAlertTableListener(listener: AlertTableListener) =
    registerAlertTableListener(
        listener = listener,
        scope = _legacyCallbackScope
    )

internal fun IESPClient.registerAlertTableListener(
    scope: CoroutineScope,
    listener: AlertTableListener,
) = registerCallback<List<AlertData>, AlertTableListener>(
    client = this,
    callbackKey = ESPCallbackType.Alert,
    listener = listener,
    collectionScope = scope,
    flowSelector = { it.alertTable },
) { table, callbacks ->
    callbacks.forEach { cb -> cb.onAlertTable(table = table) }
}

fun IESPClient.unregisterAlertTableListener(listener: AlertTableListener) = unregisterlistener(
    callbackKey = ESPCallbackType.Alert,
    listener = listener,
)

fun IESPClient.unregisterAlertTableListeners() =
    unregisterlisteners(callbackKey = ESPCallbackType.Alert)

private inline fun <reified TFlow, reified TCallback : ESPCallback> registerCallback(
    client: IESPClient,
    callbackKey: ESPCallbackType,
    listener: ESPCallback,
    collectionScope: CoroutineScope,
    flowSelector: (IESPClient) -> Flow<TFlow>,
    crossinline onDataEmitted: (TFlow, List<TCallback>) -> Unit,
) {
    _callbackRegistry[callbackKey] =
            // Update existing callback entry
        _callbackRegistry[callbackKey]?.let { existing ->
            existing.copy(
                callbacks = existing.callbacks.toMutableList().apply {
                    // Prevent duplicates
                    if (!contains(listener)) add(listener)
                }
            )
        } ?: CallbackEntry(
            callbacks = listOf(listener),
            job = flowSelector(client)
                .onEach { emittedData: TFlow ->
                    onDataEmitted(
                        emittedData,
                        _callbackRegistry
                            .getCallbacks<TCallback>(key = callbackKey)
                    )
                }
                .launchIn(scope = collectionScope)
        )
}

fun unregisterAllListeners() {
    _callbackRegistry.values.forEach { entry ->
        entry.job.cancel()
    }
    _callbackRegistry.clear()
}

private fun unregisterlistener(
    callbackKey: ESPCallbackType,
    listener: ESPCallback,
) {
    _callbackRegistry[callbackKey] = _callbackRegistry[callbackKey]?.let { existing ->
        existing.copy(
            callbacks = existing.callbacks.toMutableList().apply {
                remove(element = listener)
            }
        ).also {
            // If we don't have any callbacks cancel the job
            if (it.callbacks.isEmpty()) {
                it.job.cancel()
                _callbackRegistry.remove(key = callbackKey)
                return
            }
        }
    } ?: return
}

private fun unregisterlisteners(
    callbackKey: ESPCallbackType,
) {
    _callbackRegistry[callbackKey]?.also { existing ->
        existing.job.cancel()
        _callbackRegistry.remove(key = callbackKey)
    }
}