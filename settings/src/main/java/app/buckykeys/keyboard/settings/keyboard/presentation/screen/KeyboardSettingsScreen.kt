package app.buckykeys.keyboard.settings.keyboard.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.presentation.components.CheckboxRow
import app.buckykeys.keyboard.settings.keyboard.presentation.components.HeightSlider
import app.buckykeys.keyboard.settings.keyboard.presentation.components.SectionTitle
import app.buckykeys.keyboard.settings.keyboard.presentation.components.SwitchRow

@Composable
internal fun KeyboardSettingsScreen(
    settings: KeyboardSettings,
    onLanguageChange: (KeyboardLanguage, Boolean) -> Unit,
    onRowHeightChange: (Int) -> Unit,
    onLandscapeRowHeightChange: (Int) -> Unit,
    onKeyPreviewChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.medium)) {
        SectionTitle(stringResource(R.string.settings_languages))
        KeyboardLanguage.entries.forEach { language ->
            val checked = language in settings.languages
            CheckboxRow(
                text = stringResource(language.labelRes()),
                checked = checked,
                enabled = !checked || settings.languages.size > 1,
                onCheckedChange = { onLanguageChange(language, it) },
            )
        }
        Text(
            text = stringResource(R.string.settings_languages_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionTitle(stringResource(R.string.settings_size))
        HeightSlider(
            text = stringResource(R.string.settings_row_height),
            valueDp = settings.rowHeightDp,
            range = KeyboardSettings.ROW_HEIGHT_RANGE,
            onValueChange = onRowHeightChange,
        )
        HeightSlider(
            text = stringResource(R.string.settings_landscape_row_height),
            valueDp = settings.landscapeRowHeightDp,
            range = KeyboardSettings.LANDSCAPE_ROW_HEIGHT_RANGE,
            onValueChange = onLandscapeRowHeightChange,
        )
        SectionTitle(stringResource(R.string.settings_feedback))
        SwitchRow(
            text = stringResource(R.string.settings_key_preview),
            checked = settings.keyPreview,
            onCheckedChange = onKeyPreviewChange,
        )
        SwitchRow(
            text = stringResource(R.string.settings_haptics),
            checked = settings.haptics,
            onCheckedChange = onHapticsChange,
        )
    }
}

private fun KeyboardLanguage.labelRes(): Int =
    when (this) {
        KeyboardLanguage.EN_US -> R.string.settings_language_en_us
        KeyboardLanguage.EL -> R.string.settings_language_el
    }

@Preview
@Composable
private fun KeyboardSettingsScreenPreview() {
    BuckyTheme(darkTheme = true) {
        KeyboardSettingsScreen(KeyboardSettings.defaults("el"), { _, _ -> }, {}, {}, {}, {})
    }
}
