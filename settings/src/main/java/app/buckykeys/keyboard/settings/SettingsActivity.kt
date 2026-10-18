package app.buckykeys.keyboard.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.onboarding.presentation.screen.OnboardingScreen

public class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuckyTheme {
                var tryText by rememberSaveable { mutableStateOf("") }
                OnboardingScreen(
                    tryText = tryText,
                    onTryTextChange = { tryText = it },
                    onEnableClick = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onChooseClick = {
                        getSystemService<InputMethodManager>()?.showInputMethodPicker()
                    },
                )
            }
        }
    }
}
