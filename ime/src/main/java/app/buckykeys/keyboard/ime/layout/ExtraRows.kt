package app.buckykeys.keyboard.ime.layout

public fun Layout.withExtraRows(fKeyRow: Boolean, userKeys: List<String>): Layout {
    val user = userKeys.mapNotNull(::userKey).take(MAX_USER_KEYS)
    val extra =
        listOfNotNull(if (fKeyRow) fKeys else null, if (user.isEmpty()) null else sharedRow(user))
    return if (extra.isEmpty()) this else copy(rows = extra + rows)
}

private const val ROW_UNITS = 15f
private const val MAX_USER_KEYS = 15

private val termuxAliases =
    mapOf(
        "PGUP" to KeyCode.PAGE_UP,
        "PGDN" to KeyCode.PAGE_DOWN,
        "BKSP" to KeyCode.BACKSPACE,
        "DEL" to KeyCode.FORWARD_DEL,
        "INS" to KeyCode.INSERT,
    )

private val fKeys: Row =
    sharedRow(
        KeyCode.entries
            .filter { it.ordinal in KeyCode.F1.ordinal..KeyCode.F12.ordinal }
            .map(KeyDef::Special),
    )

private fun userKey(token: String): KeyDef? {
    val name = token.trim().uppercase()
    if (name.isEmpty()) return null
    termuxAliases[name]?.let {
        return KeyDef.Special(it)
    }
    KeyCode.entries
        .firstOrNull { it.name == name }
        ?.let {
            return KeyDef.Special(it)
        }
    ModifierKind.entries
        .firstOrNull { it.name == name }
        ?.let {
            return KeyDef.Modifier(it)
        }
    val text = token.trim()
    return if (text.codePointCount(0, text.length) == 1) KeyDef.Char(text, text) else null
}

private fun sharedRow(defs: List<KeyDef>): Row =
    Row(defs.map { KeySpec(it, DefaultLabels.of(it), ROW_UNITS / defs.size) })
