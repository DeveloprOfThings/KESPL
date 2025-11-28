@file:OptIn(ExperimentalUuidApi::class)

package io.github.developrofthings.kespl.bluetooth.connection.le

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal interface PlatformLeCharacteristicWrapper {

    val uuid: Uuid

    suspend fun notifications(): Flow<ByteArray>

    suspend fun write(data: ByteArray)
}