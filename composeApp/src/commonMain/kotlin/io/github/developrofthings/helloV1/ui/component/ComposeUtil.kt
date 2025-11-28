package io.github.developrofthings.helloV1.ui.component

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
internal fun ColorScheme.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color {
    return when {
        !enabled -> onSurface.copy(alpha = .38f)
        isError -> onSurface
        focused -> onSurface
        else -> onSurface
    }
}