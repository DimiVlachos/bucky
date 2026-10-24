package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.components.keepVisibleWhenFocused
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import app.buckykeys.keyboard.settings.keyboard.presentation.state.ExtraRowDraftState

@Composable
internal fun ExtraRowEditor(
    saved: List<String>,
    draft: ExtraRowDraftState,
    preview: ExtraRowPreview,
    onSave: () -> Unit,
) {
    val text = draft.display(saved)
    val invalid = text.isNotBlank() && !preview.valid
    OutlinedTextField(
        value = text,
        onValueChange = draft::edit,
        label = { Text(stringResource(R.string.settings_extra_row)) },
        singleLine = true,
        isError = invalid,
        trailingIcon = {
            AnimatedVisibility(
                visible = text.isNotBlank() && preview.valid,
                enter =
                    fadeIn(tween(ENTER_MS)) + scaleIn(tween(ENTER_MS), initialScale = HIDDEN_SCALE),
                exit =
                    fadeOut(tween(EXIT_MS)) + scaleOut(tween(EXIT_MS), targetScale = HIDDEN_SCALE),
            ) {
                IconButton(onClick = onSave) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.settings_save),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        supportingText = { ExtraRowSupportingText(text = text, preview = preview) },
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
        modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
    )
}

@Composable
private fun ExtraRowSupportingText(text: String, preview: ExtraRowPreview) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.extraSmall)) {
        when {
            text.isBlank() -> Text(stringResource(R.string.settings_extra_row_hint))
            preview.labels.isNotEmpty() ->
                Text(
                    text =
                        stringResource(
                            R.string.settings_extra_row_preview,
                            preview.labels.joinToString(" · "),
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
        if (preview.unknown.isNotEmpty()) {
            Text(
                text =
                    stringResource(
                        R.string.settings_extra_row_unknown,
                        preview.unknown.joinToString(", "),
                    ),
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (preview.overLimit) {
            Text(
                text = stringResource(R.string.settings_extra_row_limit, MAX_KEYS),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val ENTER_MS = 150
private const val EXIT_MS = 120
private const val HIDDEN_SCALE = 0.6f
private const val MAX_KEYS = 15
