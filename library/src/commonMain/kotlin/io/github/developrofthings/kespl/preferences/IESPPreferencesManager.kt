package io.github.developrofthings.kespl.preferences

import io.github.developrofthings.kespl.bluetooth.V1connection
import kotlinx.coroutines.flow.Flow

internal interface IESPPreferencesManager {
    val persistedDevices: Flow<List<V1connection>>
    suspend fun canPersistDevices(): Boolean
    suspend fun setPersistDevices(canPersist: Boolean)
    suspend fun hasPreviousDevice(): Boolean
    suspend fun addV1connection(v1c: V1connection)
    suspend fun clearPersistedDevices()
    suspend fun clearAllPreferences()
}