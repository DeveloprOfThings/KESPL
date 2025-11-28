@file:OptIn(ExperimentalSerializationApi::class)

package io.github.developrofthings.kespl.preferences

import androidx.datastore.core.okio.OkioSerializer
import io.github.developrofthings.kespl.proto.ESPPreferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource

internal object ESPPreferencesSerializer : OkioSerializer<ESPPreferences> {
    override val defaultValue: ESPPreferences
        get() = ESPPreferences.DEFAULT

    override suspend fun readFrom(source: BufferedSource): ESPPreferences =
        ProtoBuf.decodeFromByteArray<ESPPreferences>(bytes = source.readByteArray())

    override suspend fun writeTo(t: ESPPreferences, sink: BufferedSink) {
        sink.write(
            source = ProtoBuf.encodeToByteArray(t)
        )
    }
}