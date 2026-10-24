package app.buckykeys.keyboard.settings.keyboard.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
import app.buckykeys.keyboard.settings.keyboard.presentation.components.CheckboxRow
import app.buckykeys.keyboard.settings.keyboard.presentation.components.ExtraRowEditor
import app.buckykeys.keyboard.settings.keyboard.presentation.components.RadioRow
import app.buckykeys.keyboard.settings.keyboard.presentation.components.SectionTitle
import app.buckykeys.keyboard.settings.keyboard.presentation.components.SettingSlider
import app.buckykeys.keyboard.settings.keyboard.presentation.components.SwitchRow
import app.buckykeys.keyboard.settings.keyboard.presentation.state.ExtraRowDraftState

@Composable
internal fun KeyboardSettingsScreen(
    settings: KeyboardSettings,
    onLanguageChange: (KeyboardLanguage, Boolean) -> Unit,
    onRowHeightChange: (Int) -> Unit,
    onLandscapeRowHeightChange: (Int) -> Unit,
    onLabelScaleChange: (Int) -> Unit,
    onKeyPreviewChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onFKeyRowChange: (Boolean) -> Unit,
    onOutsideTerminalsChange: (OutsideTerminals) -> Unit,
    onAutoCapitalizeChange: (Boolean) -> Unit,
    onDoubleSpacePeriodChange: (Boolean) -> Unit,
    extraRowDraft: ExtraRowDraftState,
    extraRowPreview: ExtraRowPreview,
    onExtraRowSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.medium)) {
        SectionTitle(stringResource(R.string.settings_languages))
        val requiredDescription = stringResource(R.string.settings_language_required)
        Column {
            KeyboardLanguage.entries.forEach { language ->
                val checked = language in settings.languages
                val enabled = !checked || settings.languages.size > 1
                CheckboxRow(
                    text = stringResource(language.labelRes()),
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = { onLanguageChange(language, it) },
                    stateDescription = if (enabled) null else requiredDescription,
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_languages_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionTitle(stringResource(R.string.settings_size))
        SettingSlider(
            text = stringResource(R.string.settings_row_height),
            value = settings.rowHeightDp,
            range = KeyboardSettings.ROW_HEIGHT_RANGE,
            valueFormat = R.string.settings_dp,
            onValueChangeFinished = onRowHeightChange,
        )
        SettingSlider(
            text = stringResource(R.string.settings_landscape_row_height),
            value = settings.landscapeRowHeightDp,
            range = KeyboardSettings.LANDSCAPE_ROW_HEIGHT_RANGE,
            valueFormat = R.string.settings_dp,
            onValueChangeFinished = onLandscapeRowHeightChange,
        )
        SettingSlider(
            text = stringResource(R.string.settings_label_size),
            value = settings.labelScalePercent,
            range = KeyboardSettings.LABEL_SCALE_RANGE,
            valueFormat = R.string.settings_percent,
            step = KeyboardSettings.LABEL_SCALE_STEP,
            onValueChangeFinished = onLabelScaleChange,
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
        SectionTitle(stringResource(R.string.settings_extra_rows))
        SwitchRow(
            text = stringResource(R.string.settings_f_key_row),
            checked = settings.fKeyRow,
            onCheckedChange = onFKeyRowChange,
        )
        ExtraRowEditor(
            saved = settings.extraRow,
            draft = extraRowDraft,
            preview = extraRowPreview,
            onSave = onExtraRowSave,
        )
        SectionTitle(stringResource(R.string.settings_apps))
        Text(
            text = stringResource(R.string.settings_outside_terminals),
            style = MaterialTheme.typography.bodyLarge,
        )
        Column {
            OutsideTerminals.entries.forEach { choice ->
                RadioRow(
                    text = stringResource(choice.labelRes()),
                    description =
                        if (choice == OutsideTerminals.OTHER_KEYBOARD) {
                            stringResource(R.string.settings_outside_other_hint)
                        } else {
                            null
                        },
                    selected = settings.outsideTerminals == choice,
                    onClick = { onOutsideTerminalsChange(choice) },
                )
            }
        }
        val helpersApply = settings.outsideTerminals != OutsideTerminals.FULL
        SwitchRow(
            text = stringResource(R.string.settings_auto_capitalize),
            checked = settings.autoCapitalize,
            onCheckedChange = onAutoCapitalizeChange,
            enabled = helpersApply,
        )
        SwitchRow(
            text = stringResource(R.string.settings_double_space_period),
            checked = settings.doubleSpacePeriod,
            onCheckedChange = onDoubleSpacePeriodChange,
            enabled = helpersApply,
        )
    }
}

private fun OutsideTerminals.labelRes(): Int =
    when (this) {
        OutsideTerminals.COMPACT -> R.string.settings_outside_compact
        OutsideTerminals.FULL -> R.string.settings_outside_full
        OutsideTerminals.OTHER_KEYBOARD -> R.string.settings_outside_other
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
        KeyboardSettingsScreen(
            KeyboardSettings.defaults("el"),
            { _, _ -> },
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            ExtraRowDraftState(TextFieldState()),
            ExtraRowPreview(emptyList(), emptyList(), overLimit = false),
            {},
        )
    }
}
