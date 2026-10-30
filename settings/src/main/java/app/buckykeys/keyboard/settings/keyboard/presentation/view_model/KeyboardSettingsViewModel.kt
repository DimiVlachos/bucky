package app.buckykeys.keyboard.settings.keyboard.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.buckykeys.keyboard.settings.keyboard.domain.interpreter.ExtraRowInterpreter
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import app.buckykeys.keyboard.settings.keyboard.domain.model.FullLayoutStyle
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
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

    fun onLabelScaleChange(percent: Int) {
        update { it.withLabelScale(percent) }
    }

    fun onKeyPreviewChange(enabled: Boolean) {
        update { it.copy(keyPreview = enabled) }
    }

    fun onHapticsChange(enabled: Boolean) {
        update { it.copy(haptics = enabled) }
    }

    fun onFnTogglesChange(enabled: Boolean) {
        update { it.copy(fnToggles = enabled) }
    }

    fun onFKeyRowChange(enabled: Boolean) {
        update { it.copy(fKeyRow = enabled) }
    }

    fun onOutsideTerminalsChange(choice: OutsideTerminals) {
        update { it.copy(outsideTerminals = choice) }
    }

    fun onFullLayoutStyleChange(style: FullLayoutStyle) {
        update { it.copy(fullLayoutStyle = style) }
    }

    fun onAutoCapitalizeChange(enabled: Boolean) {
        update { it.copy(autoCapitalize = enabled) }
    }

    fun onDoubleSpacePeriodChange(enabled: Boolean) {
        update { it.copy(doubleSpacePeriod = enabled) }
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
