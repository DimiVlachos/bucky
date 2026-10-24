package app.buckykeys.keyboard.ime.dispatch

import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import app.buckykeys.keyboard.ime.engine.InputSink

public class InputDispatcher(private val connection: () -> InputConnection?) : InputSink {

    public var rawEnter: Boolean = true
    public var imeOptions: Int = 0
    public var inputType: Int = 0
    public var doubleSpacePeriod: Boolean = false

    private var lastSpaceAt = NO_SPACE

    override fun commitText(text: String) {
        val target = connection() ?: return
        if (text != " ") {
            lastSpaceAt = NO_SPACE
            target.commitText(text, 1)
            return
        }
        val now = SystemClock.uptimeMillis()
        val since = if (lastSpaceAt == NO_SPACE) Long.MAX_VALUE else now - lastSpaceAt
        if (
            doubleSpacePeriod &&
                periodWindowOpen(since) &&
                shouldInsertPeriod(target.getTextBeforeCursor(2, 0), since)
        ) {
            target.deleteSurroundingText(1, 0)
            target.commitText(". ", 1)
            lastSpaceAt = NO_SPACE
        } else {
            target.commitText(text, 1)
            lastSpaceAt = now
        }
    }

    override fun sendKey(keyCode: Int, metaState: Int) {
        val target = connection() ?: return
        lastSpaceAt = NO_SPACE
        if (keyCode == KeyEvent.KEYCODE_ENTER && metaState == 0) {
            val action = enterAction(imeOptions, inputType, rawEnter)
            if (action != NO_ENTER_ACTION) {
                target.performEditorAction(action)
                return
            }
        }
        val time = SystemClock.uptimeMillis()
        target.sendKeyEvent(KeyEvent(time, time, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        target.sendKeyEvent(KeyEvent(time, time, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }

    private companion object {
        const val NO_SPACE = -1L
    }
}
