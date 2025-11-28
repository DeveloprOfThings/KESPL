package io.github.developrofthings.kespl.bluetooth.discovery

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.IOSLegacyUnsupported
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import org.koin.core.qualifier.named

internal actual fun getScanner(
    espContext: ESPContext,
    connType: V1cType
): IV1cScanner = ESPIsolatedKoinContext.koin.get<IV1cScanner>(
    qualifier = named(
        name = when (connType) {
            // iOS doesn't support  RFCOMM/SPP Bluetooth connections
            V1cType.Legacy -> throw IOSLegacyUnsupported()
            V1cType.LE -> V1C_LE_SCANNER_QUALIFER

            V1cType.Demo -> V1C_DEMO_SCANNER_QUALIFER
        }
    )
)