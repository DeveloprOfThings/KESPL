@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.legacy

import android.bluetooth.BluetoothSocket
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.MutableESPFlowController
import io.github.developrofthings.kespl.bluetooth.BTDevice
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.EspUUID.LEGACY_UUID
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.BaseConnection
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.connection.LEGACY_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_LEGACY_SCANNER_QUALIFER
import io.github.developrofthings.kespl.collection.MutableByteList
import io.github.developrofthings.kespl.getValentineOne
import io.github.developrofthings.kespl.packet.escapeBytesForLegacyWrite
import io.github.developrofthings.kespl.packet.isFromV1
import io.github.developrofthings.kespl.packet.originIdByte
import io.github.developrofthings.kespl.packet.tryAssemblyESPByteArrayFromLegacyDataBuffer
import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * Represents a RFCOMM/SPP Bluetooth connection to a V1connection (Android) device.
 *
 * This class is responsible for establishing and maintaining a Bluetooth connection
 * with older V1 devices that use the RFCOMM/SPP protocol. It manages
 * the Bluetooth socket, input/output streams, and data processing for ESP packets.
 *
 * @property connectionScope The [CoroutineScope] used for managing connection-related coroutines.
 * @property scanner The [IV1cScanner] used for discovering nearby V1 devices.
 */
@Factory(binds = [IConnection::class])
@Named(LEGACY_CONNECTION_QUALIFIER)
internal class LegacyConnection(
    flowController: MutableESPFlowController,
    logger: PlatformLogger,
    @InjectedParam connectionScope: CoroutineScope,
    @Named(V1C_LEGACY_SCANNER_QUALIFER) private val scanner: IV1cScanner,
) : BaseConnection(
    flowController = flowController,
    logger = logger,
    connectionScope = connectionScope,
) {

    override val connectionType: V1cType get() = V1cType.Legacy
    private var _btSocket: BluetoothSocket? = null
    private var _v1OutJob: Job? = null
    private var _outputStream: OutputStream? = null

    override suspend fun performConnection(v1c: V1connection, directConnect: Boolean): Boolean {
        if (v1c !is V1connection.Remote) return false
        return withContext(Dispatchers.IO) {
            try {
                _btSocket = v1c.device.createInsecureRfcommSocketToServiceRecord(LEGACY_UUID)
                    .apply {
                        connect()
                        _v1OutJob = connectionScope.watchForInput(input = inputStream)
                        _outputStream = outputStream
                    }
                true
            } catch (e: IOException) {
                _btSocket?.close()
                logger.error(
                    "LegacyConnection", """
                |Caught an exception while attempting to establish a connection with a Legacy V1connection.
                |Exception: ${e.message}""".trimMargin()
                )
                false
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun CoroutineScope.watchForInput(
        input: InputStream,
    ): Job = launch(Dispatchers.IO) {
        val buffer = MutableByteList(initialCapacity = STREAM_BUFFER_SIZE)
        var lastV1Type: ESPDevice.ValentineOne = ESPDevice.ValentineOne.Unknown
        while (isActive) {
            try {
                with(buffer) { input.readInto(buffer = buffer) }

                while (isActive) {
                    val espData = tryAssemblyESPByteArrayFromLegacyDataBuffer(
                        buffer = buffer,
                        v1Type = lastV1Type,
                    )
                    if (espData == null) break

                    // Store the last V1 type
                    if (espData.isFromV1()) {
                        lastV1Type = getValentineOne(origin = espData.originIdByte)
                    }

                    processESPData(espData = espData)
                    // Break early..
                    if (buffer.isEmpty()) break
                }
            } catch (_: IOException) {
                // Make sure we're still connected
                if (isActive && _connectionStatus.value != ESPConnectionStatus.Connected) return@launch
            }
        }
    }

    override suspend fun performDisconnect() {
        _v1OutJob?.cancel()
        _btSocket?.close()
        onDisconnected()
    }

    override suspend fun writeBytes(bytes: ByteArray): Boolean = _outputStream?.let { output ->
        withContext(Dispatchers.IO) {
            try {
                output.write(escapeBytesForLegacyWrite(bytes = bytes))
                output.flush()
                true
            } catch (e: IOException) {
                logger.error(
                    "LegacyConnection", """
                    |Caught an exception while attempting to write an a BluetoothOutputStream.
                |Exception: ${e.message}""".trimMargin()
                )
                false
            }
        }
    } ?: false

    override suspend fun scan(scanMode: ESPScanMode): Flow<V1connectionScanResult> {
        return scanner.startScan(scanMode)
    }
}

/**
 * Create an RFCOMM [android.bluetooth.BluetoothSocket]  ready to start an insecure outgoing
 * connection to this remote device using SDP lookup of uuid.
 */
private fun BTDevice.createInsecureRfcommSocketToServiceRecord(uuid: Uuid): BluetoothSocket =
    realDevice.createInsecureRfcommSocketToServiceRecord(uuid.toJavaUuid())


private const val STREAM_BUFFER_SIZE = 1024

fun InputStream.readInto(
    buffer: MutableByteList,
    offset: Int = buffer.size,
    count: Int = (buffer.byteData.size - offset),
): Int {
    val s = buffer.size
    return read(
        /* b = */ buffer.byteData,
        /* off = */ offset,
        /* len = */ count
    ).also { readSize ->
        buffer._size = s + readSize
    }
}