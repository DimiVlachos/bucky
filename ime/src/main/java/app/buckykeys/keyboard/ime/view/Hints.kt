package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.DefaultLabels
import app.buckykeys.keyboard.ime.layout.KeyDef

// The hint shows what holding adds beyond Shift: letters hide it, symbols show the next alternate.
internal fun hintFor(def: KeyDef): String =
    when (def) {
        is KeyDef.Char -> hintFor(def.primary, def.shifted, def.alternates)
        is KeyDef.Dead -> hintFor(def.primary, def.shifted, def.alternates)
        else -> ""
    }

private fun hintFor(primary: String, shifted: String, alternates: List<KeyDef>): String {
    val first = alternates.firstOrNull() ?: return ""
    val holdsForShifted = first is KeyDef.Char && first.primary == shifted && shifted != primary
    if (!holdsForShifted) return DefaultLabels.of(first)
    if (primary.uppercase() != primary) return ""
    return alternates.getOrNull(1)?.let(DefaultLabels::of).orEmpty()
}
