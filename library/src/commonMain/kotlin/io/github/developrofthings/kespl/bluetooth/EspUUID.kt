@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object EspUUID {

    val LEGACY_UUID: Uuid = Uuid.parse(uuidString = LEGACY_UUID_STR)

    val V1CONNECTION_LE_SERVICE_UUID: Uuid =
        Uuid.parse(uuidString = V1CONNECTION_LE_SERVICE_UUID_STR)

    val CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_UUID: Uuid =
        Uuid.parse(uuidString = CLIENT_OUT_V1_IN_SHORT_CHAR_UUID_STR)

    val V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_UUID: Uuid =
        Uuid.parse(uuidString = V1_OUT_CLIENT_IN_SHORT_UUID_STR)

    val CLIENT_CHARACTERISTIC_CONFIG_CHARACTERISTIC_UUID: Uuid =
        Uuid.parse(uuidString = CLIENT_CHARACTERISTIC_CONFIG_STR)
}

private const val LEGACY_UUID_STR: String = "00001101-0000-1000-8000-00805f9b34fb"
private const val V1CONNECTION_LE_SERVICE_UUID_STR: String = "92A0AFF4-9E05-11E2-AA59-F23C91AEC05E"
private const val CLIENT_OUT_V1_IN_SHORT_CHAR_UUID_STR: String = "92A0B6D4-9E05-11E2-AA59-F23C91AEC05E"
private const val V1_OUT_CLIENT_IN_SHORT_UUID_STR: String = "92A0B2CE-9E05-11E2-AA59-F23C91AEC05E"
private const val CLIENT_CHARACTERISTIC_CONFIG_STR: String = "00002902-0000-1000-8000-00805f9b34fb"