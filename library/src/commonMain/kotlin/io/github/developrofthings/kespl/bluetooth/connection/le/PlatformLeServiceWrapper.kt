@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import io.github.developrofthings.kespl.bluetooth.EspUUID.CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_UUID
import io.github.developrofthings.kespl.bluetooth.EspUUID.V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal interface PlatformLeServiceWrapper {

    val uuid: Uuid

    val characteristics: List<PlatformLeCharacteristicWrapper>

    val clientOutV1In: PlatformLeCharacteristicWrapper
        get() = characteristics.first { it.uuid == CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_UUID }

    val v1InClientInt: PlatformLeCharacteristicWrapper
        get() = characteristics.first { it.uuid == V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_UUID }
}