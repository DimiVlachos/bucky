package app.buckykeys.keyboard.settings.keyboard.presentation.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.buckykeys.keyboard.settings.keyboard.presentation.components.ExtraRowSaveBar
import app.buckykeys.keyboard.settings.keyboard.presentation.screen.KeyboardSettingsScreen
import app.buckykeys.keyboard.settings.keyboard.presentation.state.ExtraRowDraftState
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.KeyboardSettingsViewModel

@Composable
internal fun KeyboardSettingsEntry(
    viewModel: KeyboardSettingsViewModel,
    extraRowDraft: ExtraRowDraftState,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    state.settings?.let {
        KeyboardSettingsScreen(
            settings = it,
            onLanguageChange = viewModel::onLanguageChange,
            onRowHeightChange = viewModel::onRowHeightChange,
            onLandscapeRowHeightChange = viewModel::onLandscapeRowHeightChange,
            onKeyPreviewChange = viewModel::onKeyPreviewChange,
            onHapticsChange = viewModel::onHapticsChange,
            onFKeyRowChange = viewModel::onFKeyRowChange,
            extraRowDraft = extraRowDraft,
            onExtraRowSave = { saveExtraRow(viewModel, extraRowDraft, focusManager) },
        )
    }
}

@Composable
internal fun ExtraRowSaveBarEntry(
    viewModel: KeyboardSettingsViewModel,
    extraRowDraft: ExtraRowDraftState,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val saved = state.settings?.extraRow.orEmpty()
    val changed = extraRowDraft.changed(saved)
    ExtraRowSaveBar(
        visible = extraRowDraft.focused || changed,
        enabled = changed,
        onSave = { saveExtraRow(viewModel, extraRowDraft, focusManager) },
    )
}

private fun saveExtraRow(
    viewModel: KeyboardSettingsViewModel,
    draft: ExtraRowDraftState,
    focusManager: FocusManager,
) {
    draft.text?.let(viewModel::onExtraRowSave)
    draft.reset()
    focusManager.clearFocus()
}
