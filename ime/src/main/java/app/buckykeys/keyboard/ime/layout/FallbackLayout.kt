package app.buckykeys.keyboard.ime.layout

public object FallbackLayout {

    public val pcFullEnUs: Layout =
        Layout(
            id = "pc_full_en_us",
            name = "PC Full (US)",
            rows =
                listOf(
                    Row(
                        listOf(special(KeyCode.ESC, "Esc", 1.25f)) +
                            numberRow(
                                "1!",
                                "2@",
                                "3#",
                                "4$",
                                "5%",
                                "6^",
                                "7&",
                                "8*",
                                "9(",
                                "0)",
                                "-_",
                                "=+",
                            ) +
                            special(KeyCode.BACKSPACE, "⌫", 1.75f),
                    ),
                    Row(
                        listOf(special(KeyCode.TAB, "Tab", 1.5f)) +
                            chars(
                                "qQ",
                                "wW",
                                "eE",
                                "rR",
                                "tT",
                                "yY",
                                "uU",
                                "iI",
                                "oO",
                                "pP",
                                "[{",
                                "]}",
                            ) +
                            KeySpec(KeyDef.Char("\\", "|"), "\\", 1.5f),
                    ),
                    Row(
                        listOf(modifier(ModifierKind.CTRL, "Ctrl", 1.75f)) +
                            chars(
                                "aA",
                                "sS",
                                "dD",
                                "fF",
                                "gG",
                                "hH",
                                "jJ",
                                "kK",
                                "lL",
                                ";:",
                                "'\"",
                            ) +
                            special(KeyCode.ENTER, "Enter", 2.25f),
                    ),
                    Row(
                        listOf(modifier(ModifierKind.SHIFT, "Shift", 1.5f)) +
                            chars(
                                "zZ",
                                "xX",
                                "cC",
                                "vV",
                                "bB",
                                "nN",
                                "mM",
                                ",<",
                                ".>",
                                "/?",
                                "`~",
                            ) +
                            special(KeyCode.UP, "↑", 1.25f) +
                            special(KeyCode.FORWARD_DEL, "Del", 1.25f),
                    ),
                    Row(
                        listOf(
                            modifier(ModifierKind.ALT, "Alt", 1.5f),
                            KeySpec(KeyDef.Char(" ", " "), "", 6.75f),
                            special(KeyCode.HOME, "Home", 1.5f),
                            special(KeyCode.END, "End", 1.5f),
                            special(KeyCode.LEFT, "←", 1.25f),
                            special(KeyCode.DOWN, "↓", 1.25f),
                            special(KeyCode.RIGHT, "→", 1.25f),
                        ),
                    ),
                ),
        )

    private fun chars(vararg pairs: String): List<KeySpec> = pairs.map { pair ->
        KeySpec(KeyDef.Char(pair.substring(0, 1), pair.substring(1, 2)), pair.substring(0, 1))
    }

    private fun numberRow(vararg pairs: String): List<KeySpec> = pairs.mapIndexed { index, pair ->
        val fKey = KeyDef.Special(KeyCode.entries[KeyCode.F1.ordinal + index])
        KeySpec(
            KeyDef.Char(pair.substring(0, 1), pair.substring(1, 2), listOf(fKey)),
            pair.substring(0, 1),
        )
    }

    private fun special(code: KeyCode, label: String, width: Float = 1f): KeySpec =
        KeySpec(KeyDef.Special(code), label, width)

    private fun modifier(kind: ModifierKind, label: String, width: Float): KeySpec =
        KeySpec(KeyDef.Modifier(kind), label, width)
}
