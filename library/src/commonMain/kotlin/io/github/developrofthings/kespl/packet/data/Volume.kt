package io.github.developrofthings.kespl.packet.data

import io.github.developrofthings.kespl.PAYLOAD_START_IDX
import io.github.developrofthings.kespl.emptyByte
import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.utilities.extensions.primitive.set

typealias ResponseCurrentVolume = ESPPacket
typealias ResponseAllVolume = ESPPacket

fun ByteArray.currentVolume(): V1Volume = V1Volume(
    mainVolume = this[PAYLOAD_START_IDX + MAIN_VOLUME_INDEX].toInt(),
    mutedVolume = this[PAYLOAD_START_IDX + MUTED_VOLUME_INDEX].toInt(),
)

fun ByteArray.allVolumes(): V1Volumes = V1Volumes(
    currentVolume = V1Volume(
        mainVolume = this[PAYLOAD_START_IDX + MAIN_VOLUME_INDEX].toInt(),
        mutedVolume = this[PAYLOAD_START_IDX + MUTED_VOLUME_INDEX].toInt(),
    ),
    savedVolume = V1Volume(
        mainVolume = this[PAYLOAD_START_IDX + SAVED_MAIN_VOLUME_INDEX].toInt(),
        mutedVolume = this[PAYLOAD_START_IDX + SAVED_MUTED_VOLUME_INDEX].toInt(),
    ),
)

fun ResponseCurrentVolume.currentVolume(): V1Volume = this.bytes.currentVolume()

fun ResponseAllVolume.allVolumes(): V1Volumes = this.bytes.allVolumes()

/**
 * Current volume levels of the Valentine One's main and muted audio.
 *
 * @since V4.1026
 */
data class V1Volume(
    /**
     * The volume level of the Valentine One's audio is not muted.
     * Valid values are 0-9
     */
    val mainVolume: Int,
    /**
     * The volume level of the Valentine One's audio when muted.
     * Valid values are 0-9
     *
     */
    val mutedVolume: Int,
)

fun V1Volume.toPayload(
    provideUserFeedback: Boolean,
    skipFeedbackWhenNoChange: Boolean,
    saveVolume: Boolean,
): ByteArray = ByteArray(3).apply {
    this[MAIN_VOLUME_INDEX] = (mainVolume and 0xFF).toByte()
    this[MUTED_VOLUME_INDEX] = (mutedVolume and 0xFF).toByte()
    this[2] = emptyByte.set(
        index = PROVIDE_USER_FEEDBACK_BIT_INDEX,
        value = provideUserFeedback,
    ).set(
        index = PROVIDE_SKIP_FEEDBACK_NO_CHANGE_BIT_INDEX,
        value = skipFeedbackWhenNoChange,
    ).set(
        index = PROVIDE_SAVE_VOLUME_BIT_INDEX,
        value = saveVolume,
    )
}
private const val MAIN_VOLUME_INDEX: Int = 0
private const val MUTED_VOLUME_INDEX: Int = 1
private const val SAVED_MAIN_VOLUME_INDEX: Int = 2
private const val SAVED_MUTED_VOLUME_INDEX: Int = 3
private const val PROVIDE_USER_FEEDBACK_BIT_INDEX: Int = 0
private const val PROVIDE_SKIP_FEEDBACK_NO_CHANGE_BIT_INDEX: Int = 1
private const val PROVIDE_SAVE_VOLUME_BIT_INDEX: Int = 2

/**
 * The current and saved volume levels of the Valentine One's main and muted audio.
 */
data class V1Volumes(
    /**
     * Current volume levels of the Valentine One's main and muted audio. This value may differ from
     * [currentVolume] based on the values used when calling calling [IESPClient.writeVolume].
     */
    val currentVolume: V1Volume,
    /**
     * The "saved" volume levels of the Valentine One's main and muted audio. This value may differ
     * from [currentVolume] based on the values used when calling calling [IESPClient.writeVolume].
     */
    val savedVolume: V1Volume,
)