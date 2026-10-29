package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.KeyDef

internal fun isSpecialKey(def: KeyDef): Boolean =
    def is KeyDef.Special || def is KeyDef.Modifier || def is KeyDef.LayoutSwitch || isSpaceBar(def)

internal fun isSpaceBar(def: KeyDef): Boolean = def is KeyDef.Char && def.primary == " "
