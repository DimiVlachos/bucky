package app.buckykeys.keyboard.ime.layout

public data class KeySpec(val def: KeyDef, val label: String, val width: Float = 1f)

public data class Row(val keys: List<KeySpec>)

public data class Layout(
    val id: String,
    val name: String,
    val rows: List<Row>,
    val language: String = name,
)
