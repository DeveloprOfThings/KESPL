package io.github.developrofthings.helloV1.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.developrofthings.helloV1.ui.effect.ConnectionEffect
import io.github.developrofthings.helloV1.ui.effect.ScreenOnEffect
import io.github.developrofthings.helloV1.ui.main.Main
import io.github.developrofthings.helloV1.ui.navigation.HelloV1NavHost
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun V1App(appState: HelloV1AppState = rememberHelloV1AppState()) {
    Valentine1Theme {
        val isConnected by appState.isConnected.collectAsStateWithLifecycle(false)
        val snackbarHost = remember { SnackbarHostState() }
        val permissionControllerFactory = rememberPermissionsControllerFactory()
        val permissionController = remember(permissionControllerFactory) {
            permissionControllerFactory.createPermissionsController()
        }
        ScreenOnEffect(keepScreenOn = isConnected)
        ConnectionEffect()
        BindEffect(permissionsController = permissionController)
        BluetoothSupportedEffect(appState = appState) {
            checkPermission(
                controller = permissionController,
                snackbarHostState = snackbarHost
            )
        }
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            contentWindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        ) { padding ->
            HelloV1NavHost(
                navController = appState.navController,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            insets = WindowInsets.safeDrawing.only(
                                sides = WindowInsetsSides.Horizontal,
                            ),
                        )
            )
        }
    }
}

@Composable
fun BluetoothSupportedEffect(
    appState: HelloV1AppState,
    onSupported: suspend () -> Unit,
) {
    LaunchedEffect(key1 = appState) {
        val isBtSupported = appState
            .espService
            .isBluetoothSupported.first()
        if (!isBtSupported) {
            // I don't like it but we need to add a small delay to give the `navController`
            // enough time to be attach to the NavHost
            delay(duration = 25.milliseconds)
            appState.navController.navigate(route = Unsupported) {
                // Pop the main route so that the user cannot return to it
                popUpTo<Main> { inclusive = true }
            }
        } else onSupported()
    }
}

private suspend fun checkPermission(
    controller: PermissionsController,
    snackbarHostState: SnackbarHostState,
) {
    try {
        val allPermissionsGranted = with(controller) {
            isPermissionGranted(Permission.BLUETOOTH_CONNECT) &&
                    isPermissionGranted(Permission.BLUETOOTH_SCAN)
        }

        if (!allPermissionsGranted) {
            controller.providePermission(Permission.BLUETOOTH_CONNECT)
            controller.providePermission(Permission.BLUETOOTH_SCAN)
            snackbarHostState.showSnackbar(
                message = "Permission Granted",
                duration = SnackbarDuration.Short
            )
        }
    } catch (e: Exception) {
        val result = snackbarHostState.showSnackbar(
            message = when (e) {
                is DeniedAlwaysException -> "Permanently denied"
                is DeniedException -> "Permission denied"
                is RequestCanceledException -> "Request canceled"
                else -> "Request canceled"
            },
            actionLabel = "Settings",
            duration = SnackbarDuration.Indefinite,
        )
        if (result == SnackbarResult.ActionPerformed) {
            controller.openAppSettings()
        }
    }
}