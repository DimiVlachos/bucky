package app.buckykeys.keyboard.settings.core.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Stable
internal class KeepFocusedVisibleState {
    var target: LayoutCoordinates? = null
}

private val LocalKeepFocusedVisible = staticCompositionLocalOf<KeepFocusedVisibleState?> { null }

@Composable
internal fun ProvideKeepFocusedVisible(
    state: KeepFocusedVisibleState,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKeepFocusedVisible provides state, content = content)
}

@Composable
internal fun Modifier.keepVisibleWhenFocused(): Modifier {
    val state = LocalKeepFocusedVisible.current ?: return this
    val holder = remember { arrayOfNulls<LayoutCoordinates>(1) }
    return onGloballyPositioned { holder[0] = it }
        .onFocusChanged { focus ->
            if (focus.isFocused) state.target = holder[0]
            else if (state.target === holder[0]) state.target = null
        }
}

@Composable
internal fun Modifier.scrollFocusedIntoView(
    state: KeepFocusedVisibleState,
    scrollState: ScrollState,
    margin: Dp,
): Modifier {
    val marginPx = with(LocalDensity.current) { margin.toPx() }
    return onGloballyPositioned { viewport ->
        val target = state.target?.takeIf { it.isAttached } ?: return@onGloballyPositioned
        val bottom = viewport.localBoundingBoxOf(target, clipBounds = false).bottom
        val overflow = bottom + marginPx - viewport.size.height
        if (overflow > 0f) scrollState.dispatchRawDelta(overflow)
    }
}
