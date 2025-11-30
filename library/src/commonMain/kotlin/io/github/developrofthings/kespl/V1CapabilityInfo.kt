package io.github.developrofthings.kespl

import io.github.developrofthings.kespl.packet.data.Version
import io.github.developrofthings.kespl.packet.data.asDouble
import io.github.developrofthings.kespl.utilities.V1_GEN2_AUTO_MUTE_SETTING_VERSION
import io.github.developrofthings.kespl.utilities.V1_GEN2_PHOTO_RADAR_VERSION
import io.github.developrofthings.kespl.utilities.V1_GEN_2_PLATFORM_BASELINE_VERSION

@ConsistentCopyVisibility
data class V1CapabilityInfo private constructor(
    val supportsDefaultSweepRequest: Boolean,
    val supportsReqDefaultSweepDefinitions: Boolean,
    // 4.1018
    val isGen2: Boolean,
    val hasInfDisplayDataMuteAndBtIndicator: Boolean,
    val supportsKAndKaInEuroAndUsa: Boolean,
    // 4.1027
    val supportsVolumeControl: Boolean,
    // 4.1028
    val supportsDoubleTap: Boolean,
    val hasInfDisplayDataLogicMode: Boolean,
    val hasInfDisplayDataMuteInformation: Boolean,
    val hasInfDisplayDataVolume: Boolean,
    // 4.1031
    val supportsFastLaserDetectionUserSetting: Boolean,
    val supportsKaAlwaysPriorityUserSetting: Boolean,
    val unmutingAlertsAffectsLaser: Boolean,
    // 4.1032
    val hasAlertDataJunkBit: Boolean,
    val supportsMainDisplayRequestAux0Byte: Boolean,
    val supportsKaThresholdUserSetting: Boolean,
    // 4.1035
    val supportStartupSequenceUserSetting: Boolean,
    val supportsRestingDisplayUserSetting: Boolean,
    val supportsAbortAudioDelay: Boolean,
    // 4.1036
    val supportsAutoMuteUserSettings: Boolean,
    val supportsDisplayVolumeRequest: Boolean,
    // V4.1037
    val supportsAllVolumesRequest: Boolean,
    val supportsTemporaryVolume: Boolean,
    val supportsDisplayActiveBit: Boolean,
    val supportsPhotoRadar: Boolean,
) {
    constructor(version: Double) : this(
        supportsDefaultSweepRequest = version >= 3.8920,
        supportsReqDefaultSweepDefinitions = version >= 3.8950,
        isGen2 = version >= V1_GEN_2_PLATFORM_BASELINE_VERSION,
        hasInfDisplayDataMuteAndBtIndicator = version >= V1_GEN_2_PLATFORM_BASELINE_VERSION,
        supportsKAndKaInEuroAndUsa = version >= V1_GEN_2_PLATFORM_BASELINE_VERSION,
        supportsVolumeControl = version >= 4.1027,
        supportsDoubleTap = version >= 4.1028,
        hasInfDisplayDataLogicMode = version >= 4.1028,
        hasInfDisplayDataMuteInformation = version >= 4.1028,
        hasInfDisplayDataVolume = version >= 4.1028,
        supportsFastLaserDetectionUserSetting = version >= 4.1031,
        supportsKaAlwaysPriorityUserSetting = version >= 4.1031,
        unmutingAlertsAffectsLaser = version >= 4.1031,
        hasAlertDataJunkBit = version >= 4.1032,
        supportsMainDisplayRequestAux0Byte = version >= 4.1032,
        supportsKaThresholdUserSetting = version >= 4.1032,
        supportStartupSequenceUserSetting = version >= 4.1035,
        supportsRestingDisplayUserSetting = version >= 4.1035,
        supportsAbortAudioDelay = version >= 4.1035,
        supportsAutoMuteUserSettings = version >= V1_GEN2_AUTO_MUTE_SETTING_VERSION,
        supportsDisplayVolumeRequest = version >= V1_GEN2_AUTO_MUTE_SETTING_VERSION,
        supportsAllVolumesRequest = version >= V1_GEN2_PHOTO_RADAR_VERSION,
        supportsTemporaryVolume = version >= V1_GEN2_PHOTO_RADAR_VERSION,
        supportsDisplayActiveBit = version >= V1_GEN2_PHOTO_RADAR_VERSION,
        supportsPhotoRadar = version >= V1_GEN2_PHOTO_RADAR_VERSION,
    )

    constructor(version: Version) : this(
        version = version.takeIf {
            it.contains("V", ignoreCase = true) &&  it.length == 7
        }?.asDouble() ?: 0.0
    )

    companion object {
        val DEFAULT: V1CapabilityInfo = V1CapabilityInfo(0.0)
    }
}