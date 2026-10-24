package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.DefaultLabels
import app.buckykeys.keyboard.ime.layout.KeyDef

internal fun hintFor(def: KeyDef): String {
    if (def !is KeyDef.Char) return ""
    val first = def.alternates.firstOrNull() ?: return ""
    if (first is KeyDef.Char && first.primary == def.shifted && def.shifted != def.primary)
        return ""
    return DefaultLabels.of(first)
}
