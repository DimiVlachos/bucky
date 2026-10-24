package app.buckykeys.keyboard.ime.dispatch

import android.text.InputType
import android.view.inputmethod.EditorInfo

public const val NO_ENTER_ACTION: Int = -1

private const val PERIOD_WINDOW_MS = 1_000L

public fun enterAction(imeOptions: Int, inputType: Int, rawEnter: Boolean): Int {
    if (rawEnter || imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return NO_ENTER_ACTION
    if (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0) return NO_ENTER_ACTION
    val action = imeOptions and EditorInfo.IME_MASK_ACTION
    return when (action) {
        EditorInfo.IME_ACTION_UNSPECIFIED,
        EditorInfo.IME_ACTION_NONE -> NO_ENTER_ACTION
        else -> action
    }
}

public fun shouldInsertPeriod(before: CharSequence?, sinceLastSpaceMs: Long): Boolean {
    if (before == null || before.length < 2 || !periodWindowOpen(sinceLastSpaceMs)) return false
    return before[before.lastIndex] == ' ' && before[before.lastIndex - 1].isLetterOrDigit()
}

public fun periodWindowOpen(sinceLastSpaceMs: Long): Boolean = sinceLastSpaceMs <= PERIOD_WINDOW_MS
