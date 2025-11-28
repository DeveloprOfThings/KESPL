package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.connection.DEMO_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.connection.LE_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import kotlinx.coroutines.CoroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal actual fun getConnection(
    espContext: ESPContext,
    connectionScope: CoroutineScope,
    connType: V1cType,
): IConnection = ESPIsolatedKoinContext.koin.get<IConnection>(
    qualifier = named(
        name = when(connType) {
            // iOS doesn't support RFCOMM/SPP Bluetooth connections
            V1cType.Legacy -> throw IOSLegacyUnsupported()
            V1cType.LE -> LE_CONNECTION_QUALIFIER
            V1cType.Demo -> DEMO_CONNECTION_QUALIFIER
        }
    )
) {
    parametersOf(connectionScope)
}