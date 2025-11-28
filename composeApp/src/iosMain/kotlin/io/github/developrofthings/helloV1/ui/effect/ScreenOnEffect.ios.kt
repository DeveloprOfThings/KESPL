package io.github.developrofthings.helloV1.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import platform.UIKit.UIApplication

@Composable
actual fun ScreenOnEffect(keepScreenOn: Boolean) {
    // Save the initial screen on value... this is probably not necessary
    val initialScreenOnValue = rememberSaveable { UIApplication.sharedApplication.idleTimerDisabled }
    DisposableEffect(keepScreenOn) {
        UIApplication.sharedApplication.setIdleTimerDisabled(idleTimerDisabled = keepScreenOn)
        // When we exit composition retore to the initial value.
        onDispose {
            UIApplication.sharedApplication.setIdleTimerDisabled(idleTimerDisabled = initialScreenOnValue)
        }
    }
}