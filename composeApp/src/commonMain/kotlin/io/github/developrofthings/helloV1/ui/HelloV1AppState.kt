package io.github.developrofthings.helloV1.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.helloV1.ui.main.Main
import io.github.developrofthings.kespl.IESPClient
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberHelloV1AppState(
    espService: IESPService = koinInject<IESPService>(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): HelloV1AppState = remember(
    navController,
    coroutineScope,
    espService,
) {
    HelloV1AppState(
        navController = navController,
        coroutineScope = coroutineScope,
        espService = espService,
    )
}

class HelloV1AppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val espService: IESPService,
) {
    val isConnected: Flow<Boolean>
        get() = espService
            .connectionStatus
            .map { it == ESPConnectionStatus.Connected }
            .stateIn(
                coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )
}