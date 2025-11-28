package io.github.developrofthings.kespl.utilities.extensions.flow

import app.cash.turbine.test
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPFailure
import io.github.developrofthings.kespl.ESPPacketId
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.sweepDefinition
import io.github.developrofthings.kespl.packet.data.version
import io.github.developrofthings.kespl.packet.isDisplayOn
import io.github.developrofthings.kespl.packet.packetId
import io.github.developrofthings.kespl.utilities.createDataErrorPacket
import io.github.developrofthings.kespl.utilities.createInfDisplayDataPacket
import io.github.developrofthings.kespl.utilities.createNotProcessedPacket
import io.github.developrofthings.kespl.utilities.createPacketArray
import io.github.developrofthings.kespl.utilities.createRespSweepDefinitionPackets
import io.github.developrofthings.kespl.utilities.createUnsupportedPacket
import io.github.developrofthings.kespl.utilities.createV1BusyPacket
import io.github.developrofthings.kespl.utilities.defaultDisplayPayload
import io.github.developrofthings.kespl.utilities.defaultSweeps
import io.github.developrofthings.kespl.utilities.displayOffDisplayPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ESPByteArrayFlowExtTest {

    @Test
    fun `When flow contains response from target device destined for V1connection then Flow_filterForResponse() emits`() =
        runTest {
            // Test that a packet matching the responsePacketId and target origin is emitted.
            val expectedVersion = "V4.1018"
            val requestPacketId = ESPPacketId.ReqVersion
            val responsePacketId = ESPPacketId.RespVersion
            val sourceFlow = flowOf(
                createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = responsePacketId,
                    payload = expectedVersion
                )
            )


            sourceFlow.filterForResponse(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = requestPacketId,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = responsePacketId,
            ).test {
                val packet = awaitItem()
                assertEquals(responsePacketId, packet.packetId)
                assertEquals(expected = expectedVersion, actual = packet.version())
                awaitComplete()
            }
        }

    @Test
    fun `When flow contains response from target device destined for device then Flow_filterForResponse() emits`() =
        runTest {
            // Test that a packet matching the responsePacketId and target origin is emitted.
            val expectedVersion = "V4.1018"
            val requestPacketId = ESPPacketId.ReqVersion
            val responsePacketId = ESPPacketId.RespVersion
            val sourceFlow = flowOf(
                createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.ThirdParty1,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = responsePacketId,
                    payload = expectedVersion
                )
            )

            sourceFlow.filterForResponse(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = requestPacketId,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = responsePacketId,
            ).test {
                awaitComplete()
                ensureAllEventsConsumed()
            }
        }


    @Test
    fun `When flow contains general broadcast response from target device then Flow_filterForResponse() emits`() =
        runTest {
            // Test that a packet matching the responsePacketId and target origin is emitted.
            val expectedVersion = "V4.1018"
            val requestPacketId = ESPPacketId.ReqVersion
            val responsePacketId = ESPPacketId.RespVersion
            val sourceFlow = flowOf(
                createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.GeneralBroadcast,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = responsePacketId,
                    payload = expectedVersion
                )
            )

            sourceFlow.filterForResponse(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = requestPacketId,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = responsePacketId,
            ).test {
                val packet = awaitItem()
                assertEquals(responsePacketId, packet.packetId)
                assertEquals(expected = expectedVersion, actual = packet.version())
                awaitComplete()
            }
        }


    @Test
    fun `When flow contains non-target response from target device destined for V1connection then Flow_filterForResponse() emits does not emit`() =
        runTest {
            // Test that a packet matching the responsePacketId and target origin is emitted.
            val expectedSerialNumber = "0123456789"
            val sourceFlow = flowOf(
                createPacketArray(
                    useChecksum = true,
                    destination = ESPDevice.V1connection,
                    origin = ESPDevice.ValentineOne.Checksum,
                    packetId = ESPPacketId.RespSerialNumber,
                    payload = expectedSerialNumber
                )
            )

            sourceFlow.filterForResponse(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqVersion,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespVersion,
            ).test {
                awaitComplete()
                ensureAllEventsConsumed()
            }
        }

    @Test
    fun `When ByteArray contains destination identifier == ESPDevice_V1connection then ByteArray_isPacketFor() == true`() {
        assertTrue(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.V1connection,
                origin = ESPDevice.ValentineOne.Checksum,
                packetId = ESPPacketId.RespSerialNumber,
                payload = "0123456789"
            ).isPacketForMe()
        )
    }

    @Test
    fun `When ByteArray contains destination identifier == ESPDevice_GeneralBroadcast then ByteArray_isPacketFor() == true`() {
        assertTrue(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.GeneralBroadcast,
                origin = ESPDevice.ValentineOne.Checksum,
                packetId = ESPPacketId.RespSerialNumber,
                payload = "0123456789"
            ).isPacketForMe()
        )
    }

    @Test
    fun `When ByteArray contains destination identifier == ESPDevice_SAVVY then ByteArray_isPacketFor() == false`() {
        assertFalse(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.SAVVY,
                origin = ESPDevice.ValentineOne.Checksum,
                packetId = ESPPacketId.RespSerialNumber,
                payload = "0123456789"
            ).isPacketForMe()
        )
    }

    @Test
    fun `When ByteArray contains packet identifier == ESPPacketId_RespSerialNumber and origin identifier == ESPDevice_ValentineOne_Checksum that matches then ByteArray_isTargetResponse() == true`() {
        assertTrue(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.V1connection,
                origin = ESPDevice.ValentineOne.Checksum,
                packetId = ESPPacketId.RespSerialNumber,
                payload = "0123456789"
            ).isTargetResponse(
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespSerialNumber,
            )
        )
    }

    @Test
    fun `When ByteArray contains packet identifier != ESPPacketId_RespSerialNumber and origin identifier == ESPDevice_ValentineOne_Checksum that matches then ByteArray_isTargetResponse() == false`() {
        assertFalse(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.V1connection,
                origin = ESPDevice.ValentineOne.Checksum,
                packetId = ESPPacketId.RespVersion,
                payload = "V4.1018"
            ).isTargetResponse(
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespSerialNumber,
            )
        )
    }

    @Test
    fun `When ByteArray contains packet identifier == ESPPacketId_RespSerialNumber and origin identifier != ESPDevice_ValentineOne_Checksum that matches then ByteArray_isTargetResponse() == false`() {
        assertFalse(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.V1connection,
                origin = ESPDevice.SAVVY,
                packetId = ESPPacketId.RespSerialNumber,
                payload = "0123456789"
            ).isTargetResponse(
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespSerialNumber,
            )
        )
    }

    @Test
    fun `When ByteArray contains packet identifier != ESPDevice_ValentineOne_Checksum and origin identifier != ESPPacketId_RespSerialNumber that matches then ByteArray_isTargetResponse() == false`() {
        assertFalse(
            actual = createPacketArray(
                useChecksum = true,
                destination = ESPDevice.V1connection,
                origin = ESPDevice.SAVVY,
                packetId = ESPPacketId.RespVersion,
                payload = "V4.1018"
            ).isTargetResponse(
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespSerialNumber,
            )
        )
    }

    @Test
    fun `When a V1Busy ByteArray containing ESPPacketId_RespVersion packet identifier then ByteArray_checkForBusyOrUnProcessedRequest(ESPPacketId_RespVersion) == true`() {
        assertTrue(
            actual = createV1BusyPacket(
                useChecksum = true,
                ESPPacketId.RespVersion.id
            ).checkForBusyOrUnProcessedRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.RespVersion,
            )
        )
    }

    @Test
    fun `When a V1Busy ByteArray containing ESPPacketId_RespSerialNumber packet identifier then ByteArray_checkForBusyOrUnProcessedRequest(ESPPacketId_RespVersion) == false`() {
        assertFalse(
            actual = createV1BusyPacket(
                useChecksum = true,
                ESPPacketId.RespSerialNumber.id
            ).checkForBusyOrUnProcessedRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.RespVersion,
            )
        )
    }

    @Test
    fun `When a NotProcessed ByteArray containing ESPPacketId_RespVersion packet identifier then ByteArray_checkForBusyOrUnProcessedRequest(ESPPacketId_RespVersion) == true`() {
        assertTrue(
            actual = createNotProcessedPacket(
                useChecksum = true,
                ESPPacketId.RespVersion.id
            ).checkForBusyOrUnProcessedRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.RespVersion,
            )
        )
    }

    @Test
    fun `When a NotProcessed ByteArray containing ESPPacketId_RespSerialNumber packet identifier then ByteArray_checkForBusyOrUnProcessedRequest(ESPPacketId_RespVersion) == false`() {
        assertFalse(
            actual = createNotProcessedPacket(
                useChecksum = true,
                ESPPacketId.RespSerialNumber.id
            ).checkForBusyOrUnProcessedRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.RespVersion,
            )
        )
    }

    @Test
    fun `When flow contains expected packet sequence, then Flow_transformPacketWhileNull emits expected value`() =
        runTest {
            val expected = 3
            var counter = 0
            packetByteList(count = 5)
                .asFlow()
                .transformPacketWhileNull(
                    transformPacket = {
                        counter++
                        if (counter == expected) counter else null
                    },
                    maxBusyPackets = 0
                ).test {
                    val actual = assertIs<ESPResponse.Success<Int>>(value = awaitItem()).data
                    assertEquals(
                        expected = expected,
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains an ESPPacketId_InfV1Busy packet, then Flow_transformPacketWhileNull emits ESPFailure_V1Busy`() =
        runTest {
            var counter = 0
            packetByteList(count = 1) {
                createV1BusyPacket(
                    useChecksum = true,
                    ESPPacketId.ReqVersion,
                )
            }.asFlow()
                .transformPacketWhileNull(
                    transformPacket = {
                        counter++
                        if (counter == 3) counter else null
                    },
                    maxBusyPackets = 0
                ).test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = awaitItem()).data
                    assertEquals(
                        expected = ESPFailure.V1Busy,
                        actual = actual
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains an ESPPacketId_RespDataError packet, then Flow_transformPacketWhileNull emits ESPFailure_DataError`() =
        runTest {
            var counter = 0
            packetByteList(count = 1) {
                createDataErrorPacket(
                    useChecksum = true,
                    ESPPacketId.ReqVersion,
                )
            }.asFlow()
                .transformPacketWhileNull(
                    transformPacket = {
                        counter++
                        if (counter == 3) counter else null
                    },
                    maxBusyPackets = 0
                ).test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = awaitItem()).data
                    assertEquals(
                        expected = ESPFailure.DataError,
                        actual = actual
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains an ESPPacketId_RespRequestNotProcessed packet, then Flow_transformPacketWhileNull emits ESPFailure_NotProcessed`() =
        runTest {
            var counter = 0
            packetByteList(count = 1) {
                createNotProcessedPacket(
                    useChecksum = true,
                    ESPPacketId.ReqVersion,
                )
            }.asFlow()
                .transformPacketWhileNull(
                    transformPacket = {
                        counter++
                        if (counter == 3) counter else null
                    },
                    maxBusyPackets = 0
                ).test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = awaitItem()).data
                    assertEquals(
                        expected = ESPFailure.NotProcessed,
                        actual = actual
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains an ESPPacketId_RespUnsupportedPacket packet, then Flow_transformPacketWhileNull emits ESPFailure_NotSupported`() =
        runTest {
            var counter = 0
            packetByteList(count = 1) {
                createUnsupportedPacket(
                    useChecksum = true,
                    packetId = ESPPacketId.ReqVersion,
                )
            }.asFlow()
                .transformPacketWhileNull(
                    transformPacket = {
                        counter++
                        if (counter == 3) counter else null
                    },
                    maxBusyPackets = 0
                ).test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = awaitItem()).data
                    assertEquals(
                        expected = ESPFailure.NotSupported,
                        actual = actual
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence, then Flow_transformToResponseWhile emits matching List of Sweep Definitions`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            )
                .asFlow()
                .transformToResponseWhile(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = 0,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }.test {
                    val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(
                        value = awaitItem(),
                    ).data
                    assertEquals(
                        expected = defaultSweeps,
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with less than max ESPPacketId_InfV1Busy packets, then Flow_transformToResponseWhile emits matching List of Sweep Definitions`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()

            createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject a single InfV1Busy into the flow/sequence of packets
                add(
                    index = 1,
                    element = createV1BusyPacket(
                        useChecksum = true,
                        ESPPacketId.ReqVersion,
                    )
                )
            }
                .asFlow()
                .transformToResponseWhile(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = 1,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }.test {
                    val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(
                        value = awaitItem(),
                    ).data
                    assertEquals(
                        expected = defaultSweeps,
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with one more than max ESPPacketId_InfV1Busy packets, then Flow_transformToResponseWhile returns ESPFailure_V1Busy`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val maxBusyPackets = 1
            createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject 1 more than the maximum allowed busy packets into the
                // flow/sequence of packets
                repeat(maxBusyPackets + 1) {
                    add(
                        index = 1 + it,
                        element = createV1BusyPacket(
                            useChecksum = true,
                            ESPPacketId.ReqAllSweepDefinitions,
                        )
                    )
                }
            }
                .asFlow()
                .transformToResponseWhile(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = maxBusyPackets,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }.test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(
                        value = awaitItem(),
                    ).data
                    assertEquals(
                        expected = ESPFailure.V1Busy,
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with unrelated max ESPPacketId_InfV1Busy packets, then Flow_transformToResponseWhile returns ESPFailure_V1Busy`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val maxBusyPackets = 1
            createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject 1 more than the maximum allowed busy packets into the
                // flow/sequence of packets
                repeat(maxBusyPackets + 1) {
                    add(
                        index = 1 + it,
                        element = createV1BusyPacket(
                            useChecksum = true,
                            ESPPacketId.ReqSweepSections,
                        )
                    )
                }
            }
                .asFlow()
                .transformToResponseWhile(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = maxBusyPackets,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }.test {
                    val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(
                        value = awaitItem(),
                    ).data
                    assertEquals(
                        expected = defaultSweeps,
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow throws an exception, then Flow_transformToResponseWhile returns ESPFailure_Unknown`() =
        runTest {
            val exception = Exception("Random Exception")
            flow<ByteArray> {
                delay(20.milliseconds)
                throw exception
            }
                .transformToResponseWhile(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = 0,
                ) { null }.test {
                    val actual = assertIs<ESPResponse.Failure<ESPFailure>>(
                        value = awaitItem(),
                    ).data
                    assertEquals(
                        expected = ESPFailure.Unknown(e = exception),
                        actual = actual,
                    )
                    awaitComplete()
                }
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence, then Flow_waitForESPResponseForESPRequest emits matching List of Sweep Definitions`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val response = createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            )
                .asFlow()
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    timeout = 1.seconds,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }
            val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(response).data
            assertEquals(
                expected = defaultSweeps,
                actual = actual,
            )
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with less than max ESPPacketId_InfV1Busy packets, then Flow_waitForESPResponseForESPRequest emits matching List of Sweep Definitions`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val maxBusyPackets = 1
            val response = createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject a single InfV1Busy into the flow/sequence of packets
                add(
                    index = 1,
                    element = createV1BusyPacket(
                        useChecksum = true,
                        ESPPacketId.ReqVersion,
                    )
                )
            }
                .asFlow()
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = maxBusyPackets,
                    timeout = 1.seconds,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }
            val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(value = response).data
            assertEquals(
                expected = defaultSweeps,
                actual = actual,
            )
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with one more than max ESPPacketId_InfV1Busy packets, then Flow_waitForESPResponseForESPRequest returns ESPFailure_V1Busy`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val maxBusyPackets = 1
            val response = createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject 1 more than the maximum allowed busy packets into the
                // flow/sequence of packets
                repeat(maxBusyPackets + 1) {
                    add(
                        index = 1 + it,
                        element = createV1BusyPacket(
                            useChecksum = true,
                            ESPPacketId.ReqAllSweepDefinitions,
                        )
                    )
                }
            }
                .asFlow()
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = maxBusyPackets,
                    timeout = 1.seconds,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }


            val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = response).data
            assertEquals(
                expected = ESPFailure.V1Busy,
                actual = actual,
            )
        }

    @Test
    fun `When flow contains Sweep Definition packet sequence intermingled with unrelated max ESPPacketId_InfV1Busy packets, then Flow_waitForESPResponseForESPRequest returns ESPFailure_V1Busy`() =
        runTest {
            val maxSweeps = defaultSweeps.size
            val sweeps = arrayListOf<SweepDefinition>()
            val maxBusyPackets = 1
            val response = createRespSweepDefinitionPackets(
                useChecksum = true,
                default = false,
                sweepsDefinitions = defaultSweeps,
            ).toMutableList().apply {
                // We want to inject 1 more than the maximum allowed busy packets into the
                // flow/sequence of packets
                repeat(maxBusyPackets + 1) {
                    add(
                        index = 1 + it,
                        element = createV1BusyPacket(
                            useChecksum = true,
                            ESPPacketId.ReqSweepSections,
                        )
                    )
                }
            }
                .asFlow()
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    maxBusyPackets = maxBusyPackets,
                    timeout = 1.seconds,
                ) {
                    sweeps.add(it.sweepDefinition())
                    if (sweeps.size == maxSweeps) sweeps else null
                }
            val actual = assertIs<ESPResponse.Success<List<SweepDefinition>>>(
                value = response,
            ).data
            assertEquals(
                expected = defaultSweeps,
                actual = actual,
            )
        }

    @Test
    fun `When flow throws an exception, then Flow_waitForESPResponseForESPRequest returns ESPFailure_Unknown`() =
        runTest {
            val exception = Exception("Random Exception")
            val response = flow<ByteArray> {
                delay(20.milliseconds)
                throw exception
            }
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    timeout = 1.seconds,
                ) { null }

            val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = response).data
            assertEquals(
                expected = ESPFailure.Unknown(e = exception),
                actual = actual,
            )
        }

    @Test
    fun `When flow contains a partial Sweep Definition packet sequence, then Flow_waitForESPResponseForESPRequest returns ESPFailure_TimedOut`() = runTest {
        val maxSweeps = defaultSweeps.size
        val sweeps = arrayListOf<SweepDefinition>()
        val response = createRespSweepDefinitionPackets(
            useChecksum = true,
            default = false,
            sweepsDefinitions = defaultSweeps,
        ).take(5)
            .asFlow()
            .waitForESPResponseForESPRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.RespSweepDefinition,
                timeout = 1.seconds,
            ) {
                sweeps.add(it.sweepDefinition())
                if (sweeps.size == maxSweeps) sweeps else null
            }
        val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = response).data
        assertEquals(
            expected = ESPFailure.TimedOut,
            actual = actual,
        )
    }

    @Test
    fun `When a flow does not emit any packets, then Flow_waitForESPResponseForESPRequest returns ESPFailure_TimedOut`() =
        runTest {
            val response = emptyFlow<ByteArray>()
                .waitForESPResponseForESPRequest(
                    requestDestination = ESPDevice.ValentineOne.Checksum,
                    requestPacketId = ESPPacketId.ReqAllSweepDefinitions,
                    responseOrigin = ESPDevice.ValentineOne.Checksum,
                    responsePacketId = ESPPacketId.RespSweepDefinition,
                    timeout = 1.seconds,
                ) { null }

            val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = response).data
            assertEquals(
                expected = ESPFailure.TimedOut,
                actual = actual,
            )
        }

    @Test
    fun `When flow contains 'N' count InfDisplayData packets, Flow_waitForResponseCountForESPRequest returns last InfDisplayData packet`() = runTest {
        val dummyData = packetByteList(count = 2)
        val response = dummyData
            .asFlow()
            .waitForResponseCountForESPRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.InfDisplayData,
                timeout = 1.seconds,
                responsePacketCount = 2,
            )

        val actual = assertIs<ESPResponse.Success<ByteArray>>(value = response).data
        assertEquals(
            expected = dummyData.last(),
            actual = actual,
        )
    }

    @Test
    fun `When flow contains less than 'N' count InfDisplayData packets, Flow_waitForResponseCountForESPRequest returns ESPFailure_TimedOut`() = runTest {
        val dummyData = packetByteList(count = 2)
        val response = dummyData
            .asFlow()
            .waitForResponseCountForESPRequest(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                responseOrigin = ESPDevice.ValentineOne.Checksum,
                responsePacketId = ESPPacketId.InfDisplayData,
                timeout = 1.seconds,
                responsePacketCount = 3,
            )

        val actual = assertIs<ESPResponse.Failure<ESPFailure.TimedOut>>(value = response).data
        assertEquals(
            expected = ESPFailure.TimedOut,
            actual = actual,
        )
    }

    @Test
    fun `When flow contains 'N' count InfDisplayData packets, Flow_waitForNInfDisplayDataUnitObserveRequestFeedback returns last InfDisplayData packet`() = runTest {
        val dummyData = packetByteList(count = 2)
        val response = dummyData
            .asFlow()
            .waitForNInfDisplayDataUnitObserveRequestFeedback(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                v1 = ESPDevice.ValentineOne.Checksum,
                timeout = 1.seconds,
                infDisplayDataCount = 2,
            )

        val actual = assertIs<ESPResponse.Success<ByteArray>>(value = response).data
        assertEquals(
            expected = dummyData.last(),
            actual = actual,
        )
    }

    @Test
    fun `When flow contains less than 'N' count InfDisplayData packets, Flow_waitForNInfDisplayDataUnitObserveRequestFeedback returns ESPFailure_TimedOut`() = runTest {
        val dummyData = packetByteList(count = 2)
        val response = dummyData
            .asFlow()
            .waitForNInfDisplayDataUnitObserveRequestFeedback(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                v1 = ESPDevice.ValentineOne.Checksum,
                timeout = 1.seconds,
                infDisplayDataCount = 3,
            )

        val actual = assertIs<ESPResponse.Failure<ESPFailure.TimedOut>>(value = response).data
        assertEquals(
            expected = ESPFailure.TimedOut,
            actual = actual,
        )
    }

    @Test
    fun `When flow contains InfDisplayData that matches predicate, then Flow_waitForMatchingInfDisplayDataObserveRequestFeedback returns expected packet`() = runTest {
        val response = listOf(
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
            createInfDisplayDataPacket(useChecksum = true, displayData = displayOffDisplayPayload),
        )
            .asFlow()
            .waitForMatchingInfDisplayDataObserveRequestFeedback(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                v1 = ESPDevice.ValentineOne.Checksum,
                timeout = 1.seconds,
            ) { !it.isDisplayOn }

        val actual = assertIs<ESPResponse.Success<ByteArray>>(value = response).data
        assertFalse(actual = actual.isDisplayOn)
    }

    @Test
    fun `When flow does not contain InfDisplayData that matches predicate, then Flow_waitForMatchingInfDisplayDataObserveRequestFeedback returns expected packet`() = runTest {
        val response = listOf(
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
            createInfDisplayDataPacket(useChecksum = true, displayData = defaultDisplayPayload),
        )
            .asFlow()
            .waitForMatchingInfDisplayDataObserveRequestFeedback(
                requestDestination = ESPDevice.ValentineOne.Checksum,
                requestPacketId = ESPPacketId.ReqMuteOn,
                v1 = ESPDevice.ValentineOne.Checksum,
                timeout = 1.seconds,
            ) { !it.isDisplayOn }

        val actual = assertIs<ESPResponse.Failure<ESPFailure>>(value = response).data
        assertEquals(
            expected = ESPFailure.TimedOut,
            actual = actual,
        )
    }
}

private fun packetByteList(
    count: Int,
    valueProvider: (index: Int) -> ByteArray = { createInfDisplayDataPacket(useChecksum = true) }
): List<ByteArray> = buildList { repeat(times = count) { valueProvider(it).also(::add) } }