package io.github.developrofthings.kespl.preferences

import androidx.datastore.core.DataStore
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import io.github.developrofthings.kespl.proto.ESPPreferences
import io.github.developrofthings.kespl.proto.V1CRecord
import io.github.developrofthings.kespl.proto.V1cConnType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.annotation.Single

@Single
internal class ESPPreferencesManager(
    private val bluetoothManager: IBluetoothManager,
    private val preferencesDataStore: DataStore<ESPPreferences>,
) : IESPPreferencesManager {
    private val preferences: Flow<ESPPreferences> get() = preferencesDataStore.data

    override val persistedDevices: Flow<List<V1connection>>
        get() = preferences.mapNotNull {
            if (!bluetoothManager.checkIsBluetoothSupported()) return@mapNotNull emptyList()
            it.lastDevices.mapNotNull { it -> it.toV1connection(bluetoothManager) }
        }

    override suspend fun canPersistDevices(): Boolean =
        preferences.map { it.isPersistingLastDevices }.first()

    override suspend fun setPersistDevices(canPersist: Boolean) {
        preferencesDataStore.updateData { current ->
            current.copy(isPersistingLastDevices = canPersist)
        }
    }

    override suspend fun hasPreviousDevice(): Boolean =
        preferences.firstOrNull()?.lastDevices?.isNotEmpty() ?: false

    override suspend fun addV1connection(v1c: V1connection) {
        // Disable persisting demo devices
        if (!v1c.canSave) return

        preferencesDataStore.updateData { current: ESPPreferences ->
            v1c.toRecord().let { rec ->
                val existingDevices = current.lastDevices.toMutableList()
                // If the devices has already been persisted, we want to remove it otherwise check
                // to see if we've reached the maximum persisted device
                if (existingDevices.contains(rec)) existingDevices.remove(rec)
                else if (existingDevices.size == maxDevicesToPersist) existingDevices.removeAt(0)

                // New devices are added to the tail of the list
                existingDevices.add(v1c.toRecord())
                current.copy(
                    lastDevices = existingDevices
                )
            }
        }
    }

    override suspend fun clearPersistedDevices() {
        preferencesDataStore.updateData { current ->
            current.copy(
                lastDevices = emptyList(),
            )
        }
    }

    override suspend fun clearAllPreferences() {
        preferencesDataStore.updateData { current ->
            current.copy(
                isPersistingLastDevices = true,
                lastDevices = emptyList(),
            )
        }
    }
}

private fun V1connection.toRecord(): V1CRecord = V1CRecord(
    identifier = this@toRecord.id,
    connectionType = when (this@toRecord.type) {
        V1cType.Legacy -> V1cConnType.V1C_CONN_TYPE_LEGACY
        V1cType.LE -> V1cConnType.V1C_CONN_TYPE_LE
        V1cType.Demo -> V1cConnType.V1C_CONN_TYPE_DEMO
    }
)

private suspend fun V1CRecord.toV1connection(
    bluetoothManager: IBluetoothManager,
): V1connection? = bluetoothManager.tryAcquireBTDevice(
    this.identifier,
)?.let { bTDevice ->
    return when (this.connectionType) {
        V1cConnType.V1C_CONN_TYPE_LE -> V1connection.Remote(
            device = bTDevice,
            type = V1cType.LE
        )

        V1cConnType.V1C_CONN_TYPE_LEGACY -> V1connection.Remote(
            device = bTDevice,
            type = V1cType.Legacy
        )

        V1cConnType.V1C_CONN_TYPE_UNSPECIFIED,
        V1cConnType.V1C_CONN_TYPE_DEMO,
            -> throw IllegalStateException()
    }
}

private const val maxDevicesToPersist: Int = 3