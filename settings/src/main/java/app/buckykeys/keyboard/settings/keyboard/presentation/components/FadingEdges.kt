package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

@Suppress("HardcodedColor")
internal fun Modifier.horizontalFadingEdges(scroll: ScrollState, fadeWidth: Dp): Modifier =
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        val fadeStart = scroll.value > 0
        val fadeEnd = scroll.value < scroll.maxValue
        if (!fadeStart && !fadeEnd) return@drawWithContent
        val fraction = (fadeWidth.toPx() / size.width).coerceAtMost(HALF)
        drawRect(
            brush =
                Brush.horizontalGradient(
                    0f to if (fadeStart) Color.Transparent else Color.Black,
                    (if (fadeStart) fraction else 0f) to Color.Black,
                    (if (fadeEnd) 1f - fraction else 1f) to Color.Black,
                    1f to if (fadeEnd) Color.Transparent else Color.Black,
                ),
            blendMode = BlendMode.DstIn,
        )
    }

private const val HALF = 0.5f
