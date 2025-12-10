@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.utilities.PlatformLogger
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class IOSLeServiceWrapper(
    private val peripheral: CBPeripheral,
    private val service: CBService,
    private val isConnected: suspend () -> Boolean,
    private val logger: PlatformLogger,
    private val cbEvents: SharedFlow<ESPCoreBluetoothEvent>,
    private val mutex: Mutex,
): PlatformLeServiceWrapper {

    override val uuid: Uuid get() = Uuid.parse(uuidString = service.UUID.UUIDString)

    override val characteristics: List<PlatformLeCharacteristicWrapper> = service.characteristics?.map {
        IOSLeCharacteristicWrapper(
            peripheral = peripheral,
            characteristic = it as CBCharacteristic,
            isConnected = isConnected,
            logger = logger,
            cbEvents = cbEvents,
            mutex = mutex,
        )
    } ?: emptyList()
}
