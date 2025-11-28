package io.github.developrofthings.kespl.utilities

import io.github.developrofthings.kespl.fullByte
import io.github.developrofthings.kespl.packet.data.sweep.SweepDefinition
import io.github.developrofthings.kespl.packet.data.sweep.SweepSection
import io.github.developrofthings.kespl.utilities.V1VersionInfo.SweepInfo.V3_8920_CustomSweep
import io.github.developrofthings.kespl.utilities.V1VersionInfo.SweepInfo.V3_8952_CustomSweep
import io.github.developrofthings.kespl.utilities.V1VersionInfo.SweepInfo.V4_1000_CustomFrequencies
import io.github.developrofthings.kespl.utilities.V1VersionInfo.UserSettingsInfo.V3_8920_UserBytes
import io.github.developrofthings.kespl.utilities.V1VersionInfo.UserSettingsInfo.V4_1000_UserBytes

private const val V3_8920_SWEEP_SECTION_1_LOWER_EDGE = 33383
private const val V3_8920_SWEEP_SECTION_1_UPPER_EDGE = 34770
private const val V3_8920_SWEEP_SECTION_2_LOWER_EDGE = 34774
private const val V3_8920_SWEEP_SECTION_2_UPPER_EDGE = 36072
private const val V3_8952_SWEEP_SECTION_0_LOWER_EDGE = 33360
private const val V3_8952_SWEEP_SECTION_0_UPPER_EDGE = 36051
private const val V4_1000_K_SWEEP_SECTION_LOWER_EDGE = 23908
private const val V4_1000_KA_SWEEP_SECTION_LOWER_EDGE = 33398
private const val V4_1000_K_SWEEP_SECTION_UPPER_EDGE = 24252
private const val V4_1000_KA_SWEEP_SECTION_UPPER_EDGE = 36002
private const val V3_8920_KA_SWEEP_0_LOWER_EDGE = 33900
private const val V3_8920_KA_SWEEP_0_UPPER_EDGE = 34106
private const val V3_8920_KA_SWEEP_1_LOWER_EDGE = 34180
private const val V3_8920_KA_SWEEP_1_UPPER_EDGE = 34475
private const val V3_8920_KA_SWEEP_2_LOWER_EDGE = 34563
private const val V3_8920_KA_SWEEP_2_UPPER_EDGE = 34652
private const val V3_8920_KA_SWEEP_3_LOWER_EDGE = 35467
private const val V3_8920_KA_SWEEP_3_UPPER_EDGE = 35526
private const val V3_8952_KA_SWEEP_0_LOWER_EDGE = 33905
private const val V3_8952_KA_SWEEP_0_UPPER_EDGE = 34112
private const val V3_8952_KA_SWEEP_1_LOWER_EDGE = 34186
private const val V3_8952_KA_SWEEP_1_UPPER_EDGE = 34480
private const val V3_8952_KA_SWEEP_2_LOWER_EDGE = 34569
private const val V3_8952_KA_SWEEP_2_UPPER_EDGE = 34657
private const val V3_8952_KA_SWEEP_3_LOWER_EDGE = 35462
private const val V3_8952_KA_SWEEP_3_UPPER_EDGE = 35535
private const val V4_1000_DEF_USA_SWEEP_0_LOWER_EDGE = 33400
private const val V4_1000_DEF_USA_SWEEP_1_LOWER_EDGE = 23910
private const val V4_1000_DEF_USA_SWEEP_0_UPPER_EDGE = 36002
private const val V4_1000_DEF_USA_SWEEP_1_UPPER_EDGE = 24250

object V1VersionInfo {

    object SweepInfo {

        val V3_8920_SweepSections: List<SweepSection> = listOf(
            SweepSection(
                indexCount = 0x12,
                lowerEdge = V3_8920_SWEEP_SECTION_1_LOWER_EDGE,
                upperEdge = V3_8920_SWEEP_SECTION_1_UPPER_EDGE,
            ),
            SweepSection(
                indexCount = 0x22,
                lowerEdge = V3_8920_SWEEP_SECTION_2_LOWER_EDGE,
                upperEdge = V3_8920_SWEEP_SECTION_2_UPPER_EDGE,
            ),
        )

        val V3_8952_SweepSections: List<SweepSection> = listOf(
            SweepSection(
                indexCount = 0x11,
                lowerEdge = V3_8952_SWEEP_SECTION_0_LOWER_EDGE,
                upperEdge = V3_8952_SWEEP_SECTION_0_UPPER_EDGE,
            ),
        )

        val V4_1000_SweepSections: List<SweepSection> = listOf(
            SweepSection(
                indexCount = 0x12,
                lowerEdge = V4_1000_K_SWEEP_SECTION_LOWER_EDGE,
                upperEdge = V4_1000_K_SWEEP_SECTION_UPPER_EDGE,
            ),
            SweepSection(
                indexCount = 0x22,
                lowerEdge = V4_1000_KA_SWEEP_SECTION_LOWER_EDGE,
                upperEdge = V4_1000_KA_SWEEP_SECTION_UPPER_EDGE,
            ),
        )

