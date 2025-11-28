package io.github.developrofthings.kespl.bluetooth.discovery

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import kotlinx.coroutines.flow.Flow

interface IV1cScanner {

    val scanType: V1cType

    fun startScan(scanMode: ESPScanMode = ESPScanMode.LowPower): Flow<V1connectionScanResult>

    companion object {
        fun getScanner(connType: V1cType): IV1cScanner = getScanner(
            espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
            connType = connType,
        )
    }
}

internal expect fun getScanner(
    espContext: ESPContext,
    connType: V1cType
): IV1cScanner

internal const val V1C_DEMO_SCANNER_QUALIFER: String = "V1C_DEMO_QUALIFER"
internal const val V1C_LE_SCANNER_QUALIFER: String = "V1C_LE_QUALIFER"
internal const val V1C_LEGACY_SCANNER_QUALIFER: String = "V1C_LEGACY_QUALIFER"