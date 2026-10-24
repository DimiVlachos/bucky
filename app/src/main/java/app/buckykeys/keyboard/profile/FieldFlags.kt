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
    val helpers = profile == Profile.COMPACT && inputType != InputType.TYPE_NULL
    return FieldFlags(
        rawEnter = packageName in TERMINAL_PACKAGES,
        doubleSpacePeriod =
            helpers &&
                settings.doubleSpacePeriod &&
                !isPasswordInput(inputType) &&
                inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS == 0,
        autoCapitalize = helpers && settings.autoCapitalize,
    )
}
