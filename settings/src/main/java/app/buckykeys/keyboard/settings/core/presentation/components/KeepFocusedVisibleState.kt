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
    internal var viewport: LayoutCoordinates? = null
    internal var scrollState: ScrollState? = null
    internal var marginPx: Float = 0f

    internal fun revealTarget() {
        val target = target?.takeIf { it.isAttached } ?: return
        val viewport = viewport?.takeIf { it.isAttached } ?: return
        val bottom = viewport.localBoundingBoxOf(target, clipBounds = false).bottom
        val overflow = bottom + marginPx - viewport.size.height
        if (overflow > 0f) scrollState?.dispatchRawDelta(overflow)
    }
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
    val lastHeight = remember { intArrayOf(UNMEASURED) }
    return onGloballyPositioned { coordinates ->
        holder[0] = coordinates
        val height = coordinates.size.height
        val grew = lastHeight[0] != UNMEASURED && height > lastHeight[0]
        lastHeight[0] = height
        if (grew && state.target === coordinates) state.revealTarget()
    }
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
    state.scrollState = scrollState
    state.marginPx = with(LocalDensity.current) { margin.toPx() }
    val lastHeight = remember { intArrayOf(UNMEASURED) }
    return onGloballyPositioned { viewport ->
        state.viewport = viewport
        val height = viewport.size.height
        val shrank = lastHeight[0] != UNMEASURED && height < lastHeight[0]
        lastHeight[0] = height
        if (shrank) state.revealTarget()
    }
}

private const val UNMEASURED = -1
