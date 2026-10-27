package app.buckykeys.keyboard.ime.layout

internal object DefaultLabels {

    fun of(def: KeyDef): String =
        when (def) {
            is KeyDef.Char -> if (def.primary.isBlank()) "" else def.primary
            is KeyDef.Dead -> def.primary
            is KeyDef.Special -> special(def.code)
            is KeyDef.Modifier -> modifier(def.kind)
            is KeyDef.LayoutSwitch ->
                when (def.targetId) {
                    BuiltInLayouts.PAGE_SYMBOLS -> "?123"
                    BuiltInLayouts.PAGE_LETTERS -> "ABC"
                    BuiltInLayouts.PAGE_SYMBOLS_MORE -> "=\\<"
                    BuiltInLayouts.OTHER_KEYBOARD -> "⌨"
                    BuiltInLayouts.SETTINGS -> "⚙"
                    BuiltInLayouts.NEXT_LANGUAGE -> "🌐"
                    else -> def.targetId
                }
        }

    private fun special(code: KeyCode): String =
        when (code) {
            KeyCode.ESC -> "Esc"
            KeyCode.TAB -> "⇥"
            KeyCode.ENTER -> "Enter"
            KeyCode.BACKSPACE -> "⌫"
            KeyCode.FORWARD_DEL -> "Del"
            KeyCode.INSERT -> "Ins"
            KeyCode.HOME -> "Home"
            KeyCode.END -> "End"
            KeyCode.PAGE_UP -> "PgUp"
            KeyCode.PAGE_DOWN -> "PgDn"
            KeyCode.UP -> "↑"
            KeyCode.DOWN -> "↓"
            KeyCode.LEFT -> "←"
            KeyCode.RIGHT -> "→"
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
            KeyCode.F12 -> code.name
            KeyCode.SYSRQ -> "SysRq"
            KeyCode.SCROLL_LOCK -> "ScrLk"
            KeyCode.BREAK -> "Brk"
            KeyCode.NUM_LOCK -> "NumLk"
            KeyCode.DPAD_CENTER -> "○"
        }

    private fun modifier(kind: ModifierKind): String =
        when (kind) {
            ModifierKind.CTRL -> "Ctrl"
            ModifierKind.ALT -> "Alt"
            ModifierKind.SHIFT -> "⇧"
            ModifierKind.FN -> "Fn"
            ModifierKind.META -> "❖"
        }
}
