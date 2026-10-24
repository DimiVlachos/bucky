package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset

internal fun Modifier.extendIntoPadding(start: Dp, end: Dp): Modifier =
    layout { measurable, constraints ->
        val startPx = start.roundToPx()
        val endPx = end.roundToPx()
        val placeable = measurable.measure(constraints.offset(horizontal = startPx + endPx))
        layout((placeable.width - startPx - endPx).coerceAtLeast(0), placeable.height) {
            placeable.place(-startPx, 0)
        }
    }

@Suppress("HardcodedColor")
internal fun Modifier.horizontalFadingEdges(scroll: ScrollState, start: Dp, end: Dp): Modifier =
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        val fadeStart = scroll.value > 0
        val fadeEnd = scroll.value < scroll.maxValue
        if (!fadeStart && !fadeEnd) return@drawWithContent
        val startStop = (start.toPx() / size.width).coerceAtMost(HALF)
        val endStop = 1f - (end.toPx() / size.width).coerceAtMost(HALF)
        drawRect(
            brush =
                Brush.horizontalGradient(
                    0f to if (fadeStart) Color.Transparent else Color.Black,
                    (if (fadeStart) startStop else 0f) to Color.Black,
                    (if (fadeEnd) endStop else 1f) to Color.Black,
                    1f to if (fadeEnd) Color.Transparent else Color.Black,
                ),
            blendMode = BlendMode.DstIn,
        )
    }

private const val HALF = 0.5f
