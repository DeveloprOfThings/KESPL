package io.github.developrofthings.kespl.bluetooth.discovery

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connectionScanResult
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the contract for scanning and discovering V1connection devices.
 *
 * Implementations provide the ability to search for specific Valentine One connection types
 * (e.g., LE, Legacy, or Demo) and stream the discovery results.
 */
interface IV1cScanner {

    /**
     * The specific type of V1connection that this scanner is designed to discover.
     */
    val scanType: V1cType

    /**
     * Starts scanning for V1connection devices based on the specified [scanMode].
     *
     * @param scanMode The power and performance settings to use for the scan. Defaults to [ESPScanMode.LowPower].
     * @return A [Flow] emitting [V1connectionScanResult] as devices are discovered.
     */
    fun startScan(scanMode: ESPScanMode = ESPScanMode.LowPower): Flow<V1connectionScanResult>

    companion object {
        /**
         * Returns an instance of [IV1cScanner] for the specified [connType].
         *
         * This convenience method automatically retrieves the required [ESPContext]
         * from the [ESPIsolatedKoinContext].
         *
         * @param connType The type of V1connection to scan for (e.g., LE, Legacy, or Demo).
         * @return An implementation of [IV1cScanner] corresponding to the provided [connType].
         */
        fun getScanner(connType: V1cType): IV1cScanner = getScanner(
            espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
            connType = connType,
        )
    }
}

/**
 * Factory function to obtain a platform-specific [IV1cScanner] implementation.
 *
 * @param espContext The application context or environment required for scanner initialization.
 * @param connType The specific Bluetooth connection type ([V1cType]) to create a scanner for.
 * @return An instance of [IV1cScanner] configured for the specified connection type.
 */
internal expect fun getScanner(
    espContext: ESPContext,
    connType: V1cType
): IV1cScanner

internal const val V1C_DEMO_SCANNER_QUALIFER: String = "V1C_DEMO_QUALIFER"
internal const val V1C_LE_SCANNER_QUALIFER: String = "V1C_LE_QUALIFER"
internal const val V1C_LEGACY_SCANNER_QUALIFER: String = "V1C_LEGACY_QUALIFER"