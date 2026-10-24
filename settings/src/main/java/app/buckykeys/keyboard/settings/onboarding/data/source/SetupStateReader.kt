package app.buckykeys.keyboard.settings.onboarding.data.source

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import app.buckykeys.keyboard.settings.onboarding.domain.model.SetupState

internal class SetupStateReader(private val context: Context) {

    fun read(): SetupState {
        val enabled =
            context.getSystemService<InputMethodManager>()?.enabledInputMethodList.orEmpty().any {
                it.packageName == context.packageName
            }
        val current =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return SetupState(
            enabled = enabled,
            selected = current?.substringBefore('/') == context.packageName,
        )
    }
}
