package io.github.developrofthings.kespl.di

import io.github.developrofthings.kespl.ESPContext
import org.koin.core.KoinApplication

actual fun KoinApplication.platformInitialize(espContext: ESPContext) {}