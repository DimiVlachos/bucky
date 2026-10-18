package app.buckykeys.keyboard.ime.engine

public interface InputSink {
    public fun commitText(text: String)

    public fun sendKey(keyCode: Int, metaState: Int)
}

public enum class PointerAction {
    DOWN,
    MOVE,
    UP,
    CANCEL,
}
