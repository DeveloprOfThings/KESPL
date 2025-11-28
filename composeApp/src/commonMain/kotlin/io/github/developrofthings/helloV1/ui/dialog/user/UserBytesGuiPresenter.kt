package io.github.developrofthings.helloV1.ui.dialog.user

import io.github.developrofthings.helloV1.data.repository.ESPDataLogRepository
import io.github.developrofthings.helloV1.service.ESPService
import io.github.developrofthings.kespl.ESPDevice
import io.github.developrofthings.kespl.ESPResponse
import io.github.developrofthings.kespl.packet.data.user.defaultUserBytes
import io.github.developrofthings.kespl.packet.isUserBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserBytesGuiPresenter(
    userBytes: ByteArray,
    val targetDevice: ESPDevice,
    private val scope: CoroutineScope,
    private val espService: ESPService,
    private val espDataLogRepository: ESPDataLogRepository,
) {
    private val _userBytes = espService
        .espData
        .filter { it.isUserBytes }
        .map {
            ByteArray(6).apply {
                it.copyInto(
                    destination = this,
                    length = this@apply.size,
                )
            }
        }
        .stateIn(
            scope,
            started = SharingStarted.Lazily,
            initialValue = userBytes
        )

    val uiState = combine(
        flow = espService.v1CapabilityInfo.map { it.isGen2 },
        flow2 = _userBytes,
        transform = ::UserBytesUiState,
    ).stateIn(
        scope,
        started = SharingStarted.Lazily,
        initialValue = UserBytesUiState.DEFAULT,
    )

    fun onWriteUserBytes(bytes: ByteArray) {
        scope.launch {
            espDataLogRepository.addLog("Writing user bytes: ${bytes.toHexString()}")
            espService.writeUserBytes(
                device = targetDevice,
                userBytes = bytes
            ).also { resp ->
                when (resp) {
                    is ESPResponse.Failure -> espDataLogRepository.addLog("Failed to write V1 user bytes: ${resp.data}")
                    is ESPResponse.Success -> espDataLogRepository.addLog("Successfully wrote V1 user bytes: ${resp.data}")
                }
            }
        }
    }
}

data class UserBytesUiState(
    val isGen2: Boolean,
    val userBytes: ByteArray,
) {
    override fun hashCode(): Int {
        var result = isGen2.hashCode()
        result = 31 * result + userBytes.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UserBytesUiState

        if (isGen2 != other.isGen2) return false
        if (!userBytes.contentEquals(other.userBytes)) return false

        return true
    }

    companion object {
        val DEFAULT: UserBytesUiState = UserBytesUiState(
            isGen2 = false,
            userBytes = defaultUserBytes,

            )
    }
}