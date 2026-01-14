package io.github.developrofthings.kespl.preferences

import io.github.developrofthings.kespl.bluetooth.V1connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class FakeESPPreferencesManager(
    initialDevices: List<V1connection> = emptyList()
) : IESPPreferencesManager {

    private val _persistedDevices: MutableStateFlow<List<V1connection>> = MutableStateFlow(
        value = initialDevices.toList()
    )

    private val _canPersistDevices: MutableStateFlow<Boolean> = MutableStateFlow(value = true)

    override val persistedDevices: Flow<List<V1connection>>
        get() = _persistedDevices.asSharedFlow()

    override suspend fun canPersistDevices(): Boolean = _canPersistDevices.value

    override suspend fun setPersistDevices(canPersist: Boolean) {
        _canPersistDevices.emit(value = canPersist)
    }

    override suspend fun hasPreviousDevice(): Boolean = _persistedDevices.value.isNotEmpty()

    override suspend fun addV1connection(v1c: V1connection) {
        _persistedDevices.emit(
            value = _persistedDevices.value.toMutableList().apply {
                // Remove old instance
                remove(v1c)
                // Add
                add(v1c)
            }
        )
    }

    override suspend fun clearPersistedDevices() {
        _persistedDevices.emit(value = _persistedDevices.value.toMutableList().apply { clear() })
    }

    override suspend fun clearAllPreferences() {
        _persistedDevices.emit(value = _persistedDevices.value.toMutableList().apply { clear() })
        _canPersistDevices.emit(value = true)
    }
}