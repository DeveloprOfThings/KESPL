package io.github.developrofthings.helloV1.ui.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.developrofthings.helloV1.service.IESPService
import io.github.developrofthings.kespl.V1CapabilityInfo
import io.github.developrofthings.kespl.packet.data.displayData.BandArrowIndicator
import io.github.developrofthings.kespl.packet.data.displayData.BogeyCounter7Segment
import io.github.developrofthings.kespl.packet.data.displayData.DisplayData
import io.github.developrofthings.kespl.packet.data.displayData.SignalStrengthBarGraph
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InfDisplayViewModel(espService: IESPService) : ViewModel() {

    val uiState = combine(
        flow = espService
            .v1CapabilityInfo,
        flow2 = espService
            .displayData,
        transform = ::InfDisplayUiState,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = InfDisplayUiState.DEFAULT,
        )
}

data class InfDisplayUiState(
    val hasMuteAndBTIndicator: Boolean,
    val muteImg1: Boolean,
    val muteImg2: Boolean,
    val bluetoothImg1: Boolean,
    val bluetoothImg2: Boolean,
    val bogeyCounterImg1: BogeyCounter7Segment,
    val bogeyCounterImg2: BogeyCounter7Segment,
    val signalStrengthBarGraph: SignalStrengthBarGraph,
    val bandArrowIndicatorImg1: BandArrowIndicator,
    val bandArrowIndicatorImg2: BandArrowIndicator,
) {

    constructor(
        v1CapabilityInfo: V1CapabilityInfo,
        displayData: DisplayData
    ) : this(
        hasMuteAndBTIndicator = v1CapabilityInfo.hasInfDisplayDataMuteAndBtIndicator,
        muteImg1 = displayData.muteIndicatorImage1,
        muteImg2 = displayData.muteIndicatorImage2,
        bluetoothImg1 = displayData.btIndicatorImage1,
        bluetoothImg2 = displayData.btIndicatorImage2,
        bogeyCounterImg1 = displayData.bogeyCounter7SegmentImage1,
        bogeyCounterImg2 = displayData.bogeyCounter7SegmentImage2,
        signalStrengthBarGraph = displayData.signalStrengthBarGraphImage,
        bandArrowIndicatorImg1 = displayData.bandArrowIndicatorImage1,
        bandArrowIndicatorImg2 = displayData.bandArrowIndicatorImage2,
    )

    companion object {
        val DEFAULT: InfDisplayUiState = InfDisplayUiState(
            hasMuteAndBTIndicator = false,
            muteImg1 = false,
            muteImg2 = false,
            bluetoothImg1 = false,
            bluetoothImg2 = false,
            bogeyCounterImg1 = BogeyCounter7Segment(0x00),
            bogeyCounterImg2 = BogeyCounter7Segment(0x00),
            signalStrengthBarGraph = SignalStrengthBarGraph(0x0),
            bandArrowIndicatorImg1 = BandArrowIndicator(0x00),
            bandArrowIndicatorImg2 = BandArrowIndicator(0x00),
        )
    }
}