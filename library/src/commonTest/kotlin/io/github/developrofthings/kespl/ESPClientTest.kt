package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.connection.FakeConnection
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.packet.data.SerialNumber
import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.user.TechDisplayUserSettings
import io.github.developrofthings.kespl.packet.data.user.UserSettings
import io.github.developrofthings.kespl.packet.data.user.V18UserSettings
import io.github.developrofthings.kespl.packet.data.user.V19UserSettings
import io.github.developrofthings.kespl.preferences.FakeESPPreferencesManager
import io.github.developrofthings.kespl.preferences.IESPPreferencesManager
import io.github.developrofthings.kespl.utilities.V1VersionInfo.UserSettingsInfo
import io.github.developrofthings.kespl.utilities.createInfDisplayDataPacket
import io.github.developrofthings.kespl.utilities.createPacketArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

class ESPClientTest {

    private val _flowController: MutableESPFlowController = ImplESPFlowController()
    private val _preferencesManager: IESPPreferencesManager = FakeESPPreferencesManager()

    private fun createClient(
        testScope: CoroutineScope,
        espContext: ESPContext = platformContext(
            bluetoothSupported = true,
            bleSupported = true,
        ),
        flowController: ESPFlowController = _flowController,
        preferencesManager: IESPPreferencesManager = _preferencesManager,
        connection: IConnection = FakeConnection(
            flowController = flowController as MutableESPFlowController,
            testScope = testScope,
        ),
    ): ESPClient = ESPClient(
        initialConnection = connection,
        espContext = espContext,
        flowController = flowController,
        preferencesManager = preferencesManager,
        scope = testScope,
    )

    private suspend fun emitESPDataPacketWhenReady(
        espDatum: ByteArray,
        subscriptionCountThreshold: Int = 0,
        awaitEmpty: Boolean = false
    ) {
        // Wait until shared flows subscription count exceeds `subscriptionCountThreshold` before
        // sending the ESP packet byte data this represents the ESPClient subscribing and listening
        // to the flow awaiting for it's target response
        _flowController.espData.subscriptionCount.first { it > subscriptionCountThreshold }
        _flowController.espData.emit(value = espDatum)
        if (awaitEmpty) _flowController.espData.subscriptionCount.first { it == 0 }
    }

    @Test
    fun given_a_new_ESPClient_then_isConnected_equals_false() = runTest {
        val client = createClient(testScope = this.backgroundScope)
        assertFalse(actual = client.isConnected)
    }

