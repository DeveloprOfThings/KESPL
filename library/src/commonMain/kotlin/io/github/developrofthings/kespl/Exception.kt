package io.github.developrofthings.kespl

import kotlin.coroutines.cancellation.CancellationException

open class V1connectionException(message: String? = null): CancellationException(message)

class BTUnsupported: V1connectionException("Bluetooth service is not supported on this device.")

class IOSLegacyUnsupported: V1connectionException("Legacy Connections are not supported on iOS.")

class LeUnsupported: V1connectionException("Bluetooth Low Energy (LE) is not supported on current device.")

class ESPOutOfMemoryError(message: String?) : Error(message)