        val V3_8920_CustomSweep: List<SweepDefinition> = listOf(
            SweepDefinition(
                index = 0,
                lowerEdge = V3_8920_KA_SWEEP_0_LOWER_EDGE,
                upperEdge = V3_8920_KA_SWEEP_0_UPPER_EDGE
            ),
            SweepDefinition(
                index = 1,
                lowerEdge = V3_8920_KA_SWEEP_1_LOWER_EDGE,
                upperEdge = V3_8920_KA_SWEEP_1_UPPER_EDGE
            ),
            SweepDefinition(
                index = 2,
                lowerEdge = V3_8920_KA_SWEEP_2_LOWER_EDGE,
                upperEdge = V3_8920_KA_SWEEP_2_UPPER_EDGE
            ),
            SweepDefinition(
                index = 3,
                lowerEdge = V3_8920_KA_SWEEP_3_LOWER_EDGE,
                upperEdge = V3_8920_KA_SWEEP_3_UPPER_EDGE
            ),
            SweepDefinition(index = 4, lowerEdge = 0, upperEdge = 0),
            SweepDefinition(index = 5, lowerEdge = 0, upperEdge = 0),
        )

        val V3_8952_CustomSweep: List<SweepDefinition> = listOf(
            SweepDefinition(
                index = 0,
                lowerEdge = V3_8952_KA_SWEEP_0_LOWER_EDGE,
                upperEdge = V3_8952_KA_SWEEP_0_UPPER_EDGE
            ),
            SweepDefinition(
                index = 1,
                lowerEdge = V3_8952_KA_SWEEP_1_LOWER_EDGE,
                upperEdge = V3_8952_KA_SWEEP_1_UPPER_EDGE
            ),
            SweepDefinition(
                index = 2,
                lowerEdge = V3_8952_KA_SWEEP_2_LOWER_EDGE,
                upperEdge = V3_8952_KA_SWEEP_2_UPPER_EDGE
            ),
            SweepDefinition(
                index = 3,
                lowerEdge = V3_8952_KA_SWEEP_3_LOWER_EDGE,
                upperEdge = V3_8952_KA_SWEEP_3_UPPER_EDGE
            ),
            SweepDefinition(index = 4, lowerEdge = 0, upperEdge = 0),
            SweepDefinition(index = 5, lowerEdge = 0, upperEdge = 0),
        )

        val V4_1000_CustomFrequencies: List<SweepDefinition> = listOf(
            SweepDefinition(
                index = 0,
                lowerEdge = V4_1000_DEF_USA_SWEEP_0_LOWER_EDGE,
                upperEdge = V4_1000_DEF_USA_SWEEP_0_UPPER_EDGE
            ),
            SweepDefinition(
                index = 1,
                lowerEdge = V4_1000_DEF_USA_SWEEP_1_LOWER_EDGE,
                upperEdge = V4_1000_DEF_USA_SWEEP_1_UPPER_EDGE
            ),
            SweepDefinition(index = 2, lowerEdge = 0, upperEdge = 0),
            SweepDefinition(index = 3, lowerEdge = 0, upperEdge = 0),
            SweepDefinition(index = 4, lowerEdge = 0, upperEdge = 0),
            SweepDefinition(index = 5, lowerEdge = 0, upperEdge = 0),
        )
    }

    fun defaultSweepForVersion(version: Double): List<SweepDefinition> = when {
        version >= V1_GEN_2_PLATFORM_BASELINE_VERSION -> V4_1000_CustomFrequencies
        version >= 3.8952 -> V3_8952_CustomSweep
        else -> V3_8920_CustomSweep
    }

    object UserSettingsInfo {

        val V3_8920_UserBytes: ByteArray = byteArrayOf(
            fullByte,
            fullByte,
            // Traffic monitor filter off by default
            0xF7.toByte(),
            fullByte,
            fullByte,
            fullByte,
        )

        val V4_1000_UserBytes: ByteArray = byteArrayOf(
            fullByte,
            fullByte,
            fullByte,
            fullByte,
            fullByte,
            fullByte,
        )
    }

    fun defaultUserBytesForVersion(version: Double): ByteArray = when {
        version >= V1_GEN_2_PLATFORM_BASELINE_VERSION -> V4_1000_UserBytes
        else -> V3_8920_UserBytes
    }
}

/**
 * Baseline version of the V1 Gen2 platform
 */
const val V1_GEN_2_PLATFORM_BASELINE_VERSION = 4.1000

const val V1_GEN2_AUTO_MUTE_SETTING_VERSION = 4.1036
const val V1_GEN2_PHOTO_RADAR_VERSION = 4.1037

/**
 * Max version of the V1 Gen2 platform
 */
const val V1_GEN_2_PLATFORM_MAX_VERSION = 4.9999