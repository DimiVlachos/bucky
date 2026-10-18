package app.buckykeys.keyboard.ime.layout

public object SpikeLayout {

    public val pcFullEnUs: Layout =
        Layout(
            id = "pc_full_en_us",
            name = "PC Full (US)",
            rows =
                listOf(
                    Row(
                        listOf(special(KeyCode.ESC, "Esc")) +
                            chars(
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
                            special(KeyCode.BACKSPACE, "⌫", 1.5f),
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
                            ),
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
                            special(KeyCode.ENTER, "Enter", 1.75f),
                    ),
                    Row(
                        listOf(modifier(ModifierKind.SHIFT, "Shift", 2f)) +
                            chars("zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?") +
                            special(KeyCode.UP, "↑"),
                    ),
                    Row(
                        listOf(
                            modifier(ModifierKind.ALT, "Alt", 1.5f),
                            KeySpec(KeyDef.Char(" ", " "), "", 6f),
                            special(KeyCode.HOME, "Home"),
                            special(KeyCode.END, "End"),
                            special(KeyCode.LEFT, "←"),
                            special(KeyCode.DOWN, "↓"),
                            special(KeyCode.RIGHT, "→"),
                        ),
                    ),
                ),
        )

    private fun chars(vararg pairs: String): List<KeySpec> = pairs.map { pair ->
        KeySpec(KeyDef.Char(pair.substring(0, 1), pair.substring(1, 2)), pair.substring(0, 1))
    }

    private fun special(code: KeyCode, label: String, width: Float = 1f): KeySpec =
        KeySpec(KeyDef.Special(code), label, width)

    private fun modifier(kind: ModifierKind, label: String, width: Float): KeySpec =
        KeySpec(KeyDef.Modifier(kind), label, width)
}
