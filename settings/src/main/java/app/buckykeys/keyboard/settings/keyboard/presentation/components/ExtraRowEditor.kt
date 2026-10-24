package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme

@Composable
internal fun ExtraRowEditor(saved: List<String>, onSave: (String) -> Unit) {
    val savedText = saved.joinToString(" ")
    var text by rememberSaveable(savedText) { mutableStateOf(savedText) }
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.small)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.settings_extra_row)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.settings_extra_row_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = { onSave(text) },
            enabled = text != savedText,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_save))
        }
    }
}
