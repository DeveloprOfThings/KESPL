package io.github.developrofthings.kespl.callbacks

import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import io.github.developrofthings.kespl.FakeESPClient
import io.github.developrofthings.kespl.IESPClient
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.create3AlertTable
import io.github.developrofthings.kespl.createInfDisplayDataPacket
import io.github.developrofthings.kespl.defaultDisplayPayload
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.displayData.displayData
import io.github.developrofthings.kespl.utils.custom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IESPClientTest {

    private val _connectionStatus: MutableStateFlow<ESPConnectionStatus> =
        MutableStateFlow(value = ESPConnectionStatus.Disconnected)
    private val _espData: MutableSharedFlow<ByteArray> = MutableSharedFlow()
    private val _noData: MutableSharedFlow<Unit> = MutableSharedFlow()
    private val _notifications: MutableSharedFlow<String> = MutableSharedFlow()

    private val _fakeClient: IESPClient = FakeESPClient(
        connectionStatusSource = _connectionStatus,
        espDataSource = _espData,
        noDataSource = _noData,
        notificationDataSource = _notifications,
    )

    @Test
    fun registerNoDataListener_isClearedBy_unregisterNoDataListeners() = runTest {
        val noDataCallback = mock<NoDataListener>(mode = MockMode.autoUnit)
        _fakeClient.registerNoDataListener(scope = this, listener = noDataCallback)
        advanceUntilIdle()
        _noData.emit(Unit) // Event 1
        _fakeClient.unregisterNoDataListeners() // Unregister
        _noData.emit(Unit) // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(1)) { noDataCallback.onNoData() }
    }

    @Test
    fun registerNoDataListener_isClearedBy_unregisterSpecificListener() = runTest {
        val noDataCallback = mock<NoDataListener>(mode = MockMode.autoUnit)
        _fakeClient.registerNoDataListener(scope = this, listener = noDataCallback)
        advanceUntilIdle()
        _noData.emit(Unit) // Event 1
        _fakeClient.unregisterNoDataListener(noDataCallback) // Unregister
        _noData.emit(Unit) // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(1)) { noDataCallback.onNoData() }
    }

    @Test
    fun registerNotificationListener_isClearedBy_unregisterNotificationListeners() = runTest {
        val notificationCallback = mock<NotificationListener>(mode = MockMode.autoUnit)
        _fakeClient.registerNotificationListener(scope = this, listener = notificationCallback)
        advanceUntilIdle()
        _notifications.emit(value = "Example notification message #1") // Event 1
        _fakeClient.unregisterNotificationListeners() // Unregister
        _notifications.emit(value = "Example notification message #2") // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { notificationCallback.onNotification(notification = "Example notification message #1") }
        verify(mode = exactly(n = 0)) { notificationCallback.onNotification(notification = "Example notification message #2") }
    }

    @Test
    fun registerNotificationListener_isClearedBy_unregisterSpecificListener() = runTest {
        val notificationCallback = mock<NotificationListener>(mode = MockMode.autoUnit)
        _fakeClient.registerNotificationListener(scope = this, listener = notificationCallback)
        advanceUntilIdle()
        _notifications.emit(value = "Example notification message #1") // Event 1
        _fakeClient.unregisterNotificationListener(notificationCallback) // Unregister
        _notifications.emit(value = "Example notification message #2") // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { notificationCallback.onNotification(notification = "Example notification message #1") }
        verify(mode = exactly(n = 0)) { notificationCallback.onNotification(notification = "Example notification message #2") }
    }

    @Test
    fun registerConnectionListener_isClearedBy_unregisterConnectionListeners() = runTest {
        val connStatusCallback = mock<ESPConnectionStatusListener>(mode = MockMode.autoUnit)
        _fakeClient.registerConnectionListener(scope = this@runTest, listener = connStatusCallback)
        // It takes some time to start flow collection (coroutine) so we want to advance the test
        // scheduler until all schedule tasks are run
        advanceUntilIdle()
        _connectionStatus.emit(value = ESPConnectionStatus.Connected) // Event 1
        // It may take some cycles for the flow to emit the new status so we want to advance the
        // test scheduler until all schedule tasks are run so that we don't unregister the callback
        // and miss the status emission
        advanceUntilIdle()
        _fakeClient.unregisterConnectionListeners() // Unregister
        // Emit another status and make sure it isn't observed in the listener
        _connectionStatus.emit(value = ESPConnectionStatus.ConnectionLost) // Event 2 (Should be ignored)
        advanceUntilIdle()
        // The underlying data source is a StateFlow so it will always emit the initial state
        // (ESPConnectionStatus.Disconnected)
        verify(mode = exactly(n = 1)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.Disconnected) }
        verify(mode = exactly(n = 1)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.Connected) }
        verify(mode = exactly(n = 0)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.ConnectionLost) }
    }

    @Test
    fun registerConnectionListener_isClearedBy_unregisterSpecificListener() = runTest {
        val connStatusCallback = mock<ESPConnectionStatusListener>(mode = MockMode.autoUnit)
        _fakeClient.registerConnectionListener(scope = this@runTest, listener = connStatusCallback)
        // It takes some time to start flow collection (coroutine) so we want to advanced the test
        // scheduler until all schedule tasks are run
        advanceUntilIdle()
        _connectionStatus.emit(value = ESPConnectionStatus.Connected) // Event 1
        // It may take some cycles for the flow to emit the new status so we want to advance the
        // test scheduler until all schedule tasks are run so that we don't unregister the callback
        // and miss the status emission
        advanceUntilIdle()
        _fakeClient.unregisterConnectionListener(connStatusCallback) // Unregister
        // Emit another status and make sure it isn't observed in the listener
        _connectionStatus.emit(value = ESPConnectionStatus.ConnectionLost) // Event 2 (Should be ignored)
        advanceUntilIdle()
        // The underlying data source is a StateFlow so it will always emit the initial state
        // (ESPConnectionStatus.Disconnected)
        verify(mode = exactly(n = 1)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.Disconnected) }
        verify(mode = exactly(n = 1)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.Connected) }
        verify(mode = exactly(n = 0)) { connStatusCallback.onConnectionStatusChange(status = ESPConnectionStatus.ConnectionLost) }
    }

    @Test
    fun registerPacketListener_isClearedBy_unregisterPacketListeners() = runTest {
        val packetCallback = mock<ESPPacketListener>(mode = MockMode.autoUnit)
        _fakeClient.registerPacketListener(scope = this@runTest, listener = packetCallback)
        advanceUntilIdle()

        val infDisplayData = createInfDisplayDataPacket()
        _espData.emit(value = infDisplayData) // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterPacketListeners() // Unregister

        val infDisplayDataAlt = createInfDisplayDataPacket(
            payload = defaultDisplayPayload
                .clone()
                .also { it.fill(0xFF.toByte()) }
        )
        _espData.emit(value = infDisplayDataAlt)  // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { packetCallback.onPacket(packet = ESPPacket(bytes = infDisplayData)) }
        verify(mode = exactly(n = 0)) { packetCallback.onPacket(packet = ESPPacket(bytes = infDisplayDataAlt)) }
    }

    @Test
    fun registerPacketListener_isClearedBy_unregisterSpecificListener() = runTest {
        val packetCallback = mock<ESPPacketListener>(mode = MockMode.autoUnit)
        _fakeClient.registerPacketListener(scope = this@runTest, listener = packetCallback)
        advanceUntilIdle()

        val infDisplayData = createInfDisplayDataPacket()
        _espData.emit(value = infDisplayData) // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterPacketListener(packetCallback) // Unregister

        val infDisplayDataAlt = createInfDisplayDataPacket(
            payload = defaultDisplayPayload
                .clone()
                .also { it.fill(0xFF.toByte()) }
        )
        _espData.emit(value = infDisplayDataAlt)  // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { packetCallback.onPacket(packet = ESPPacket(bytes = infDisplayData)) }
        verify(mode = exactly(n = 0)) { packetCallback.onPacket(packet = ESPPacket(bytes = infDisplayDataAlt)) }
    }

    @Test
    fun registerDisplayDataListener_isClearedBy_unregisterPacketListeners() = runTest {
        val packetCallback = mock<DisplayDataListener>(mode = MockMode.autoUnit)
        _fakeClient.registerDisplayDataListener(scope = this@runTest, listener = packetCallback)
        advanceUntilIdle()

        val infDisplayData = createInfDisplayDataPacket()
        _espData.emit(value = infDisplayData) // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterDisplayDataListeners() // Unregister

        val infDisplayDataAlt = createInfDisplayDataPacket(
            payload = defaultDisplayPayload
                .clone()
                .also { it.fill(0xFF.toByte()) }
        )
        _espData.emit(value = infDisplayDataAlt)  // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) {
            packetCallback.onDisplayData(
                display = custom(infDisplayData.displayData()) { l, r ->
                    l.bytes.contentEquals(r.bytes)
                }
            )
        }
        verify(mode = exactly(n = 0)) { packetCallback.onDisplayData(display = any()) }
    }

    @Test
    fun registerDisplayDataListener_isClearedBy_unregisterSpecificListener() = runTest {
        val packetCallback = mock<DisplayDataListener>(mode = MockMode.autoUnit)
        _fakeClient.registerDisplayDataListener(scope = this@runTest, listener = packetCallback)
        advanceUntilIdle()

        val infDisplayData = createInfDisplayDataPacket()
        _espData.emit(value = infDisplayData) // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterDisplayDataListener(packetCallback) // Unregister

        val infDisplayDataAlt = createInfDisplayDataPacket(
            payload = defaultDisplayPayload
                .clone()
                .also { it.fill(0xFF.toByte()) }
        )
        _espData.emit(value = infDisplayDataAlt) // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) {
            packetCallback.onDisplayData(
                display = custom(infDisplayData.displayData()) { l, r ->
                    l.bytes.contentEquals(r.bytes)
                }
            )
        }
        verify(mode = exactly(n = 0)) { packetCallback.onDisplayData(display = any()) }
    }

    @Test
    fun registerAlertTableListener_isClearedBy_unregisterPacketListeners() = runTest {
        val alertTableCallback = mock<AlertTableListener>(mode = MockMode.autoUnit)
        _fakeClient.registerAlertTableListener(scope = this@runTest, listener = alertTableCallback)
        advanceUntilIdle()

        val alarmTable = create3AlertTable()
        alarmTable.forEach { alert -> _espData.emit(value = alert) } // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterAlertTableListeners() // Unregister
        alarmTable.forEach { alert -> _espData.emit(value = alert) }  // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { alertTableCallback.onAlertTable(table = any()) }
        verify(mode = exactly(n = 0)) { alertTableCallback.onAlertTable(table = any()) }
    }


    @Test
    fun registerAlertTableListener_isClearedBy_unregisterSpecificListener() = runTest {
        val alertTableCallback = mock<AlertTableListener>(mode = MockMode.autoUnit)
        _fakeClient.registerAlertTableListener(scope = this@runTest, listener = alertTableCallback)
        advanceUntilIdle()

        val alarmTable = create3AlertTable()
        alarmTable.forEach { alert -> _espData.emit(value = alert) } // Event 1
        advanceUntilIdle()
        _fakeClient.unregisterAlertTableListeners() // Unregister
        alarmTable.forEach { alert -> _espData.emit(value = alert) }  // Event 2 (Should be ignored)
        advanceUntilIdle()

        verify(mode = exactly(n = 1)) { alertTableCallback.onAlertTable(table = any()) }
        verify(mode = exactly(n = 0)) { alertTableCallback.onAlertTable(table = any()) }
    }
}
