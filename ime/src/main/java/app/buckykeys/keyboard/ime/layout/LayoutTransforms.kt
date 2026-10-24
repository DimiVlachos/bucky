package app.buckykeys.keyboard.ime.layout

public fun Layout.withoutLanguageKey(): Layout =
    copy(
        rows =
            rows.map { row ->
                val languageKeys = row.keys.filter { it.def == languageKey }
                if (languageKeys.isEmpty()) return@map row
                val kept = row.keys.filter { it.def != languageKey }
                val widest = kept.maxBy { it.width }
                val freed = languageKeys.sumOf { it.width.toDouble() }.toFloat()
                Row(kept.map { if (it === widest) it.copy(width = it.width + freed) else it })
            },
    )

private val languageKey = KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE)
