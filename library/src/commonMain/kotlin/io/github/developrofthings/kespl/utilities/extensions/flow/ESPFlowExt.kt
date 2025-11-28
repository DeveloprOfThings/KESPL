package io.github.developrofthings.kespl.utilities.extensions.flow

import io.github.developrofthings.kespl.packet.ESPPacket
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.displayData
import io.github.developrofthings.kespl.packet.isInfDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

fun Flow<ESPPacket>.filterIsDisplayData(): Flow<DisplayData> = this
    .filter { it.isInfDisplayData }
    .map { it.displayData() }
