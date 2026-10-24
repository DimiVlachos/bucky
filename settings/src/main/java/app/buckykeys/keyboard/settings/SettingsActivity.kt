package app.buckykeys.keyboard.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import app.buckykeys.keyboard.settings.home.presentation.components.AboutFooter
import app.buckykeys.keyboard.settings.home.presentation.screen.HomeScreen
import app.buckykeys.keyboard.settings.keyboard.presentation.entry.KeyboardSettingsEntry
import app.buckykeys.keyboard.settings.keyboard.presentation.state.rememberExtraRowDraftState
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.KeyboardSettingsViewModel
import app.buckykeys.keyboard.settings.onboarding.data.source.SetupStateReader
import app.buckykeys.keyboard.settings.onboarding.domain.model.SetupState
import app.buckykeys.keyboard.settings.onboarding.presentation.screen.OnboardingSection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
public class SettingsActivity : ComponentActivity() {

    private val keyboardSettings: KeyboardSettingsViewModel by viewModels()

    private val setupReader by lazy { SetupStateReader(this) }

    private var setup by mutableStateOf(SetupState(enabled = false, selected = false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuckyTheme {
                var tryText by rememberSaveable { mutableStateOf("") }
                val extraRowDraft = rememberExtraRowDraftState()
                HomeScreen(
                    setup = {
                        OnboardingSection(
                            setup = setup,
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
                    settings = { KeyboardSettingsEntry(keyboardSettings, extraRowDraft) },
                    footer = { AboutFooter(versionName(), ::openSource) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setup = setupReader.read()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setup = setupReader.read()
    }

    private fun versionName(): String {
        val info =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION") packageManager.getPackageInfo(packageName, 0)
            }
        return info.versionName.orEmpty()
    }

    private fun openSource() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL)))
        } catch (e: ActivityNotFoundException) {
            return
        }
    }

    private companion object {
        const val SOURCE_URL = "https://github.com/DimiVlachos/bucky"
    }
}
