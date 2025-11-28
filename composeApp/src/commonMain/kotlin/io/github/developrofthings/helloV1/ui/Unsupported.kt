package io.github.developrofthings.helloV1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.ic_block
import hellov1.composeapp.generated.resources.ic_bluetooth
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Serializable
data object Unsupported

fun NavGraphBuilder.unsupported() {
    composable<Unsupported> { UnsupportedScreen() }
}

@Preview
@Composable
private fun UnsupportedScreen() = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.errorContainer),
    contentAlignment = Alignment.Center,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center,) {
            Image(
                imageVector = vectorResource(Res.drawable.ic_bluetooth),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Image(
                imageVector = vectorResource(Res.drawable.ic_block),
                contentDescription = null,
                modifier = Modifier.size(275.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
            )
        }

        Text(
            text = "Bluetooth Unsupported!",
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
    }
}