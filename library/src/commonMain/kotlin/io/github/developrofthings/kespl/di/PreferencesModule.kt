package io.github.developrofthings.kespl.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.preferences.ESPPreferencesSerializer
import io.github.developrofthings.kespl.proto.ESPPreferences
import okio.Path.Companion.toPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@ComponentScan("io.github.developrofthings.kespl.preferences")
internal class PreferencesModule {

    @Single
    fun providesDataStore(scope: Scope): DataStore<ESPPreferences> = createDataStore(
        espContext = scope.get<ESPContext>()
    )
}

internal fun createDataStore(espContext: ESPContext): DataStore<ESPPreferences> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = espContext.platformFileSystem,
            producePath = {
                espContext.resolvePath(fileName = DATA_STORE_FILE_NAME).toPath()
            },
            serializer = ESPPreferencesSerializer,
        ),
    )


internal const val DATA_STORE_FILE_NAME = "esp_datastore.preferences_pb"