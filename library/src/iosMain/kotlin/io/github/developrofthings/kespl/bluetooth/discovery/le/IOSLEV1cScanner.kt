package io.github.developrofthings.kespl.bluetooth.discovery.le

import V1CONNECTION_LE_SERVICE_CBUUID
import io.github.developrofthings.kespl.bluetooth.BTDevice
import io.github.developrofthings.kespl.bluetooth.EspUUID
import io.github.developrofthings.kespl.bluetooth.IOSBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.connection.le.CentralManagerDiscoveryEvent
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_LE_SCANNER_QUALIFER
import io.github.developrofthings.kespl.bluetooth.discovery.le.startScan
import io.github.developrofthings.kespl.bluetooth.discovery.le.toScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.le.toV1connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerScanOptionAllowDuplicatesKey
import platform.CoreBluetooth.CBPeripheral
import kotlin.uuid.ExperimentalUuidApi

@Factory
@Named(V1C_LE_SCANNER_QUALIFER)
@OptIn(ExperimentalUuidApi::class)
internal class IOSLEV1cScanner(
    private val bluetoothManager: IOSBluetoothManager,
) : IV1cScanner {
    private val _cbCentralManager = bluetoothManager.cbCentralManager
    private val _centralManagerDelegate = bluetoothManager.delegate

    override val scanType: V1cType
        get() = V1cType.LE

    override fun startScan(scanMode: ESPScanMode): Flow<V1connectionScanResult> =
        _centralManagerDelegate
            .events
            .filterIsInstance<CentralManagerDiscoveryEvent>()
            .map { it.toScanResult() }
            .onStart {
                _cbCentralManager.startScan()
            }
            // Once the flow
            .onCompletion {
                _cbCentralManager.stopScan()
            }
}

private fun CentralManagerDiscoveryEvent.toScanResult(): V1connectionScanResult =
    V1connectionScanResult(
        rssi = this.rssi,
        device = peripheral.toV1connection()
    )

private fun CBPeripheral.toV1connection(): V1connection = V1connection.Remote(
    device = BTDevice(realDevice = this),
    type = V1cType.LE,
)


@OptIn(ExperimentalUuidApi::class)
private fun CBCentralManager.startScan(): Unit = this.scanForPeripheralsWithServices(
    serviceUUIDs = listOf(EspUUID.V1CONNECTION_LE_SERVICE_CBUUID),
    // Allow duplicate devices, this will enable updating RSSI
    options = mapOf(
        CBCentralManagerScanOptionAllowDuplicatesKey to true
    ),
)