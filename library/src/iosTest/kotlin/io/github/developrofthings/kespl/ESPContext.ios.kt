package io.github.developrofthings.kespl

actual fun platformContext(
    bluetoothSupported: Boolean,
    bleSupported: Boolean,
): ESPContext = ESPContext(isSimulator = bluetoothSupported)