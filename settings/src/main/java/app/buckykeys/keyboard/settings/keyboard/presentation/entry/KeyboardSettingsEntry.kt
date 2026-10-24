package app.buckykeys.keyboard.settings.keyboard.presentation.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.buckykeys.keyboard.settings.keyboard.presentation.screen.KeyboardSettingsScreen
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.KeyboardSettingsViewModel

@Composable
internal fun KeyboardSettingsEntry(viewModel: KeyboardSettingsViewModel) {
    val settings by viewModel.state.collectAsStateWithLifecycle()
    settings?.let {
        KeyboardSettingsScreen(
            settings = it,
            onLanguageChange = viewModel::onLanguageChange,
            onRowHeightChange = viewModel::onRowHeightChange,
            onLandscapeRowHeightChange = viewModel::onLandscapeRowHeightChange,
            onKeyPreviewChange = viewModel::onKeyPreviewChange,
            onHapticsChange = viewModel::onHapticsChange,
        )
    }
}
