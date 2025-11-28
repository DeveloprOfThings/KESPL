package io.github.developrofthings.kespl.di

import android.app.Application
import android.content.Context
import io.github.developrofthings.kespl.ESPContext
import org.koin.core.KoinApplication
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun KoinApplication.platformInitialize(espContext: ESPContext) {
    modules(
        module {
            espContext.appContext.also { appContext ->
                if (appContext is Application) {
                    single { appContext } bind (Context::class)
                } else {
                    single { appContext }
                }
            }
        }
    )
}