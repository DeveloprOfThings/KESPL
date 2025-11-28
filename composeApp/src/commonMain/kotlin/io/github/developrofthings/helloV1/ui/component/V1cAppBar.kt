package io.github.developrofthings.helloV1.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.bluetooth.ESPConnectionStatus
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.V1connection
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.ic_bluetooth_searching
import hellov1.composeapp.generated.resources.no_device_selected
import hellov1.composeapp.generated.resources.scan_for_v1c
import hellov1.composeapp.generated.resources.scan_for_v1connection_s
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val amberColor = Color(0xFFFFBF00)
private fun ESPConnectionStatus.toColor() = when (this) {
    ESPConnectionStatus.Connecting -> amberColor
    ESPConnectionStatus.Connected -> Color.Green
    ESPConnectionStatus.ConnectionLost -> Color.Red
    ESPConnectionStatus.Disconnecting -> Color.Red
    ESPConnectionStatus.Disconnected -> Color.Red
    ESPConnectionStatus.ConnectionFailed -> Color.Red
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V1cAppBar(
    connectionStatus: ESPConnectionStatus,
    v1c: V1connection?,
    modifier: Modifier = Modifier,
    onScanClick: (V1cType) -> Unit,
    onConnectClick: (V1connection) -> Unit,
    onDisconnectClick: () -> Unit,
) = TopAppBar(
    title = {
        V1cInfo(
            v1c = v1c,
            onScanClicked = { onScanClick(V1cType.LE) },
            modifier = Modifier.fillMaxWidth(),
        )
    },
    modifier = modifier,
    navigationIcon = {
        ConnectionStatusIndicator(
            statusCode = connectionStatus,
            modifier = Modifier
                .then(
                    if (v1c != null) Modifier.clickable(
                        onClickLabel = stringResource(Res.string.scan_for_v1connection_s),
                        onClick = {
                            when(connectionStatus) {
                                ESPConnectionStatus.Disconnected,
                                ESPConnectionStatus.ConnectionLost -> {
                                    onConnectClick(v1c)
                                }
                                else -> onDisconnectClick()
                            }
                        },
                        indication = null,
                        interactionSource = null,
                    )
                    else Modifier
                )
                .padding(8.dp),
        )
    },
    actions = {
        IconButton(
            onClick = {
                onScanClick(V1cType.LE)
            },
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_bluetooth_searching),
                contentDescription = stringResource(Res.string.scan_for_v1c),
                tint = Color.Blue,
            )
        }
    },
    scrollBehavior = null,
)

@Composable
private fun V1cInfo(
    v1c: V1connection?,
    onScanClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = v1c,
        modifier = modifier,
    ) {
        if (it == null) {
            Text(
                text = stringResource(Res.string.no_device_selected),
                modifier = Modifier.clickable(
                    onClickLabel = stringResource(Res.string.scan_for_v1connection_s),
                    onClick = onScanClicked,
                    indication = null,
                    interactionSource = null,
                ),
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
        } else {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = it.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = it.id,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}


@Preview
@Composable
private fun ConnectionStatusIndicator(
    modifier: Modifier = Modifier,
    statusCode: ESPConnectionStatus = ESPConnectionStatus.Disconnected,
) {
    val indicatorColor by animateColorAsState(
        targetValue = statusCode.toColor(),
        label = "indicator color"
    )

    Box(
        modifier = Modifier
            .then(modifier)
            .background(
                color = indicatorColor,
                shape = CircleShape,
            )
            .requiredSize(25.dp)
    )
}

@Preview
@Composable
private fun V1cAppBarPreview() {
    Valentine1Theme {
        V1cAppBar(
            connectionStatus = ESPConnectionStatus.Connecting,
            v1c = V1connection.Demo("demo"),
            modifier = Modifier
                .fillMaxWidth(),
            onScanClick = {},
            onConnectClick = {},
            onDisconnectClick = {},
        )
    }
}