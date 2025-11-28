package io.github.developrofthings.kespl.bluetooth.discovery.le

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.bluetooth.BTDevice
import io.github.developrofthings.kespl.bluetooth.EspUUID
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_LE_SCANNER_QUALIFER
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.extensions.acquireBtAdapter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

/**
 * Implementation of [IV1cScanner] for scanning for Bluetooth Low Energy (LE)
 * [V1connection] devices.
 *
 * It filters for devices advertising the [EspUUID.V1CONNECTION_LE_SERVICE_UUID].
 *
 * @property bluetoothAdapter The Android [BluetoothAdapter] used for scanning.
 */
@Factory
@Named(V1C_LE_SCANNER_QUALIFER)
@OptIn(ExperimentalUuidApi::class)
internal class AndroidV1cLeScanner internal constructor(
    espContext: ESPContext,
    private val logger: PlatformLogger,
) : IV1cScanner {

    val bluetoothAdapter: BluetoothAdapter = espContext.acquireBtAdapter()

    override val scanType: V1cType
        get() = V1cType.LE

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScan(
        scanMode: ESPScanMode,
    ): Flow<V1connectionScanResult> {
        val leScanner = bluetoothAdapter.bluetoothLeScanner ?: return emptyFlow()
        return callbackFlow {
            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.toV1connection()
                    val rssi = result.rssi
                    logger.info(
                        tag = "LEV1cScanner",
                        message = "${device.name} discovered, RSSI: $rssi"
                    )

                    trySendBlocking(
                        V1connectionScanResult(
                            rssi = rssi,
                            device = device
                        )
                    )
                }
            }
            // Creates a scanFilter Builder for the V1 LE UUID.

            val filters = listOf(
                ScanFilter.Builder()
                    .setServiceUuid(
                        ParcelUuid(EspUUID.V1CONNECTION_LE_SERVICE_UUID.toJavaUuid())
                    )
                    .build()
            )

            val scanSetting = ScanSettings.Builder().apply {
                // If we are running on the apis that supports MatchMode use MATCH_MODE_AGGRESSIVE.
                setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                setScanMode(scanMode.toAndroidBLEScanMode())
            }.build()

            leScanner.startScan(
                /* filters = */ filters,
                /* settings = */ scanSetting,
                /* callback = */ scanCallback
            )

            awaitClose {
                logger.info(tag = "LEV1cScanner", message = "Stopping discovery")
                leScanner.stopScan(scanCallback)
            }
        }
    }
}

private fun ESPScanMode.toAndroidBLEScanMode(): Int = when (this) {
    ESPScanMode.Opportunistic -> ScanSettings.SCAN_MODE_OPPORTUNISTIC
    ESPScanMode.LowPower -> ScanSettings.SCAN_MODE_LOW_POWER
    ESPScanMode.Balanced -> ScanSettings.SCAN_MODE_BALANCED
    ESPScanMode.LowLatency -> ScanSettings.SCAN_MODE_LOW_LATENCY
}

@SuppressLint("MissingPermission")
private fun ScanResult.toV1connection(): V1connection = V1connection.Remote(
    device = BTDevice(this.device),
    type = V1cType.LE,
)