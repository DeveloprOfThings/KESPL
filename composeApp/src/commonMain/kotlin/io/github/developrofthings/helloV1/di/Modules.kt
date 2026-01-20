package io.github.developrofthings.helloV1.di

import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepositoryImpl
import io.github.developrofthings.helloV1.service.ESPService
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.helloV1.ui.MainViewModel
import io.github.developrofthings.helloV1.ui.controls.ControlsViewModel
import io.github.developrofthings.helloV1.ui.dialog.sweep.SweepInfoPresenter
import io.github.developrofthings.helloV1.ui.dialog.user.UserBytesGuiPresenter
import io.github.developrofthings.helloV1.ui.dialog.volume.VolumePresenter
import io.github.developrofthings.helloV1.ui.display.InfDisplayViewModel
import io.github.developrofthings.helloV1.ui.log.ESPLogViewModel
import io.github.developrofthings.helloV1.ui.v1c.V1cDiscoveryViewModel
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.IESPClient
import io.github.developrofthings.kespl.V1ConnectionTypePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun initApp(
    espContext: ESPContext,
    loggingEnabled: Boolean = true,
    platformModule: Module = module { },
) {
    // Make sure the ESP Library is initialized before we setup koin
    IESPClient.init(
        espContext = espContext,
        loggingEnabled = loggingEnabled,
    )

    initKoin(platformModule = platformModule)
}

private fun initKoin(platformModule: Module): Koin = startKoin {
    val appModule = module {
        factory {
            IESPClient.getClient(
                preference = V1ConnectionTypePreference.Auto,
                // use default coroutine scope
            )
        }

        single {
            ESPDataLogRepositoryImpl(
                espService = get(),
                coroutineScope = get(),
            )
        } bind (ESPDataLogRepository::class)

        single {
            ESPService(
                espClient = get<IESPClient>(),
                coroutineScope = get(),
            )
        } bind (IESPService::class)

        single {
            CoroutineScope(Dispatchers.Main + SupervisorJob())
        } bind (CoroutineScope::class)

        factory { param ->
            UserBytesGuiPresenter(
                targetDevice = param[0],
                userBytes = param[1],
                scope = param[2],
                espService = get<ESPService>(),
                espDataLogRepository = get<ESPDataLogRepository>(),
            )
        }

        factory { param ->
            SweepInfoPresenter(
                scope = param[0],
                espService = get<ESPService>(),
                espDataLogRepository = get<ESPDataLogRepository>(),
            )
        }

        factory { param ->
            VolumePresenter(
                scope = param[0],
                espService = get<ESPService>(),
                espDataLogRepository = get<ESPDataLogRepository>(),
            )
        }
    }

    val viewModels = module {
        viewModel { param ->
            V1cDiscoveryViewModel(
                scanType = param[0],
                espService = get<IESPService>(),
            )
        }
        viewModel { InfDisplayViewModel(espService = get<IESPService>()) }
        viewModel {
            ControlsViewModel(
                espService = get<IESPService>(),
                espDataLogRepository = get<ESPDataLogRepository>(),
            )
        }
        viewModel { ESPLogViewModel(espDataLogRepository = get<ESPDataLogRepository>()) }
        viewModel { MainViewModel(espService = get<IESPService>()) }
    }

    modules(platformModule, appModule, viewModels)
}.koin
