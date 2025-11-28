package io.github.developrofthings.kespl.bluetooth.discovery.legacy

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import io.github.developrofthings.kespl.BTUnsupported
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.bluetooth.BTDevice
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_LEGACY_SCANNER_QUALIFER
import io.github.developrofthings.kespl.bluetooth.isV1Connection
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.extensions.acquireBtAdapter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

/**
 * Implementation of [IV1cScanner] for scanning for RFCOMM/SPP
 * [V1connection] devices.
 *
 * @property bluetoothAdapter The [BluetoothAdapter] used to perform discovery.
 * @property espContext The [ESPContext] used to register/unregister the discovery [BroadcastReceiver].
 */
@Factory
@Named(V1C_LEGACY_SCANNER_QUALIFER)
class AndroidV1cLegacyScanner(
    private val espContext: ESPContext,
    private val logger: PlatformLogger,
) : IV1cScanner {

    /**
     * Assumes Bluetooth is supported; calling [acquireBtAdapter] will throw [BTUnsupported] on
     * devices that do not support Bluetooth.
     */
    val bluetoothAdapter: BluetoothAdapter = espContext.acquireBtAdapter()

    override val scanType: V1cType
        get() = V1cType.Legacy

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun startScan(scanMode: ESPScanMode): Flow<V1connectionScanResult> {
        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(ctx: Context, intent: Intent) {
                    val action = intent.action
                    when (action) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            logger.info("LegacyV1cScanner", "discovery started")
                        }

                        BluetoothDevice.ACTION_FOUND -> {
                            intent.device?.takeIf { device ->
                                device.name?.let { name -> isV1Connection(name) } ?: false
                            }?.let { device ->
                                val rssi = intent.rssi
                                logger.info(
                                    tag = "LegacyV1cScanner",
                                    message = "${device.name} discovered, RSSI: $rssi"
                                )

                                trySendBlocking(
                                    element = V1connectionScanResult(
                                        rssi = rssi,
                                        device = device.toV1connection()
                                    )
                                )
                            }
                        }

                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            logger.info(
                                tag = "LegacyV1cScanner",
                                message = if (isActive) "discovery ended, restarting"
                                else "discovery ended"
                            )
                            if (isActive) bluetoothAdapter.startDiscovery()
                            else channel.close()
                        }

                        else -> { /*Intentionally Left Blank*/
                        }
                    }
                }
            }

            espContext.appContext.registerReceiver(
                /* receiver = */ receiver,
                /* filter = */ IntentFilter(BluetoothDevice.ACTION_FOUND)
                    .apply {
                        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                    }
            )

            bluetoothAdapter.startDiscovery()

            // Suspend until the flow collection has been cancelled, at that point clear the broadcast
            // receiver
            awaitClose {
                espContext.appContext.unregisterReceiver(receiver)
                bluetoothAdapter.cancelDiscovery()
            }
        }
    }

}


private val Intent.device: BluetoothDevice?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(
            BluetoothDevice.EXTRA_DEVICE,
            BluetoothDevice::class.java
        )
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
    }

private val Intent.rssi: Int
    get() = getShortExtra(
        BluetoothDevice.EXTRA_RSSI,
        (-127).toShort()
    ).toInt()

private fun BluetoothDevice.toV1connection(): V1connection = V1connection.Remote(
    device = BTDevice(this),
    type = V1cType.Legacy,
)