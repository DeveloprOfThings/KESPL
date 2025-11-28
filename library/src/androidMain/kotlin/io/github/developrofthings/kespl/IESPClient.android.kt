package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.connection.DEMO_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.connection.IConnection
import io.github.developrofthings.kespl.bluetooth.connection.LEGACY_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.bluetooth.connection.LE_CONNECTION_QUALIFIER
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import kotlinx.coroutines.CoroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal actual fun getConnection(
    espContext: ESPContext,
    connectionScope: CoroutineScope,
    connType: V1cType
): IConnection = with(espContext) {
    ESPIsolatedKoinContext.koin.get<IConnection>(
        qualifier = named(
            name = when (connType) {
                V1cType.Legacy -> {
                    if (!isBluetoothSupported()) throw BTUnsupported()

                    LEGACY_CONNECTION_QUALIFIER
                }

                V1cType.LE -> {
                    if (!isBluetoothSupported()) throw BTUnsupported()
                    else if (!isBluetoothLESupported()) throw LeUnsupported()

                    LE_CONNECTION_QUALIFIER
                }

                V1cType.Demo -> DEMO_CONNECTION_QUALIFIER
            }
        )
    ) {
        parametersOf(connectionScope)
    }
}