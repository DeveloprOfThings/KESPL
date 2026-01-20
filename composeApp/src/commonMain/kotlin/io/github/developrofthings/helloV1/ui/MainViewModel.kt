package io.github.developrofthings.helloV1.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.developrofthings.helloV1.service.IESPService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

class MainViewModel(
    val espService: IESPService,
) : ViewModel() {

    val holdSplashScreen: StateFlow<Boolean> = espService
        .isBluetoothSupported
        .map {
            // Create an artificial delay if BT is not support so we can obscure the transition to
            // the "Unsupported" screen
            if(!it) delay(duration = 150.milliseconds)
            /*
            This flow is primarily needed for iOS in order to keep the "Splash" UI on the screen
            while the library determines detects (request implicit) bluetooth support
             */
            false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )
}