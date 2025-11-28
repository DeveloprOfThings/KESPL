package io.github.developrofthings.helloV1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.developrofthings.helloV1.ui.Unsupported
import io.github.developrofthings.helloV1.ui.display.infDisplayRoute
import io.github.developrofthings.helloV1.ui.display.showInfDisplayMirror
import io.github.developrofthings.helloV1.ui.main.Main
import io.github.developrofthings.helloV1.ui.main.mainRoute
import io.github.developrofthings.helloV1.ui.unsupported
import io.github.developrofthings.helloV1.ui.v1c.showDialog
import io.github.developrofthings.helloV1.ui.v1c.v1connectionDialogRoute

@Composable
fun HelloV1NavHost(
    modifier: Modifier = Modifier,
    isBluetoothSupported: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination = if(isBluetoothSupported) Main else Unsupported
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        unsupported()

        mainRoute(
            onScanClick = navController::showDialog,
            onShowInfDisplayMirror = navController::showInfDisplayMirror,
        )

        infDisplayRoute()

        v1connectionDialogRoute(navController::popBackStack)
    }
}
