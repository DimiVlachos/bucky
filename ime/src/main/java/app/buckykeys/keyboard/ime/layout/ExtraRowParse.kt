package app.buckykeys.keyboard.ime.layout

public data class ExtraRowParse(
    val keys: List<KeySpec>,
    val unknown: List<String>,
    val overLimit: Boolean,
)
