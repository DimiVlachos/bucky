package app.buckykeys.keyboard.settings.onboarding.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme

@Composable
internal fun OnboardingScreen(
    tryText: String,
    onTryTextChange: (String) -> Unit,
    onEnableClick: () -> Unit,
    onChooseClick: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(BuckyTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onEnableClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_enable))
            }
            Button(onClick = onChooseClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_choose))
            }
            OutlinedTextField(
                value = tryText,
                onValueChange = onTryTextChange,
                label = { Text(stringResource(R.string.onboarding_try)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenLightPreview() {
    BuckyTheme(darkTheme = false) { OnboardingScreen("", {}, {}, {}) }
}

@Preview
@Composable
private fun OnboardingScreenDarkPreview() {
    BuckyTheme(darkTheme = true) { OnboardingScreen("", {}, {}, {}) }
}