    //region Version
    @Test
    fun when_ESPClient_isConnected_then_requestDeviceVersion_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }
            val expectedVersionNumber = "V4.1036"
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = expectedVersionNumber
                )
            )

            val successResponse =
                assertIs<ESPResponse.Success<Version>>(value = versionAsyncResult.await())
            assertEquals(expected = expectedVersionNumber, actual = successResponse.data)
        }

    @Test
    fun when_ESPClient_not_connected_then_requestDeviceVersion_returns_NotConnectedFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Disconnected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.NotConnected, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_v1_serial_number_response_is_received_then_requestDeviceVersion_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = "01234567789"
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_a_concealed_display_version_response_is_received_then_requestDeviceVersion_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val expectedVersion = "C2.1300"
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespVersion,
                    payload = expectedVersion
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_no_version_response_is_received_then_requestDeviceVersion_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }
            // We intentionally don't emit the version response as to test out the operation timeout

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_client_has_not_detected_v1_type_then_requestV1Version_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async { client.requestV1Version(timeout = 100.milliseconds) }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_client_has_detected_v1_isLegacy_then_requestV1Version_returns_NotSupportedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Legacy)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async { client.requestV1Version(timeout = 100.milliseconds) }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.NotSupported, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_requestDeviceVersion_returns_LegacyModeFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a legacy V1 error
            connection.setWriteRequestFailure(reason = ESPFailure.LegacyMode)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.LegacyMode, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_has_not_detected_v1_type_then_requestDeviceVersion_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.V1NotDetermined)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_then_requestDeviceVersion_returns_NotTimeSlicingFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not time slicing
            // error
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val versionAsyncResult: Deferred<ESPResponse<Version, ESPFailure>> =
                async {
                    client.requestDeviceVersion(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = versionAsyncResult.await())
            assertEquals(expected = ESPFailure.NotTimeSlicing, actual = failureResponse.data)
        }
    // endregion

    //region Serial Number
    @Test
    fun when_ESPClient_isConnected_then_requestDeviceSerialNumber_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }
            val expectedSerialNumber = "0123456789"
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = expectedSerialNumber
                )
            )

            val successResponse =
                assertIs<ESPResponse.Success<SerialNumber>>(value = snAsyncResult.await())
            assertEquals(expected = expectedSerialNumber, actual = successResponse.data)
        }

    @Test
    fun when_ESPClient_not_connected_then_requestDeviceSerialNumber_returns_NotConnectedFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Disconnected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.NotConnected, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_v1_serial_number_response_is_received_then_requestDeviceSerialNumber_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_concealed_display_version_response_is_received_then_requestDeviceSerialNumber_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val expectedVersion = "1122334455"
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.SAVVY,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = expectedVersion
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_no_version_response_is_received_then_requestDeviceSerialNumber_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }
            // We intentionally don't emit the version response as to test out the operation timeout

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_client_has_not_detected_v1_type_then_requestV1SerialNumber_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async { client.requestV1SerialNumber(timeout = 100.milliseconds) }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_client_has_detected_v1_isLegacy_then_requestV1SerialNumber_returns_NotSupportedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Legacy)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async { client.requestV1SerialNumber(timeout = 100.milliseconds) }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.NotSupported, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_requestDeviceSerialNumber_returns_LegacyModeFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a legacy V1 error
            connection.setWriteRequestFailure(reason = ESPFailure.LegacyMode)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.LegacyMode, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_has_not_detected_v1_type_then_requestDeviceSerialNumber_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.V1NotDetermined)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_then_requestDeviceSerialNumber_returns_NotTimeSlicingFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not time slicing
            // error
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)
            // Wrap in sn request in an async coroutine so that we can start "collecting" and emit the
            // serial number response concurrently
            val snAsyncResult: Deferred<ESPResponse<SerialNumber, ESPFailure>> =
                async {
                    client.requestDeviceSerialNumber(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = snAsyncResult.await())
            assertEquals(expected = ESPFailure.NotTimeSlicing, actual = failureResponse.data)
        }
    //endregion

    //region User Settings
    @Test
    fun when_ESPClient_isConnected_to_Gen2_then_requestUserSettings_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Version response, a version is needed internally to determine the type of
                // UserSettings to return ie `V19UserSettings`/`V18UserSettings`
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
                awaitEmpty = true,
            )

            emitESPDataPacketWhenReady(
                // Actual  User bytes response
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = UserSettingsInfo.V4_1000_UserBytes
                ),
                awaitEmpty = false,
            )

            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = userSettingsAsyncResult.await())
            val userSettings = assertIs<V19UserSettings>(value = successResponse.data)
            assertContentEquals(
                expected = UserSettingsInfo.V4_1000_UserBytes,
                actual = userSettings.userBytes
            )
        }

    @Test
    fun when_ESPClient_isConnected_to_Gen1_then_requestUserSettings_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Version response, a version is needed internally to determine the type of
                // UserSettings to return ie `V19UserSettings`/`V18UserSettings`
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V3.8952"
                ),
                awaitEmpty = true,
            )

            emitESPDataPacketWhenReady(
                // Actual  User bytes response
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = UserSettingsInfo.V3_8920_UserBytes
                ),
                awaitEmpty = false,
            )

            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = userSettingsAsyncResult.await())
            val userSettings = assertIs<V18UserSettings>(value = successResponse.data)
            assertContentEquals(
                expected = UserSettingsInfo.V3_8920_UserBytes,
                actual = userSettings.userBytes
            )
        }

    @Test
    fun when_ESPClient_isConnected_to_TechDisplay_then_requestUserSettings_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.RemoteDisplay,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Version response, a version is needed internally to determine the type of
                // UserSettings to return ie TechDisplayUserSettings
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespVersion,
                    payload = "T1.0001"
                ),
                awaitEmpty = true,
            )

            emitESPDataPacketWhenReady(
                // Actual  User bytes response
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = UserSettingsInfo.T1_0000_UserBytes,
                ),
                awaitEmpty = false,
            )

            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = userSettingsAsyncResult.await())
            val userSettings = assertIs<TechDisplayUserSettings>(value = successResponse.data)
            assertContentEquals(
                expected = UserSettingsInfo.T1_0000_UserBytes,
                actual = userSettings.userBytes
            )
        }

    @Test
    fun when_ESPClient_not_connected_then_requestUserSettings_returns_NotConnectedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Disconnected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.NotConnected, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_v1_serial_number_response_response_is_received_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }


            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = "0123456789"
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_a_concealed_display_version_response_is_received_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            // Version response, a version is needed internally to determine the type of
            // UserSettings to return ie `V19UserSettings`/`V18UserSettings`.. intentionally emit a
            // none matching version response
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespVersion,
                    payload = "C2.1300"
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_no_version_response_is_received_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_no_userBytes_is_received_then_requestUserSettings_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Version response, a version is needed internally to determine the type of
                // UserSettings to return ie `V19UserSettings`/`V18UserSettings`
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_client_has_not_detected_v1_type_then_requestV1UserSettings_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestV1UserSettings(
                        forceVersionRequest = true,
                        timeout = 100.milliseconds
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_client_has_detected_v1_isLegacy_then_requestV1UserSettings_returns_NotSupportedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Legacy)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in version request in an async coroutine so that we can start "collecting" and emit the
            // version response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestV1UserSettings(
                        forceVersionRequest = true,
                        timeout = 100.milliseconds
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.NotSupported, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a legacy V1 error
            connection.setWriteRequestFailure(reason = ESPFailure.LegacyMode)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_has_not_detected_v1_type_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.V1NotDetermined)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)
            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_after_version_response_then_requestUserSettings_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )

            // Wrap in user settings request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.requestUserSettings(
                        destination = ESPDevice.ValentineOne.Checksum,
                        forceVersionRequest = true,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Version response, a version is needed internally to determine the type of
                // UserSettings to return ie `V19UserSettings`/`V18UserSettings`
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
            )
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)


            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.NotTimeSlicing, actual = failureResponse.data)
        }
    //endregion

    //region User Bytes
    //region Read User Bytes
    @Test
    fun when_ESPClient_isConnected_to_Gen2_then_requestUserBytes_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this.backgroundScope)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                // Actual  User bytes response
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = UserSettingsInfo.V4_1000_UserBytes
                ),
            )

            val successResponse =
                assertIs<ESPResponse.Success<ByteArray>>(value = userBytesAsyncResult.await())
            val userBytes = assertIs<ByteArray>(successResponse.data)
            assertContentEquals(
                expected = UserSettingsInfo.V4_1000_UserBytes,
                actual = userBytes,
            )
        }

    @Test
    fun when_ESPClient_not_connected_then_requestUserBytes_returns_NotConnectedFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Disconnected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }
            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.NotConnected, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_v1_serial_number_response_is_received_then_requestUserBytes_returns_TimedOutFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = "01234567789"
                )
            )

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_no_version_response_is_received_then_requestUserByte_returns_TimedOutFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val client = createClient(testScope = this@runTest.backgroundScope)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            // We intentionally don't emit the version response as to test out the operation timeout

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_requestUserBytes_returns_LegacyModeFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            // We intentionally don't emit the version response as to test out the operation timeout

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_has_not_detected_v1_type_then_requestUserBytes_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.V1NotDetermined)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_then_requestUserBytes_returns_NotTimeSlicingFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)
            // Wrap in user bytes request in an async coroutine so that we can start "collecting"
            // and emit the version & user settings response concurrently
            val userBytesAsyncResult: Deferred<ESPResponse<ByteArray, ESPFailure>> =
                async {
                    client.requestUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        timeout = 100.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userBytesAsyncResult.await())
            assertEquals(expected = ESPFailure.NotTimeSlicing, actual = failureResponse.data)
        }
    //endregion

    //region Write User Bytes
    @Test
    fun when_ESPClient_isConnected_to_Gen2_then_writeUserBytes_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)

            val expectedUserBytes = byteArrayOf(
                (0xFF).toByte(),
                (0xFE).toByte(),
                (0xFD).toByte(),
                (0xFC).toByte(),
                (0xFB).toByte(),
                (0xFA).toByte(),
            )

            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val writeUserBytesResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = expectedUserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            //  After writing bytes, ESPClient.writeUserBytes(...) request user settings which
            //  request target device versions to return the appropriately typed UserSettings
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
                awaitEmpty = true,
            )
            // Actual user bytes response
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = expectedUserBytes,
                ),
            )

            val deferredResult = writeUserBytesResult.await()
            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = deferredResult)
            val userSettings = assertIs<V19UserSettings>(successResponse.data)
            assertContentEquals(
                expected = expectedUserBytes,
                actual = userSettings.userBytes,
            )
        }

    @Test
    fun when_ESPClient_isConnected_to_Gen1_then_writeUserBytes_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)

            val expectedUserBytes = byteArrayOf(
                (0xFF).toByte(),
                (0xFE).toByte(),
                (0xFD).toByte(),
                (0xFC).toByte(),
                (0xFB).toByte(),
                (0xFA).toByte(),
            )

            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val writeUserBytesResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = expectedUserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            //  After writing bytes, ESPClient.writeUserBytes(...) request user settings which
            //  request target device versions to return the appropriately typed UserSettings
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V3.8952"
                ),
                awaitEmpty = true,
            )
            // Actual user bytes response
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = expectedUserBytes,
                ),
            )

            val deferredResult = writeUserBytesResult.await()
            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = deferredResult)
            val userSettings = assertIs<V18UserSettings>(successResponse.data)
            assertContentEquals(
                expected = expectedUserBytes,
                actual = userSettings.userBytes,
            )
        }

    @Test
    fun when_ESPClient_isConnected_to_TechDisplay_then_writeUserBytes_returns_ESPResponse_Success() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)

            val expectedUserBytes = byteArrayOf(
                (0xFF).toByte(),
                (0xFE).toByte(),
                (0xFD).toByte(),
                (0xFC).toByte(),
                (0xFB).toByte(),
                (0xFA).toByte(),
            )

            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val writeUserBytesResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.RemoteDisplay,
                        userBytes = expectedUserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            //  After writing bytes, ESPClient.writeUserBytes(...) request user settings which
            //  request target device versions to return the appropriately typed UserSettings
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespVersion,
                    payload = "T1.0001"
                ),
                awaitEmpty = true,
            )
            // Actual user bytes response
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.RemoteDisplay,
                    packetId = ESPPacketId.RespUserBytes,
                    payload = expectedUserBytes,
                ),
            )

            val deferredResult = writeUserBytesResult.await()
            val successResponse =
                assertIs<ESPResponse.Success<UserSettings>>(value = deferredResult)
            val userSettings = assertIs<TechDisplayUserSettings>(successResponse.data)
            assertContentEquals(
                expected = expectedUserBytes,
                actual = userSettings.userBytes,
            )
        }

    @Test
    fun when_ESPClient_not_connected_then_writeUserBytes_returns_NotConnectedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Disconnected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val writeUserBytesResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = writeUserBytesResult.await())
            assertEquals(expected = ESPFailure.NotConnected, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_does_not_receive_expected_count_of_infDisplayData_then_writeUserBytes_returns_TimedOutFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }
            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 4
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.TimedOut, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_does_not_receive_target_version_response_then_writeUserBytes_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }
            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }
            /* We intentionally don't emit version or user bytes response*/
            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_and_does_not_receive_target_user_bytes_response_then_writeUserBytes_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            //  After writing bytes, ESPClient.writeUserBytes(...) request user settings which
            //  request target device versions to return the appropriately typed UserSettings
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
                awaitEmpty = true,
            )

            /* We intentionally don't emit version or user bytes response*/
            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_writeUserBytes_returns_ESPOperationFailedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            _flowController.valentineOneType.emit(value = ESPDevice.ValentineOne.Checksum)
            val client = createClient(testScope = this.backgroundScope)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            // ESPClient.writeUserBytes(...) expect 5 concurrent InfDisplayData packets to
            // consider actual byte write complete
            val expectedInfDisplayCount = 5
            repeat(times = expectedInfDisplayCount) {
                emitESPDataPacketWhenReady(
                    espDatum = createInfDisplayDataPacket(useChecksum = true),
                    awaitEmpty = it == (expectedInfDisplayCount - 1)
                )
            }

            //  After writing bytes, ESPClient.writeUserBytes(...) request user settings which
            //  request target device versions to return the appropriately typed UserSettings
            emitESPDataPacketWhenReady(
                espDatum = createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespVersion,
                    payload = "V4.1036"
                ),
                awaitEmpty = true,
            )

            /* We intentionally don't emit version or user bytes response*/
            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.ESPOperationFailed, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_legacy_then_writeUserBytes_returns_LegacyModeFailure() =
        runTest {
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a legacy V1 error
            connection.setWriteRequestFailure(reason = ESPFailure.LegacyMode)
            /*
                client.writeUserBytes() is made up of multiple operations that
                each need to be manually mocked:
                    1. write user bytes to device
                        1a. then wait for 5 infDisplayData for the bytes to take affect
                    2. request user settings back from device
                        2a. request device version to return the appropriate UserSettings
                        2b. request user bytes from device

                to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
                so that we can start "collecting" before we start emitting the necessary esp data.
             */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.LegacyMode, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_has_not_detected_v1_type_then_writeUserBytes_returns_V1NotDeterminedFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.V1NotDetermined)
            /*
            client.writeUserBytes() is made up of multiple operations that
            each need to be manually mocked:
            1. write user bytes to device
            1a. then wait for 5 infDisplayData for the bytes to take affect
            2. request user settings back from device
                    2a. request device version to return the appropriate UserSettings
            2b. request user bytes from device

                    to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
            so that we can start "collecting" before we start emitting the necessary esp data.
            */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.V1NotDetermined, actual = failureResponse.data)
        }

    @Test
    fun when_ESPClient_isConnected_but_connection_detected_v1_is_not_timeslicing_then_writeUserBytes_returns_NotTimeSlicingFailure() =
        runTest {
            // Force the client to "connected"
            _flowController.connectionStatus.emit(value = ESPConnectionStatus.Connected)
            val connection = FakeConnection(
                flowController = _flowController,
                testScope = this@runTest.backgroundScope,
            )
            val client = createClient(
                testScope = this@runTest.backgroundScope,
                connection = connection,
            )
            // Override the result of `IConnection.writeRequest()` to simulate a V1 not not
            // determined error
            connection.setWriteRequestFailure(reason = ESPFailure.NotTimeSlicing)
            /*
            client.writeUserBytes() is made up of multiple operations that
            each need to be manually mocked:
            1. write user bytes to device
            1a. then wait for 5 infDisplayData for the bytes to take affect
            2. request user settings back from device
                    2a. request device version to return the appropriate UserSettings
            2b. request user bytes from device

                    to orchestrate these operations we must wrap writeUserBytes() in a async coroutine
            so that we can start "collecting" before we start emitting the necessary esp data.
            */
            val userSettingsAsyncResult: Deferred<ESPResponse<UserSettings, ESPFailure>> =
                async {
                    client.writeUserBytes(
                        destination = ESPDevice.ValentineOne.Checksum,
                        userBytes = UserSettingsInfo.V4_1000_UserBytes,
                        verifyBytes = true,
                        timeout = 200.milliseconds,
                    )
                }

            val failureResponse =
                assertIs<ESPResponse.Failure<ESPFailure>>(value = userSettingsAsyncResult.await())
            assertEquals(expected = ESPFailure.NotTimeSlicing, actual = failureResponse.data)
        }
    //endregion
    //endregion
}