package io.github.developrofthings.helloV1.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView

@Composable
actual fun ScreenOnEffect(keepScreenOn: Boolean) {
    val localView = LocalView.current
    // Save the initial screen on value... this is probably not necessary
    val initialScreenOnValue = rememberSaveable { localView.keepScreenOn }
    DisposableEffect(keepScreenOn) {
        localView.keepScreenOn = keepScreenOn
        // When we exit composition retore to the initial value.
        onDispose { localView.keepScreenOn = initialScreenOnValue }
    }
}