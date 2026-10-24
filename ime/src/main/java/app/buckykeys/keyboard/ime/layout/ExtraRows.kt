package app.buckykeys.keyboard.ime.layout

import java.text.BreakIterator

public fun parseExtraRow(userKeys: List<String>): ExtraRowParse {
    val items = userKeys.flatMap(::termuxItems)
    val accepted = items.mapNotNull(::userKey)
    val unknown = items.filter { userKey(it) == null }
    val kept = accepted.take(MAX_USER_KEYS)
    val keys = if (kept.isEmpty()) emptyList() else sharedRow(kept).keys
    return ExtraRowParse(keys = keys, unknown = unknown, overLimit = accepted.size > MAX_USER_KEYS)
}

public fun Layout.withExtraRows(fKeyRow: Boolean, userKeys: List<String>): Layout {
    val user = parseExtraRow(userKeys).keys
    val extra = listOfNotNull(if (fKeyRow) fKeys else null, if (user.isEmpty()) null else Row(user))
    return if (extra.isEmpty()) this else copy(rows = extra + rows)
}

private const val ROW_UNITS = 15f
private const val MAX_USER_KEYS = 15
private const val SPACE_LABEL = "␣"
private const val TERMUX_PUNCTUATION = "[]'\""

private val whitespace = Regex("\\s+")
private val quoted = Regex("'([^']+)'|\"([^\"]+)\"")

private val termuxAliases: Map<String, KeyDef> =
    mapOf(
        "ESCAPE" to KeyDef.Special(KeyCode.ESC),
        "CONTROL" to KeyDef.Modifier(ModifierKind.CTRL),
        "RETURN" to KeyDef.Special(KeyCode.ENTER),
        "DELETE" to KeyDef.Special(KeyCode.FORWARD_DEL),
        "DEL" to KeyDef.Special(KeyCode.FORWARD_DEL),
        "PAGEUP" to KeyDef.Special(KeyCode.PAGE_UP),
        "PGUP" to KeyDef.Special(KeyCode.PAGE_UP),
        "PAGEDOWN" to KeyDef.Special(KeyCode.PAGE_DOWN),
        "PGDOWN" to KeyDef.Special(KeyCode.PAGE_DOWN),
        "PGDN" to KeyDef.Special(KeyCode.PAGE_DOWN),
        "BKSP" to KeyDef.Special(KeyCode.BACKSPACE),
        "INS" to KeyDef.Special(KeyCode.INSERT),
        "BACKSLASH" to KeyDef.Char("\\", "\\"),
        "QUOTE" to KeyDef.Char("\"", "\""),
        "APOSTROPHE" to KeyDef.Char("'", "'"),
    )

private val fKeys: Row =
    sharedRow(
        KeyCode.entries
            .filter { it.ordinal in KeyCode.F1.ordinal..KeyCode.F12.ordinal }
            .map(KeyDef::Special),
    )

private fun termuxItems(entry: String): List<String> =
    entry.split(whitespace).flatMap { word ->
        when {
            word.isEmpty() -> emptyList()
            graphemeCount(word) == 1 -> listOf(word)
            quoted.containsMatchIn(word) ->
                quoted
                    .findAll(word)
                    .map { it.groupValues[1].ifEmpty { it.groupValues[2] } }
                    .toList()
            else ->
                word
                    .split(',')
                    .map { it.trim { c -> c in TERMUX_PUNCTUATION } }
                    .filter { it.isNotEmpty() }
        }
    }

private fun userKey(token: String): KeyDef? {
    val name = token.uppercase()
    if (name == "SPACE") return KeyDef.Char(" ", " ")
    termuxAliases[name]?.let {
        return it
    }
    KeyCode.entries
        .firstOrNull { it.name == name }
        ?.let {
            return KeyDef.Special(it)
        }
    ModifierKind.entries
        .firstOrNull { it.name == name }
        ?.let {
            return if (it == ModifierKind.FN) null else KeyDef.Modifier(it)
        }
    if (graphemeCount(token) != 1) return null
    val upper = token.uppercase()
    return KeyDef.Char(token, if (graphemeCount(upper) == 1) upper else token)
}

private fun graphemeCount(text: String): Int {
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(text)
    var count = 0
    while (iterator.next() != BreakIterator.DONE) count++
    return count
}

private fun sharedRow(defs: List<KeyDef>): Row =
    Row(defs.map { KeySpec(it, label(it), ROW_UNITS / defs.size) })

private fun label(def: KeyDef): String =
    if (def is KeyDef.Char && def.primary == " ") SPACE_LABEL else DefaultLabels.of(def)
