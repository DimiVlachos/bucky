package app.buckykeys.keyboard.settings.keyboard.presentation.entry

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.buckykeys.keyboard.settings.R
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
    val context = LocalContext.current
    LaunchedEffect(state.saveFailed) {
        if (state.saveFailed) {
            Toast.makeText(context, R.string.settings_save_failed, Toast.LENGTH_SHORT).show()
            viewModel.onSaveErrorShown()
        }
    }
    state.settings?.let {
        val extraRowText = extraRowDraft.text
        val extraRowPreview = remember(extraRowText) { viewModel.previewExtraRow(extraRowText) }
        KeyboardSettingsScreen(
            settings = it,
            onLanguageChange = viewModel::onLanguageChange,
            onRowHeightChange = viewModel::onRowHeightChange,
            onLandscapeRowHeightChange = viewModel::onLandscapeRowHeightChange,
            onLabelScaleChange = viewModel::onLabelScaleChange,
            onKeyPreviewChange = viewModel::onKeyPreviewChange,
            onHapticsChange = viewModel::onHapticsChange,
            onFKeyRowChange = viewModel::onFKeyRowChange,
            onOutsideTerminalsChange = viewModel::onOutsideTerminalsChange,
            onAutoCapitalizeChange = viewModel::onAutoCapitalizeChange,
            onDoubleSpacePeriodChange = viewModel::onDoubleSpacePeriodChange,
            extraRowDraft = extraRowDraft,
            extraRowPreview = extraRowPreview,
            onExtraRowSave = {
                if (extraRowPreview.valid) saveExtraRow(viewModel, extraRowDraft, focusManager)
            },
            onFullLayoutStyleChange = viewModel::onFullLayoutStyleChange,
        )
    }
}

private fun saveExtraRow(
    viewModel: KeyboardSettingsViewModel,
    draft: ExtraRowDraftState,
    focusManager: FocusManager,
) {
    viewModel.onExtraRowSave(draft.text)
    focusManager.clearFocus()
}
