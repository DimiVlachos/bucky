package app.buckykeys.keyboard.settings.keyboard.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class KeyboardSettingsViewModel
@Inject
constructor(private val repository: KeyboardSettingsRepository) : ViewModel() {

    val state: StateFlow<KeyboardSettings?> =
        repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            null,
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

    private fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch { repository.update(transform) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
