package dev.pckeys.keyboard.ime.engine

import android.view.KeyEvent
import dev.pckeys.keyboard.ime.layout.KeyCode

internal object KeyCodes {

    fun forChar(c: Char): Int =
        when (c) {
            in 'a'..'z' -> KeyEvent.KEYCODE_A + (c - 'a')
            in 'A'..'Z' -> KeyEvent.KEYCODE_A + (c - 'A')
            in '0'..'9' -> KeyEvent.KEYCODE_0 + (c - '0')
            ' ' -> KeyEvent.KEYCODE_SPACE
            '-' -> KeyEvent.KEYCODE_MINUS
            '=' -> KeyEvent.KEYCODE_EQUALS
            '[' -> KeyEvent.KEYCODE_LEFT_BRACKET
            ']' -> KeyEvent.KEYCODE_RIGHT_BRACKET
            ';' -> KeyEvent.KEYCODE_SEMICOLON
            '\'' -> KeyEvent.KEYCODE_APOSTROPHE
            ',' -> KeyEvent.KEYCODE_COMMA
            '.' -> KeyEvent.KEYCODE_PERIOD
            '/' -> KeyEvent.KEYCODE_SLASH
            '\\' -> KeyEvent.KEYCODE_BACKSLASH
            '`' -> KeyEvent.KEYCODE_GRAVE
            else -> KeyEvent.KEYCODE_UNKNOWN
        }

    fun forSpecial(code: KeyCode): Int =
        when (code) {
            KeyCode.ESC -> KeyEvent.KEYCODE_ESCAPE
            KeyCode.TAB -> KeyEvent.KEYCODE_TAB
            KeyCode.ENTER -> KeyEvent.KEYCODE_ENTER
            KeyCode.BACKSPACE -> KeyEvent.KEYCODE_DEL
            KeyCode.FORWARD_DEL -> KeyEvent.KEYCODE_FORWARD_DEL
            KeyCode.INSERT -> KeyEvent.KEYCODE_INSERT
            KeyCode.HOME -> KeyEvent.KEYCODE_MOVE_HOME
            KeyCode.END -> KeyEvent.KEYCODE_MOVE_END
            KeyCode.PAGE_UP -> KeyEvent.KEYCODE_PAGE_UP
            KeyCode.PAGE_DOWN -> KeyEvent.KEYCODE_PAGE_DOWN
            KeyCode.UP -> KeyEvent.KEYCODE_DPAD_UP
            KeyCode.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
            KeyCode.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
            KeyCode.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            KeyCode.F1,
            KeyCode.F2,
            KeyCode.F3,
            KeyCode.F4,
            KeyCode.F5,
            KeyCode.F6,
            KeyCode.F7,
            KeyCode.F8,
            KeyCode.F9,
            KeyCode.F10,
            KeyCode.F11,
            KeyCode.F12 -> KeyEvent.KEYCODE_F1 + (code.ordinal - KeyCode.F1.ordinal)
        }
}
