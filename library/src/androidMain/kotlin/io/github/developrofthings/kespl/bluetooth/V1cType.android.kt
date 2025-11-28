package io.github.developrofthings.kespl.bluetooth

import io.github.developrofthings.kespl.ESPContext

internal actual fun supportedV1cTypes(espContext: ESPContext): List<V1cType> = buildList {
    if (espContext.isBluetoothSupported()) add(V1cType.Legacy)
    if (espContext.isBluetoothLESupported()) add(V1cType.LE)
    add(V1cType.Demo)
}