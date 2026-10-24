package app.buckykeys.keyboard.settings.onboarding.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.onboarding.domain.model.SetupState

@Composable
internal fun OnboardingSection(
    setup: SetupState,
    tryText: String,
    onTryTextChange: (String) -> Unit,
    onEnableClick: () -> Unit,
    onChooseClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.medium)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        val status =
            when {
                setup.enabled && setup.selected -> R.string.onboarding_status_ready
                setup.enabled -> R.string.onboarding_status_enabled
                else -> null
            }
        status?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        SetupButton(
            text = stringResource(R.string.onboarding_enable),
            primary = !setup.enabled,
            onClick = onEnableClick,
        )
        SetupButton(
            text = stringResource(R.string.onboarding_choose),
            primary = setup.enabled && !setup.selected,
            onClick = onChooseClick,
        )
        OutlinedTextField(
            value = tryText,
            onValueChange = onTryTextChange,
            label = { Text(stringResource(R.string.onboarding_try)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SetupButton(text: String, primary: Boolean, onClick: () -> Unit) {
    if (primary) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
    }
}

@Preview
@Composable
private fun OnboardingSectionLightPreview() {
    BuckyTheme(darkTheme = false) {
        OnboardingSection(SetupState(enabled = true, selected = false), "", {}, {}, {})
    }
}

@Preview
@Composable
private fun OnboardingSectionDarkPreview() {
    BuckyTheme(darkTheme = true) {
        OnboardingSection(SetupState(enabled = true, selected = false), "", {}, {}, {})
    }
}
