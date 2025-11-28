package io.github.developrofthings.helloV1.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.developrofthings.kespl.utilities.extensions.primitive.get
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SevenSegment(
    segments: Byte,
    modifier: Modifier = Modifier,
    activeColor: Color = litBarColor,
    inactiveColor: Color = unlitBarColor,
    dpSize: Dp = 9.5.dp,
    segmentWidth: Dp = 8.dp,
    segmentsSpace: Dp = 1.5.dp,
) {
    var segmentPaths = remember { listOf<Path>() }
    var decimalPoint = remember { Offset.Zero }
    Canvas(
        // This modifier ensures that the view is displayed in a correct ratio of 2:1
        modifier = modifier
            .padding(end = 11.dp)
            .aspectRatio(
                ratio = 0.5f,
                matchHeightConstraintsFirst = true,
            )
    ) {
        val dpRadius = dpSize.toPx() * 0.5F
        if (segmentPaths.isEmpty()) {
            segmentPaths = getSegmentPaths(
                segmentWidth = segmentWidth,
                segmentsSpace = segmentsSpace,
            )
            decimalPoint = Offset(
                x = size.width + dpRadius + segmentsSpace.toPx(),
                y = size.height - dpRadius,
            )
        }


        // Draw the segments
        segmentPaths.forEachIndexed { i, path ->

            drawPath(
                path = path,
                color = if (segments[i]) activeColor else inactiveColor,
            )
        }
        

        drawCircle(
            color =  if (segments[7]) activeColor else inactiveColor,
            radius = dpRadius,
            center = decimalPoint,
        )
    }
}

private fun DrawScope.getSegmentPaths(
    segmentWidth: Dp,
    segmentsSpace: Dp,
): List<Path> {
    val halfViewHeight = (size.height / 2)
    val halfWidth = (segmentWidth.toPx() / 2)

    val rightEdge = (size.width - halfWidth)
    val bottomEdge = (size.height - halfWidth)
    val spacing = segmentsSpace.toPx()

    return buildList {
        // Seg A
        add(
            createSegmentPath(
                isVertical = false,
                startX = (halfWidth + spacing),
                endX = (rightEdge - spacing),
                startY = halfWidth,
                endY = halfWidth,
                halfWidth = halfWidth
            )
        )
        // Seg B
        add(
            createSegmentPath(
                isVertical = true,
                startX = rightEdge,
                endX = rightEdge,
                startY = (halfWidth + spacing),
                endY = (halfViewHeight - spacing),
                halfWidth = halfWidth
            )
        )
        // Seg C
        add(
            createSegmentPath(
                isVertical = true,
                startX = rightEdge,
                endX = rightEdge,
                startY = (halfViewHeight + spacing),
                endY = (bottomEdge - spacing),
                halfWidth = halfWidth
            )
        )
        // Seg D
        add(
            createSegmentPath(
                isVertical = false,
                startX = (halfWidth + spacing),
                endX = (rightEdge - spacing),
                startY = bottomEdge,
                endY = bottomEdge,
                halfWidth = halfWidth
            )
        )
        // Seg E
        add(
            createSegmentPath(
                isVertical = true,
                startX = halfWidth,
                endX = halfWidth,
                startY = (halfViewHeight + spacing),
                endY = (bottomEdge - spacing),
                halfWidth = halfWidth
            )
        )
        // Seg F
        add(
            createSegmentPath(
                isVertical = true,
                startX = halfWidth,
                endX = halfWidth,
                startY = (halfWidth + spacing),
                endY = (halfViewHeight - spacing),
                halfWidth = halfWidth
            )
        )
        // Seg G
        add(
            createSegmentPath(
                isVertical = false,
                startX = (halfWidth + spacing),
                endX = (rightEdge - spacing),
                startY = halfViewHeight,
                endY = halfViewHeight,
                halfWidth = halfWidth
            )
        )
    }
}

private fun createSegmentPath(
    isVertical: Boolean,
    startX: Float,
    endX: Float,
    startY: Float,
    endY: Float,
    halfWidth: Float,
): Path = Path().apply {
    // Move the cursor to the start of the segment
    moveTo(startX, startY)
    if (isVertical) {
        // Drawing vertical segment path
        lineTo(startX + halfWidth, startY + halfWidth) // 1
        lineTo(startX + halfWidth, endY - halfWidth) // 2
        lineTo(endX, endY)
        lineTo(startX - halfWidth, endY - halfWidth) // 3
        lineTo(startX - halfWidth, startY + halfWidth) // 4
    } else {
        // Drawing horizontal segment path
        lineTo(startX + halfWidth, startY - halfWidth) // 1
        lineTo(endX - halfWidth, startY - halfWidth) // 2
        lineTo(endX, endY)
        lineTo(endX - halfWidth, startY + halfWidth) // 3
        lineTo(startX + halfWidth, startY + halfWidth) // 4
    }
    // Close the path
    close()
}

@Preview
@Composable
fun SevenSegmentPreview() {
    SevenSegment(
        segments = (0b1111_1111).toByte(),
        modifier = Modifier.height(100.dp),
    )
}