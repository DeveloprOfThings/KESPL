@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import kotlin.uuid.ExperimentalUuidApi

open class ESPLeException(message: String? = null) : Exception(message)

class LeConnectionFailed(deviceName: String) : ESPLeException("Failed to establish a connection with a $deviceName")