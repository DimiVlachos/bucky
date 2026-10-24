package app.buckykeys.keyboard.settings.keyboard.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
internal class ExtraRowDraftState(text: String?) {

    var text: String? by mutableStateOf(text)
        private set

    var focused: Boolean by mutableStateOf(false)

    fun display(saved: List<String>): String = text ?: saved.joinToString(" ")

    fun changed(saved: List<String>): Boolean =
        text?.normalized()?.let { it != saved.joinToString(" ") } == true

    fun edit(value: String) {
        text = value
    }

    fun reset() {
        text = null
    }

    companion object {
        val Saver: Saver<ExtraRowDraftState, String> =
            Saver(
                save = { it.text ?: NOT_EDITED },
                restore = { ExtraRowDraftState(it.takeIf { it != NOT_EDITED }) },
            )

        private const val NOT_EDITED = "\u0000"
    }
}

@Composable
internal fun rememberExtraRowDraftState(): ExtraRowDraftState =
    rememberSaveable(saver = ExtraRowDraftState.Saver) { ExtraRowDraftState(null) }

private val Whitespace = Regex("\\s+")

private fun String.normalized(): String =
    split(Whitespace).filter { it.isNotEmpty() }.joinToString(" ")
