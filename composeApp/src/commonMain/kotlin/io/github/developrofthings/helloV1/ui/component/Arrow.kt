package io.github.developrofthings.helloV1.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.kespl.packet.data.displayData.BandArrowIndicator
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Arrow(
    lit: Boolean,
    modifier: Modifier = Modifier,
    reverse: Boolean = false,
    aspectRatio: Float = FRONT_ARROW_ASPECT_RATIO,
) {
    val color by animateColorAsState(targetValue = if (lit) litBarColor else unlitBarColor)
    Canvas(
        modifier = modifier
            .aspectRatio(ratio = aspectRatio)
            .then(if (reverse) Modifier.rotate(180F) else Modifier)
    ) {
        drawPath(
            path = Path()
                .apply {
                    moveTo(
                        x = center.x,
                        y = 0F,
                    )

                    lineTo(
                        x = size.width,
                        y = size.height,
                    )

                    lineTo(
                        x = 0F,
                        y = size.height,
                    )

                    close()
                },
            color = color,
        )
    }
}

@Composable
fun UpArrow(
    lit: Boolean,
    modifier: Modifier = Modifier,
) = Arrow(
    lit = lit,
    modifier = modifier,
    reverse = false,
    aspectRatio = FRONT_ARROW_ASPECT_RATIO,
)

@Composable
fun SideArrow(
    lit: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(targetValue = if (lit) litBarColor else unlitBarColor)
    Canvas(
        modifier = modifier
            .aspectRatio(ratio = SIDE_ARROW_ASPECT_RATIO)
    ) {
        val tipLength = size.width * 0.15F
        val stickHeight = size.height * .62F
        drawPath(
            path = Path()
                .apply {
                    moveTo(
                        x = center.x,
                        y = (size.height - stickHeight) / 2F,
                    )

                    lineTo(
                        x = size.width - tipLength,
                        y = (size.height - stickHeight) / 2F,
                    )

                    lineTo(
                        x = size.width - tipLength,
                        y = 0F,
                    )

                    lineTo(
                        x = size.width,
                        y = center.y,
                    )

                    lineTo(
                        x = size.width - tipLength,
                        y = size.height,
                    )

                    lineTo(
                        x = size.width - tipLength,
                        y = (size.height + stickHeight) / 2F,
                    )

                    lineTo(
                        x = tipLength,
                        y = (size.height + stickHeight) / 2F,
                    )

                    lineTo(
                        x = tipLength,
                        y = size.height,
                    )

                    lineTo(
                        x = 0F,
                        y = center.y,
                    )

                    lineTo(
                        x = tipLength,
                        y = 0F,
                    )


                    lineTo(
                        x = tipLength,
                        y = (size.height - stickHeight) / 2F,
                    )


                    close()
                },
            color = color,
        )
    }
}

@Composable
fun RearArrow(
    lit: Boolean,
    modifier: Modifier = Modifier,
) = Arrow(
    lit = lit,
    modifier = modifier,
    reverse = true,
    aspectRatio = REAR_ARROW_ASPECT_RATIO,
)

