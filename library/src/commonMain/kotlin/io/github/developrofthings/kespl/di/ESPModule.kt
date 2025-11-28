package io.github.developrofthings.kespl.di

import org.koin.core.annotation.Module

@Module(includes = [PlatformNativeModule::class, PreferencesModule::class])
class ESPModule