package dev.pckeys.keyboard.ime.dispatch

import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import dev.pckeys.keyboard.ime.engine.InputSink

public class InputDispatcher(private val connection: () -> InputConnection?) : InputSink {

    override fun commitText(text: String) {
        connection()?.commitText(text, 1)
    }

    override fun sendKey(keyCode: Int, metaState: Int) {
        val target = connection() ?: return
        val time = SystemClock.uptimeMillis()
        target.sendKeyEvent(KeyEvent(time, time, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        target.sendKeyEvent(KeyEvent(time, time, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }
}
