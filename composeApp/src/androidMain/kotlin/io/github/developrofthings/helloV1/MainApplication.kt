package io.github.developrofthings.helloV1

import android.app.Application
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.helloV1.di.initApp

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // Make sure the Koin is is initialized
        initApp(espContext = ESPContext(this))
    }
}