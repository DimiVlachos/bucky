package app.buckykeys.keyboard.settings.keyboard.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.buckykeys.keyboard.settings.keyboard.domain.interpreter.ExtraRowInterpreter
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import app.buckykeys.keyboard.settings.keyboard.presentation.state.KeyboardSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class KeyboardSettingsViewModel
@Inject
constructor(
    private val repository: KeyboardSettingsRepository,
    private val extraRowInterpreter: ExtraRowInterpreter,
) : ViewModel() {

    private val saveFailed = MutableStateFlow(false)

    val state: StateFlow<KeyboardSettingsState> =
        combine(repository.settings, saveFailed) { settings, failed ->
                KeyboardSettingsState(settings = settings, saveFailed = failed)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                KeyboardSettingsState(),
            )

    fun onLanguageChange(language: KeyboardLanguage, enabled: Boolean) {
        update { it.withLanguage(language, enabled) }
    }

    fun onRowHeightChange(dp: Int) {
        update { it.withRowHeight(dp) }
    }

    fun onLandscapeRowHeightChange(dp: Int) {
        update { it.withLandscapeRowHeight(dp) }
    }

    fun onKeyPreviewChange(enabled: Boolean) {
        update { it.copy(keyPreview = enabled) }
    }

    fun onHapticsChange(enabled: Boolean) {
        update { it.copy(haptics = enabled) }
    }

    fun onFKeyRowChange(enabled: Boolean) {
        update { it.copy(fKeyRow = enabled) }
    }

    fun onExtraRowSave(text: String) {
        update { it.withExtraRow(text) }
    }

    fun previewExtraRow(text: String): ExtraRowPreview =
        extraRowInterpreter.interpret(text.split(Whitespace).filter { it.isNotEmpty() })

    fun onSaveErrorShown() {
        saveFailed.value = false
    }

    private fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch {
            try {
                repository.update(transform)
            } catch (e: IOException) {
                saveFailed.value = true
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val Whitespace = Regex("\\s+")
    }
}
