package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.components.keepVisibleWhenFocused
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.keyboard.presentation.state.ExtraRowDraftState

@Composable
internal fun ExtraRowEditor(saved: List<String>, draft: ExtraRowDraftState, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.small)) {
        OutlinedTextField(
            value = draft.display(saved),
            onValueChange = draft::edit,
            label = { Text(stringResource(R.string.settings_extra_row)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            modifier =
                Modifier.fillMaxWidth().keepVisibleWhenFocused().onFocusChanged {
                    draft.focused = it.isFocused
                },
        )
        Text(
            text = stringResource(R.string.settings_extra_row_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
