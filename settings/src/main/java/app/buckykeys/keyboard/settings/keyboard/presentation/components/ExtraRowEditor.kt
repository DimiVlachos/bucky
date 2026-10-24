package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.components.keepVisibleWhenFocused
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.presentation.state.ExtraRowDraftState
import kotlinx.coroutines.launch

@Composable
internal fun ExtraRowEditor(
    saved: List<String>,
    draft: ExtraRowDraftState,
    preview: ExtraRowPreview,
    onSave: () -> Unit,
) {
    val savedText = saved.joinToString(" ")
    LaunchedEffect(savedText) { draft.syncTo(savedText) }
    val text = draft.text
    val invalid = text.isNotBlank() && !preview.valid
    val textAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    val interactionSource = remember { MutableInteractionSource() }
    val lineLimits = TextFieldLineLimits.SingleLine
    val colors = MaterialTheme.colorScheme
    val decorator =
        OutlinedTextFieldDefaults.decorator(
            state = draft.textField,
            enabled = true,
            lineLimits = lineLimits,
            outputTransformation = null,
            interactionSource = interactionSource,
            label = { Text(stringResource(R.string.settings_extra_row)) },
            trailingIcon = {
                AnimatedVisibility(
                    visible = text.isNotBlank() && preview.valid,
                    enter =
                        fadeIn(tween(ENTER_MS)) +
                            scaleIn(tween(ENTER_MS), initialScale = HIDDEN_SCALE),
                    exit =
                        fadeOut(tween(EXIT_MS)) +
                            scaleOut(tween(EXIT_MS), targetScale = HIDDEN_SCALE),
                ) {
                    IconButton(onClick = onSave) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(R.string.settings_save),
                            tint = colors.primary,
                        )
                    }
                }
            },
            supportingText = {
                ExtraRowSupportingText(
                    text = text,
                    preview = preview,
                    onUseTermuxDefault = {
                        scope.launch {
                            textAlpha.snapTo(0f)
                            draft.fill(KeyboardSettings.TERMUX_DEFAULT_EXTRA_ROW)
                            textAlpha.animateTo(1f, tween(FILL_MS))
                        }
                    },
                )
            },
            isError = invalid,
        )
    val edge = START_EDGE
    val indent = with(LocalDensity.current) { edge.toSp() }
    BasicTextField(
        state = draft.textField,
        modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
        textStyle =
            MaterialTheme.typography.bodyLarge.copy(
                color = colors.onSurface.copy(alpha = textAlpha.value),
                textIndent = TextIndent(firstLine = indent),
            ),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
        onKeyboardAction = { onSave() },
        lineLimits = lineLimits,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(if (invalid) colors.error else colors.primary),
        scrollState = scroll,
        decorator = { innerTextField ->
            decorator.Decoration {
                Box(
                    modifier =
                        Modifier.extendIntoPadding(start = edge, end = END_EXTENSION)
                            .horizontalFadingEdges(scroll, start = edge, end = END_FADE),
                ) {
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun ExtraRowSupportingText(
    text: String,
    preview: ExtraRowPreview,
    onUseTermuxDefault: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.extraSmall)) {
        AnimatedContent(
            targetState = text.isBlank(),
            transitionSpec = {
                fadeIn(tween(SWAP_IN_MS, delayMillis = SWAP_OUT_MS)) togetherWith
                    fadeOut(tween(SWAP_OUT_MS))
            },
            label = "extra row supporting text",
        ) { blank ->
            when {
                blank ->
                    Column {
                        Text(stringResource(R.string.settings_extra_row_hint))
                        TextButton(
                            onClick = onUseTermuxDefault,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(stringResource(R.string.settings_extra_row_termux_default))
                        }
                    }
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
private const val FILL_MS = 200
private val START_EDGE = 10.dp
private val END_EXTENSION = 6.dp
private val END_FADE = 12.dp
private const val SWAP_OUT_MS = 100
private const val SWAP_IN_MS = 150
private const val EXIT_MS = 120
private const val HIDDEN_SCALE = 0.6f
private const val MAX_KEYS = 15
