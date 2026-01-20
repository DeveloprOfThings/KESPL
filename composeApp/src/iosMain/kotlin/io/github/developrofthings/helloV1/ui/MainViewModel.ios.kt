package io.github.developrofthings.helloV1.ui

import org.koin.mp.KoinPlatform
// Helper method for DI MainViewModel from Swift code using cleaner syntax
fun getMainVM(): MainViewModel = KoinPlatform.getKoin().get()