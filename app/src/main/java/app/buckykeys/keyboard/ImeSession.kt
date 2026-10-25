package app.buckykeys.keyboard

import app.buckykeys.keyboard.ime.layout.Layout

// Outlives the service: switching to another keyboard destroys it, coming back must not reset.
internal object ImeSession {
    val layouts: MutableMap<String, Layout> = mutableMapOf()
    var languageIndex: Int = 0
    var handedOffFrom: String? = null
}
