package io.github.developrofthings.helloV1.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.developrofthings.helloV1.service.IESPService
import kotlinx.coroutines.flow.launchIn
import org.koin.compose.koinInject

@Composable
fun ConnectionEffect(espService: IESPService = koinInject()) {
    val coroutineScope = rememberCoroutineScope()
    LifecycleStartEffect(espService) {
        val connectJob = espService.connection
            .launchIn(coroutineScope)

        // Separately observe connection loss
        val connectionLossJob = espService
            .connectionLoss
            .launchIn(coroutineScope)

        onStopOrDispose {
            connectionLossJob.cancel()
            connectJob.cancel()
        }
    }
}