package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.Serializable

// Learned by the keyboard, in row heights; kept apart from KeyboardSettings so saving it doesn't
// rebuild the keyboard.
@Serializable
public data class TouchOffset(val x: Float = 0f, val y: Float = 0f) {

    public val isLearned: Boolean
        get() = x != 0f || y != 0f

    public companion object {
        public val NONE: TouchOffset = TouchOffset()
    }
}
