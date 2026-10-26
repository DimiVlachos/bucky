package app.buckykeys.keyboard.ime.layout

public object FallbackLayout {

    private val alternates =
        mapOf(
            "a" to "A à á â ä ã å æ ā",
            "c" to "C ç ć č",
            "d" to "D ď ð",
            "e" to "E è é ê ë ē ę ě €",
            "g" to "G ğ",
            "i" to "I ì í î ï ī ı",
            "l" to "L ł",
            "n" to "N ñ ń ň",
            "o" to "O ò ó ô ö õ ø œ ō",
            "r" to "R ř",
            "s" to "S ś š ş ß",
            "t" to "T ť þ",
            "u" to "U ù ú û ü ū ů",
            "y" to "Y ý ÿ",
            "z" to "Z ź ż ž",
            "." to "> … ·",
            "'" to "\" ’ ‘ ´",
            "," to "< „ ‚",
            "/" to "? ÷ ¿",
            "[" to "{ « ‹",
            "]" to "} » ›",
        )

    public val pcFullEnUs: Layout =
        Layout(
            id = "pc_full_en_us",
            name = "PC Full (US)",
            language = "English (US)",
            rows =
                listOf(
                    Row(
                        chars("`~") +
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
                            special(KeyCode.BACKSPACE, "⌫", 2f),
                    ),
                    Row(
                        listOf(special(KeyCode.TAB, "Tab", 2f)) +
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
                            KeySpec(KeyDef.Char("\\", "|"), "\\"),
                    ),
                    Row(
                        listOf(modifier(ModifierKind.CTRL, "Ctrl", 2.25f)) +
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
                        listOf(modifier(ModifierKind.SHIFT, "Shift", 2.5f)) +
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
                            ) +
                            special(KeyCode.UP, "↑", 1.25f, KeyCode.PAGE_UP) +
                            special(KeyCode.FORWARD_DEL, "Del", 1.25f, KeyCode.INSERT),
                    ),
                    Row(
                        listOf(
                            special(KeyCode.ESC, "Esc", 1.25f),
                            modifier(ModifierKind.ALT, "Alt", 1.25f),
                            special(KeyCode.HOME, "Home", 1.5f),
                            special(KeyCode.END, "End", 1.5f),
                            KeySpec(KeyDef.Char(" ", " "), "", 4.5f),
                            modifier(ModifierKind.FN, "Fn", 1.25f),
                            special(KeyCode.LEFT, "←", 1.25f, KeyCode.HOME),
                            special(KeyCode.DOWN, "↓", 1.25f, KeyCode.PAGE_DOWN),
                            special(KeyCode.RIGHT, "→", 1.25f, KeyCode.END),
                        ),
                    ),
                ),
        )

    private fun chars(vararg pairs: String): List<KeySpec> = pairs.map { pair ->
        val primary = pair.substring(0, 1)
        KeySpec(KeyDef.Char(primary, pair.substring(1, 2), alternatesOf(primary)), primary)
    }

    private fun alternatesOf(primary: String): List<KeyDef> =
        alternates[primary]?.split(" ")?.map { KeyDef.Char(it, it.uppercase()) }.orEmpty()

    private fun numberRow(vararg pairs: String): List<KeySpec> = pairs.mapIndexed { index, pair ->
        val fKey = KeyDef.Special(KeyCode.entries[KeyCode.F1.ordinal + index])
        KeySpec(
            KeyDef.Char(pair.substring(0, 1), pair.substring(1, 2), listOf(fKey)),
            pair.substring(0, 1),
            fn = fKey,
        )
    }

    private fun special(
        code: KeyCode,
        label: String,
        width: Float = 1f,
        fn: KeyCode? = null,
    ): KeySpec = KeySpec(KeyDef.Special(code), label, width, fn?.let { KeyDef.Special(it) })

    private fun modifier(kind: ModifierKind, label: String, width: Float): KeySpec =
        KeySpec(KeyDef.Modifier(kind), label, width)
}
