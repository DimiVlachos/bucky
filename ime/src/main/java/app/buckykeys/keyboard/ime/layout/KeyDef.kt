package app.buckykeys.keyboard.ime.layout

public sealed interface KeyDef {
    public data class Char(
        val primary: String,
        val shifted: String,
        val alternates: List<String> = emptyList(),
    ) : KeyDef

    public data class Special(val code: KeyCode) : KeyDef

    public data class Modifier(val kind: ModifierKind) : KeyDef

    public data class LayoutSwitch(val targetId: String) : KeyDef
}

public enum class KeyCode {
    ESC,
    TAB,
    ENTER,
    BACKSPACE,
    FORWARD_DEL,
    INSERT,
    HOME,
    END,
    PAGE_UP,
    PAGE_DOWN,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
}

public enum class ModifierKind {
    CTRL,
    ALT,
    SHIFT,
    FN,
}
