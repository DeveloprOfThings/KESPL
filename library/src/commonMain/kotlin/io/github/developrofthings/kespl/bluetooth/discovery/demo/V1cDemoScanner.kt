package io.github.developrofthings.kespl.bluetooth.discovery.demo

import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.bluetooth.discovery.ESPScanMode
import io.github.developrofthings.kespl.bluetooth.discovery.IV1cScanner
import io.github.developrofthings.kespl.bluetooth.discovery.V1C_DEMO_SCANNER_QUALIFER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

/**
 * A "demo" [IV1cScanner] that will never discover/emit a [V1connectionScanResult].
 */
@Factory
@Named(V1C_DEMO_SCANNER_QUALIFER)
internal class V1cDemoScanner internal constructor(): IV1cScanner {

    override val scanType: V1cType
        get() = V1cType.Demo

    override fun startScan(scanMode: ESPScanMode): Flow<V1connectionScanResult> = emptyFlow()
}