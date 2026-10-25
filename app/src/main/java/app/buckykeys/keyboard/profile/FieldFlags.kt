package app.buckykeys.keyboard.profile

import android.text.InputType
import app.buckykeys.keyboard.ime.view.isPasswordInput
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings

data class FieldFlags(
    val rawEnter: Boolean,
    val doubleSpacePeriod: Boolean,
    val autoCapitalize: Boolean,
)

fun fieldFlags(
    profile: Profile,
    packageName: String?,
    inputType: Int,
    settings: KeyboardSettings,
): FieldFlags {
    val text = inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_TEXT
    val helpers = profile == Profile.COMPACT && text
    return FieldFlags(
        rawEnter = packageName in TERMINAL_PACKAGES || inputType == InputType.TYPE_NULL,
        doubleSpacePeriod =
            helpers &&
                settings.doubleSpacePeriod &&
                !isPasswordInput(inputType) &&
                inputType and InputType.TYPE_MASK_VARIATION !in noPeriodVariations &&
                inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS == 0,
        autoCapitalize = helpers && settings.autoCapitalize,
    )
}

private val noPeriodVariations =
    setOf(
        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_URI,
    )
