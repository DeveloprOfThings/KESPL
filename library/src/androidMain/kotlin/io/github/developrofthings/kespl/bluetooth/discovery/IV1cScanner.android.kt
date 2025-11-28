package io.github.developrofthings.kespl.bluetooth.discovery

import io.github.developrofthings.kespl.BTUnsupported
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.LeUnsupported
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import org.koin.core.qualifier.named

internal actual fun getScanner(
    espContext: ESPContext,
    connType: V1cType
): IV1cScanner = with(espContext) {
    ESPIsolatedKoinContext.koin.get<IV1cScanner>(
        qualifier = named(
            name = when (connType) {
                V1cType.Legacy -> {
                    if (!isBluetoothSupported()) throw BTUnsupported()

                    V1C_LEGACY_SCANNER_QUALIFER
                }

                V1cType.LE -> {
                    if (!isBluetoothSupported()) throw BTUnsupported()
                    else if (!isBluetoothLESupported()) throw LeUnsupported()

                    V1C_LE_SCANNER_QUALIFER
                }

                V1cType.Demo -> V1C_DEMO_SCANNER_QUALIFER
            }
        )
    )
}