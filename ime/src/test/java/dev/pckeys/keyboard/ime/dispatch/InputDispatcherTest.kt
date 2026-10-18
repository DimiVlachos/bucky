package dev.pckeys.keyboard.ime.dispatch

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class InputDispatcherTest {

    @Test
    fun `a missing input connection makes every call a no op`() {
        var lookups = 0
        val dispatcher = InputDispatcher {
            lookups++
            null
        }
        dispatcher.commitText("a")
        dispatcher.sendKey(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON)
        assertEquals(2, lookups)
    }
}
