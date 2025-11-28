@file:OptIn(ExperimentalSerializationApi::class)

package io.github.developrofthings.kespl.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal data class ESPPreferences(
    @ProtoNumber(1)
    val isPersistingLastDevices: Boolean,
    @ProtoNumber(2)
    val lastDevices: List<V1CRecord> = emptyList()
) {
    companion object {
        val DEFAULT: ESPPreferences = ESPPreferences(
            isPersistingLastDevices = true,
            lastDevices = emptyList(),
        )
    }
}


@Serializable
internal data class V1CRecord(
    @ProtoNumber(1)
    val identifier: String,
    @ProtoNumber(2)
    val connectionType: V1cConnType,
)

@Serializable
internal enum class V1cConnType {
    @ProtoNumber(0)
    V1C_CONN_TYPE_UNSPECIFIED,
    @ProtoNumber(1)
    V1C_CONN_TYPE_LE,
    @ProtoNumber(2)
    V1C_CONN_TYPE_LEGACY,
    @ProtoNumber(3)
    V1C_CONN_TYPE_DEMO
}