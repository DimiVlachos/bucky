package app.buckykeys.keyboard.ime.dispatch

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo

public const val NO_ENTER_ACTION: Int = -1

private const val PERIOD_WINDOW_MS = 1_000L

public fun enterAction(imeOptions: Int, rawEnter: Boolean): Int {
    if (rawEnter || imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return NO_ENTER_ACTION
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

public fun enterKeyAction(metaState: Int, imeOptions: Int, rawEnter: Boolean): Int =
    if (metaState and CHORD_META != 0) NO_ENTER_ACTION else enterAction(imeOptions, rawEnter)

private const val CHORD_META =
    KeyEvent.META_CTRL_MASK or KeyEvent.META_ALT_MASK or KeyEvent.META_META_MASK
