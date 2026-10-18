package app.buckykeys.keyboard.ime.engine.fake

import app.buckykeys.keyboard.ime.engine.InputSink

class FakeInputSink : InputSink {

    sealed interface Emitted {
        data class Text(val text: String) : Emitted

        data class Key(val keyCode: Int, val metaState: Int) : Emitted
    }

    val emitted = mutableListOf<Emitted>()

    override fun commitText(text: String) {
        emitted += Emitted.Text(text)
    }

    override fun sendKey(keyCode: Int, metaState: Int) {
        emitted += Emitted.Key(keyCode, metaState)
    }
}
