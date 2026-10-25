package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.DefaultLabels
import app.buckykeys.keyboard.ime.layout.KeyDef

// The hint shows what holding adds beyond Shift: letters hide it, symbols show the next alternate.
internal fun hintFor(def: KeyDef): String {
    if (def !is KeyDef.Char) return ""
    val first = def.alternates.firstOrNull() ?: return ""
    val holdsForShifted =
        first is KeyDef.Char && first.primary == def.shifted && def.shifted != def.primary
    if (!holdsForShifted) return DefaultLabels.of(first)
    if (def.primary.uppercase() != def.primary) return ""
    return def.alternates.getOrNull(1)?.let(DefaultLabels::of).orEmpty()
}
