package io.github.developrofthings.helloV1.di

import io.github.developrofthings.kespl.ESPContext
import org.koin.dsl.module

@Suppress("unused")
fun initApp(
    isSimulator: Boolean,
) {
    initApp(
        espContext = ESPContext(
            isSimulator = isSimulator,
        ),
        platformModule = module {},
    )
}