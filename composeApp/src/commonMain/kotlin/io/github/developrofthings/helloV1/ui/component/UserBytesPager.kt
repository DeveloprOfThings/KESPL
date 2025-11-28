package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun rememberUserBytesPagerState(
    userBytes: ByteArray,
    pagerState: PagerState = rememberPagerState { userBytes.size },
): UserBytesPagerState =
    rememberSaveable(
        userBytes,
        pagerState,
        saver = UserBytesPagerState.Saver(pagerState)
    ) {
        UserBytesPagerState(
            pagerState = pagerState,
            userBytesStates = userBytes.mapIndexed { i, it ->
                UserByteState(
                    index = i,
                    userByte = it,
                )
            }
        )
    }.apply {
        update(userBytes)
    }

class UserBytesPagerState(
    val pagerState: PagerState,
    val userBytesStates: List<UserByteState>,
) {
    operator fun get(index: Int): UserByteState = userBytesStates[index]

    val userBytes: Flow<ByteArray> = combine(
        flows = userBytesStates.map { snapshotFlow { it.userByte } },
        transform = { it.toByteArray() },
    )

    fun update(userBytes: ByteArray) {
        userBytesStates.forEachIndexed { i, byteState -> byteState.update(userBytes[i]) }
    }

    val currentUserBytes: ByteArray get() = userBytesStates.map { it.userByte }.toByteArray()

    class Saver(val pagerState: PagerState) :
        androidx.compose.runtime.saveable.Saver<UserBytesPagerState, Any> {
        override fun restore(value: Any): UserBytesPagerState? =
            UserBytesPagerState(
                pagerState = pagerState,
                userBytesStates = (value as List<*>).map {
                    with(UserByteState.Saver) {
                        restore(it as Any)
                    } as UserByteState
                }
            )

        override fun SaverScope.save(value: UserBytesPagerState): Any? =
            value.userBytesStates.map { state ->
                with(UserByteState.Saver) {
                    save(state)
                }
            }
    }
}

@Composable
fun UserBytesPager(
    isGen2: Boolean,
    userBytesState: UserBytesPagerState,
    modifier: Modifier = Modifier,
    onCommitClicked: (ByteArray) -> Unit,
) = HorizontalPager(
    state = userBytesState.pagerState,
    modifier = modifier,
    contentPadding = PaddingValues(
        start = 24.dp,
        end = 24.dp,
    ),
    pageSpacing = 24.dp,
) {
    Surface(
        modifier = Modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        UserByte(
            userByteState = userBytesState.userBytesStates[it],
            isGen2 = isGen2 ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp,
                ),
            onBitChanged = { i, changed ->
                userBytesState.userBytesStates[it].update(index = i, changed)
            },
            onCommitClicked = { onCommitClicked(userBytesState.currentUserBytes) },
        )
    }
}

@Preview
@Composable
private fun UserBytesPagerPreview() {
    Valentine1Theme(
        darkTheme = true
    ) {
        UserBytesPager(
            isGen2 = false,
            userBytesState = rememberUserBytesPagerState(
                userBytes = byteArrayOf(
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte()
                ),
            ),
            onCommitClicked = {},
        )

    }
}