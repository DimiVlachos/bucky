package app.buckykeys.keyboard.settings.keyboard.presentation.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
internal class ExtraRowDraftState(
    val textField: TextFieldState,
    private val synced: MutableState<String?> = mutableStateOf(null),
) {

    val text: String
        get() = textField.text.toString()

    fun syncTo(saved: String) {
        if (saved == synced.value) return
        synced.value = saved
        textField.setTextAndPlaceCursorAtEnd(saved)
    }

    fun fill(text: String) {
        textField.setTextAndPlaceCursorAtEnd(text)
    }
}

@Composable
internal fun rememberExtraRowDraftState(): ExtraRowDraftState {
    val field = rememberTextFieldState()
    val synced = rememberSaveable { mutableStateOf<String?>(null) }
    return remember(field, synced) { ExtraRowDraftState(field, synced) }
}
