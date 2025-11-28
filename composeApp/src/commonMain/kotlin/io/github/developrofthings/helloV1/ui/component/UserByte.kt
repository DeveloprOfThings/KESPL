package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.developrofthings.helloV1.ui.theme.Valentine1Theme
import io.github.developrofthings.kespl.utilities.extensions.primitive.get
import io.github.developrofthings.kespl.utilities.extensions.primitive.isBitSet
import io.github.developrofthings.kespl.utilities.extensions.primitive.set
import hellov1.composeapp.generated.resources.Res
import hellov1.composeapp.generated.resources.gen1_user_byte_0
import hellov1.composeapp.generated.resources.gen1_user_byte_1
import hellov1.composeapp.generated.resources.gen1_user_byte_2
import hellov1.composeapp.generated.resources.gen2_user_byte_0
import hellov1.composeapp.generated.resources.gen2_user_byte_1
import hellov1.composeapp.generated.resources.gen2_user_byte_2
import hellov1.composeapp.generated.resources.gen2_user_byte_3
import hellov1.composeapp.generated.resources.user_byte_undefined
import hellov1.composeapp.generated.resources.write_to_v1
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun rememberUserByteState(
    userByte: Byte,
    index: Int = 0,
): UserByteState =
    rememberSaveable(
        index,
        userByte,
        saver = UserByteState.Saver
    ) { UserByteState(index = index, userByte = userByte) }

class UserByteState(
    val index: Int,
    userByte: Byte,
) {
    var userByte by mutableStateOf(userByte)
        private set

    fun update(value: Byte) {
        userByte = value
    }

    fun update(index: Int, changed: Boolean) {
        userByte = userByte.set(index, changed)
    }

    operator fun get(index: Int): Boolean = userByte[index]

    object Saver : androidx.compose.runtime.saveable.Saver<UserByteState, Any> {
        override fun restore(value: Any): UserByteState? = (value as List<*>).let {
            UserByteState(
                index = it[0] as Int,
                userByte = it[1] as Byte,
            )
        }


        override fun SaverScope.save(value: UserByteState): Any? = listOf(
            value.index,
            value.userByte,
        )
    }
}

@Composable
private fun UserBitRow(
    bit: Boolean,
    label: String,
    onBitChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier
        .padding(horizontal = 8.dp)
        .clickable(
            enabled = true,
            onClickLabel = null,
            onClick = { onBitChanged(!bit) },
            role = Role.Button,
            indication = null,
            interactionSource = null,
        ),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = label,
        modifier = Modifier.weight(1F),
        maxLines = 2,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        UpArrow(
            lit = bit,
            modifier = Modifier
                .width(55.dp),
        )

        RearArrow(
            lit = !bit,
            modifier = Modifier
                .width(55.dp)
                .height(14.dp),
        )
    }
}

@Composable
fun UserByte(
    userByteState: UserByteState,
    isGen2: Boolean,
    modifier: Modifier = Modifier,
    onBitChanged: (Int, Boolean) -> Unit,
    onCommitClicked: () -> Unit,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {

    Text(
        text = "User Byte #${userByteState.index}",
        style = MaterialTheme.typography.titleMedium,
    )

    val labels = if (!isGen2) when (userByteState.index) {
        0 -> stringArrayResource(Res.array.gen1_user_byte_0)
        1 -> stringArrayResource(Res.array.gen1_user_byte_1)
        2 -> stringArrayResource(Res.array.gen1_user_byte_2)
        else -> stringArrayResource(Res.array.user_byte_undefined)
    }
    else when (userByteState.index) {
        0 -> stringArrayResource(Res.array.gen2_user_byte_0)
        1 -> stringArrayResource(Res.array.gen2_user_byte_1)
        2 -> stringArrayResource(Res.array.gen2_user_byte_2)
        3 -> stringArrayResource(Res.array.gen2_user_byte_3)
        else -> stringArrayResource(Res.array.user_byte_undefined)
    }
    labels.forEachIndexed { i, label ->
        UserBitRow(
            bit = userByteState.userByte.isBitSet(i),
            label = label,
            onBitChanged = { onBitChanged(i, it) },
        )
    }

    Button(onClick = onCommitClicked) {
        Text(text = stringResource(Res.string.write_to_v1),)
    }
}

@Preview
@Composable
private fun UserBitRowPreview() {
    Valentine1Theme(darkTheme = true) {
        Surface {
            UserBitRow(
                bit = true,
                label = "Test Label",
                onBitChanged = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun UserBytePreview() {
    Valentine1Theme(darkTheme = true) {
        Surface {
            val userByteState = rememberUserByteState(0xFF.toByte())
            UserByte(
                userByteState = userByteState,
                isGen2 = true,
                modifier = Modifier.fillMaxWidth(),
                onBitChanged = { i, changed ->
                    userByteState.update(i, changed)
                },
                onCommitClicked = {},
            )
        }
    }
}
