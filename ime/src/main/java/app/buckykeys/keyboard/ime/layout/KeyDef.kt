package app.buckykeys.keyboard.ime.layout

public sealed interface KeyDef {
    public data class Char(
        val primary: String,
        val shifted: String,
        val alternates: List<KeyDef> = emptyList(),
        val latin: String? = null,
        val composed: Map<String, String> = emptyMap(),
        val composedShifted: Map<String, String> = emptyMap(),
    ) : KeyDef

    public data class Dead(
        val primary: String,
        val shifted: String,
        val latin: String? = null,
        val alternates: List<KeyDef> = emptyList(),
    ) : KeyDef

    public data class Special(val code: KeyCode) : KeyDef

    public data class Modifier(val kind: ModifierKind) : KeyDef

    public data class LayoutSwitch(val targetId: String, val holdTargetId: String? = null) : KeyDef
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
    SYSRQ,
    SCROLL_LOCK,
    BREAK,
    NUM_LOCK,
    DPAD_CENTER,
}

public enum class ModifierKind {
    CTRL,
    ALT,
    SHIFT,
    FN,
    META,
}

internal val KeyDef.longPressAlternates: List<KeyDef>
    get() =
        when (this) {
            is KeyDef.Char -> alternates
            is KeyDef.Dead -> alternates
            else -> emptyList()
        }
