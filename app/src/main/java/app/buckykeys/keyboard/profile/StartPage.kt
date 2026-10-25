package app.buckykeys.keyboard.profile

import android.text.InputType
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts

fun startPageFor(inputType: Int): String =
    when (inputType and InputType.TYPE_MASK_CLASS) {
        InputType.TYPE_CLASS_NUMBER,
        InputType.TYPE_CLASS_PHONE,
        InputType.TYPE_CLASS_DATETIME -> BuiltInLayouts.PAGE_SYMBOLS
        else -> BuiltInLayouts.PAGE_LETTERS
    }
