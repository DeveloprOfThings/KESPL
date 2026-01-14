package io.github.developrofthings.kespl

expect fun platformContext(
    bluetoothSupported: Boolean = true,
    bleSupported: Boolean = true,
): ESPContext