@Composable
fun ArrowDisplay(
    arrows: BandArrowIndicator,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
) = Layout(
    content = {
        UpArrow(lit = arrows.front)
        SideArrow(lit = arrows.side)
        RearArrow(lit = arrows.rear)
    },
    modifier = modifier,
) { (front, side, rear), constraints: Constraints ->

    val spacingPx = spacing.roundToPx()
    // Attempt to be as tall as we can be given the available height,
    // if the desired height would result in an arrow width that is too wide for the display, we
    // clamp to the width instead
    val availableHeight = constraints.resolveHeight(fallbackHeight = 100.dp.roundToPx())
    val availableWidth = constraints.resolveWidth(fallbackWidth = 100.dp.roundToPx())
    val spacingAdjustedHeight = availableHeight - (spacingPx * 2)

    val frontPlaceable = measureAndPlaceArrow(
        arrow = front,
        availableHeight = spacingAdjustedHeight,
        availableWidth = availableWidth,
        arrowToHeightRatio = FRONT_ARROW_DISPLAY_HEIGHT_RATIO,
        arrowToWidthRation = FRONT_ARROW_ASPECT_RATIO,
    )

    val sidePlaceable = measureAndPlaceArrow(
        arrow = side,
        availableHeight = spacingAdjustedHeight,
        availableWidth = availableWidth,
        arrowToHeightRatio = SIDE_ARROW_DISPLAY_HEIGHT_RATIO,
        arrowToWidthRation = SIDE_ARROW_ASPECT_RATIO,
    )

    val rearPlaceable = measureAndPlaceArrow(
        arrow = rear,
        availableHeight = spacingAdjustedHeight,
        availableWidth = availableWidth,
        arrowToHeightRatio = REAR_ARROW_DISPLAY_HEIGHT_RATIO,
        arrowToWidthRation = REAR_ARROW_ASPECT_RATIO,
    )

    val layoutWidth = calculateArrowDisplayWidth(
        front = frontPlaceable,
        side = sidePlaceable,
        rear = rearPlaceable
    )

    val layoutHeight = calculateArrowDisplayHeight(
        front = frontPlaceable,
        side = sidePlaceable,
        rear = rearPlaceable,
        spacing = spacingPx,
    )

    layout(width = layoutWidth, height = layoutHeight) {
        var yPosition = 0
        frontPlaceable.place(x = 0, y = yPosition)
        yPosition += frontPlaceable.height + spacingPx

        sidePlaceable.place(x = 0, y = yPosition)
        yPosition += sidePlaceable.height + spacingPx

        rearPlaceable.place(x = 0, y = yPosition)
    }
}

@Preview
@Composable
private fun UnlitArrowsPreview() {
    ArrowDisplay(
        arrows = BandArrowIndicator(0x00),
        modifier = Modifier,
    )
}

@Preview
@Composable
private fun FrontArrowLitPreview() {
    ArrowDisplay(
        arrows = BandArrowIndicator(0x20),
    )
}

@Preview
@Composable
private fun SideArrowLitPreview() {
    ArrowDisplay(
        arrows = BandArrowIndicator(0x40),
    )
}

@Preview
@Composable
private fun RearArrowLitPreview() {
    ArrowDisplay(
        arrows = BandArrowIndicator(0x80.toByte()),
    )
}

@Preview
@Composable
private fun AllArrowsLitPreview() {
    ArrowDisplay(
        arrows = BandArrowIndicator(0xE0.toByte()),
    )
}

private fun measureAndPlaceArrow(
    arrow: Measurable,
    availableHeight: Int,
    availableWidth: Int,
    arrowToHeightRatio: Float,
    arrowToWidthRation: Float,
): Placeable {
    val arrowHeight = (availableHeight * arrowToHeightRatio).toInt()
    val arrowWidth = (arrowHeight * arrowToWidthRation).toInt()
    return arrow.measure(
        Constraints(
            maxWidth = minOf(availableWidth, arrowWidth),
            // If the desired width exceeds the available space, we want to calc a new height
            maxHeight = if (arrowWidth > availableWidth) {
                (availableWidth * (1 / arrowToWidthRation)).toInt()
            } else arrowHeight,
        )
    )
}


private fun Constraints.resolveHeight(fallbackHeight: Int): Int = when {
    !hasBoundedHeight -> fallbackHeight
    else -> {
        // We are bounded..
        if (hasFixedHeight) maxHeight
        else maxHeight
    }
}

private fun Constraints.resolveWidth(fallbackWidth: Int): Int = when {
    !hasBoundedWidth -> fallbackWidth
    else -> {
        // We are bounded..
        if (hasFixedWidth) maxWidth
        else maxWidth
    }
}

private fun calculateArrowDisplayWidth(
    front: Placeable,
    side: Placeable,
    rear: Placeable,
): Int = maxOf(
    front.width,
    side.width,
    rear.width,
)

private fun calculateArrowDisplayHeight(
    front: Placeable,
    side: Placeable,
    rear: Placeable,
    spacing: Int,
): Int = (spacing * 2) + front.height + side.height + rear.height


private const val FRONT_ARROW_ASPECT_RATIO: Float = 55f / 28F
private const val SIDE_ARROW_ASPECT_RATIO: Float = 55f / 8f
private const val REAR_ARROW_ASPECT_RATIO: Float = 55f / 14f

private const val FRONT_ARROW_DISPLAY_HEIGHT_RATIO: Float = 228F / 423F
private const val SIDE_ARROW_DISPLAY_HEIGHT_RATIO: Float = 65F / 423F
private const val REAR_ARROW_DISPLAY_HEIGHT_RATIO: Float = 114F / 423F