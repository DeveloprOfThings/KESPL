@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.packet.toHexString
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.bluetooth.connection.le.filterForCharacteristic
import io.github.developrofthings.kespl.bluetooth.connection.le.filterForCharacteristicWriteResponse
import io.github.developrofthings.kespl.bluetooth.connection.le.isConnectionInterrupted
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBPeripheral
import toNSData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class IOSLeCharacteristicWrapper(
    private val peripheral: CBPeripheral,
    private val characteristic: CBCharacteristic,
    private val isConnected: suspend () -> Boolean,
    private val logger: PlatformLogger,
    private val cbEvents: SharedFlow<io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent>,
    private val mutex: Mutex,
) : PlatformLeCharacteristicWrapper {
    override val uuid: Uuid get() = Uuid.parse(uuidString = characteristic.UUID.UUIDString)

    override suspend fun notifications(): Flow<ByteArray> {
        if (!isConnected()) return flow { throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralNotConnectedException() }

        return cbEvents
            .onSubscription { enableNotifications(enable = true) }
            .takeWhile { !it.isConnectionInterrupted(peripheral) }
            .filterForCharacteristic<io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateValue>(characteristic)
            .map { it.value }
    }

    private suspend fun enableNotifications(enable: Boolean) {
        if (characteristic.isNotifying == enable) {
            logger.info(
                tag = "IOSLeCharacteristicWrapper",
                message = """Notifications already ${if (enable) "enabled" else "disabled"} for 
                    |characteristic: $uuid"""
                    .trimMargin()
            )
            return
        }
        if (!isConnected()) {
            logger.error(
                tag = "IOSLeCharacteristicWrapper",
                message = """Failed to ${if (enable) "enable" else "disable"} notifications for 
                    |characteristic: $uuid because there is no connection established."""
                    .trimMargin()
            )
        }

        logger.info(
            tag = "IOSLeCharacteristicWrapper",
            message = """${if (enable) "Enabling" else "Disabling"} notifications for characteristic: $uuid""".trimMargin()
        )

        mutex.withLock {
            cbEvents
                .onSubscription {
                    peripheral.setNotifyValue(
                        enabled = enable,
                        forCharacteristic = characteristic
                    )
                }
                .takeWhile { !it.isConnectionInterrupted(peripheral) }
                .filterForCharacteristic<io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateNotificationState>(characteristic)
                .firstOrNull()?.let { event ->
                    // If we failed to change notifications throw an Exception we are in a bad state
                    if (!event.isSuccessful() || event.enabled != enable) {
                        throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.EnableNotificationsException(
                            enable = enable,
                            uuid = uuid
                        )
                    }
                }
                ?: throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.UnknownException(
                    operation = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CoreBluetoothOperation.CharacteristicNotifications
                )
        }
    }

    @OptIn(FlowPreview::class)
    override suspend fun write(data: ByteArray) {
        if (!isConnected()) {
            logger.error(
                tag = "AndroidLeCharacteristicWrapper",
                message = """Failed to write ${data.toHexString()} to characteristic: $uuid because 
                    |there is no connection established."""
                    .trimMargin()
            )
            throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralNotConnectedException()
        }

        mutex.withLock {
            cbEvents
                .onSubscription {
                    peripheral.writeValue(
                        data = data.toNSData(),
                        forCharacteristic = characteristic,
                        type = CBCharacteristicWriteWithoutResponse,
                    )
                }
                .takeWhile { !it.isConnectionInterrupted(peripheral = peripheral) }
                .filterForCharacteristicWriteResponse()
                .timeout(_root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.writeTimeout)
                .catch { exception ->
                    // If we have timed out we emit an artificial (false).. this can happen because
                    // iOS's CoreBluetooth API doesn't provide a reliable characteristic write
                    // callback, like Android does, to determine when it's safe write again to this
                    // characteristics
                    if (exception is TimeoutCancellationException) {
                        emit(false)
                    } else throw exception
                }
                .firstOrNull()?.let { successful ->
                    // If the write wasn't successfully treat this exceptionally since the library
                    // doesn't currently have a retry mechanism and the assumption this would only
                    // happen do to programmer error
                    if (!successful) throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicWriteException()
                } ?: throw _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.UnknownException(
                operation = _root_ide_package_.io.github.developrofthings.kespl.bluetooth.connection.le.CoreBluetoothOperation.CharacteristicWrite
            )
        }
    }
}

private fun Flow<io.github.developrofthings.kespl.bluetooth.connection.le.ESPCoreBluetoothEvent>.filterForCharacteristicWriteResponse(): Flow<Boolean> =
    filter { (it is io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateValue) or (it is io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralIsReadyToSend) }
        .map {
            // All Bluetooth writes use the `CBCharacteristicWriteWithoutResponse` write type so we
            // will not get a call to the `peripheral:didWriteValueForCharacteristic` so the only
            // way we can "confirm" characteristic writes is by waiting for a call to
            // `peripheralIsReadyToSendWriteWithoutResponse`... It is assumed characteristic write
            // errors will call `peripheral:didWriteValueForCharacteristic`.
            if (it is io.github.developrofthings.kespl.bluetooth.connection.le.PeripheralIsReadyToSend) true
            else (it as io.github.developrofthings.kespl.bluetooth.connection.le.CharacteristicDidUpdateValue).isSuccessful()
        }

private val writeTimeout: Duration = 3000.milliseconds