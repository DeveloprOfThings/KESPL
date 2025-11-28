package io.github.developrofthings.kespl.di

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.createLogger
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.ksp.generated.module

/**
 * Isolated Koin context such that the ESP Library's dependency injection graph doesn't
 * affect/interfere with dependent apps that may use Koin for dependency injection.
 */
internal object ESPIsolatedKoinContext {

    private var _hasInitialized: AtomicBoolean = atomic(false)

    private val koinApp = koinApplication {
        modules(ESPModule().module)
    }

    val koin: Koin = koinApp.koin

    fun init(
        espContext: ESPContext,
        loggingEnabled: Boolean = false,
    ) {
        if (_hasInitialized.compareAndSet(expect = false, update = true)) {
            koinApp.platformInitialize(espContext = espContext)
            koinApp.modules(
                modules = module {
                    single { espContext } bind ESPContext::class
                    single { createLogger(enabled = loggingEnabled) } bind PlatformLogger::class
                }
            )
        }
    }
}

expect fun KoinApplication.platformInitialize(espContext: ESPContext)
