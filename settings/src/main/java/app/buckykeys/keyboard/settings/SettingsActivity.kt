package app.buckykeys.keyboard.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.home.presentation.screen.HomeScreen
import app.buckykeys.keyboard.settings.keyboard.presentation.entry.KeyboardSettingsEntry
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.KeyboardSettingsViewModel
import app.buckykeys.keyboard.settings.onboarding.presentation.screen.OnboardingSection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
public class SettingsActivity : ComponentActivity() {

    private val keyboardSettings: KeyboardSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuckyTheme {
                var tryText by rememberSaveable { mutableStateOf("") }
                HomeScreen(
                    setup = {
                        OnboardingSection(
                            tryText = tryText,
                            onTryTextChange = { tryText = it },
                            onEnableClick = {
                                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                            },
                            onChooseClick = {
                                getSystemService<InputMethodManager>()?.showInputMethodPicker()
                            },
                        )
                    },
                    settings = { KeyboardSettingsEntry(keyboardSettings) },
                )
            }
        }
    }
}
