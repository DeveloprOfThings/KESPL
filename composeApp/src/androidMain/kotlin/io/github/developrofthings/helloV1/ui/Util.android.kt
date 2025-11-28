package io.github.developrofthings.helloV1.ui

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
actual fun EdgeToEdge(
    lightIcons: Boolean,
    statusBarColor: Color,
    navBarColor: Color
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val composeAct: ComponentActivity = composeActivity
        // Update the edge to edge configuration to match the theme
        composeAct.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                statusBarColor.toArgb(),
                statusBarColor.toArgb(),
            ) { lightIcons },
            navigationBarStyle = SystemBarStyle.auto(
                navBarColor.toArgb(),
                navBarColor.toArgb(),
            ) { lightIcons },
        )
    }
}

val composeActivity: ComponentActivity
    @Composable
    get() {
        var ctx = LocalContext.current
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return ctx
            ctx = ctx.baseContext
        }
        throw IllegalArgumentException("No ComponentActivity")
    }