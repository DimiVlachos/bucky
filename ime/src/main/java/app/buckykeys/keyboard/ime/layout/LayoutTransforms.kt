package app.buckykeys.keyboard.ime.layout

public fun Layout.withoutLanguageKey(): Layout =
    copy(
        rows =
            rows.mapNotNull { row ->
                val languageKeys = row.keys.filter { it.def == languageKey }
                if (languageKeys.isEmpty()) return@mapNotNull row
                val kept = row.keys.filter { it.def != languageKey }
                val widest = kept.maxByOrNull { it.width } ?: return@mapNotNull null
                val freed = languageKeys.sumOf { it.width.toDouble() }.toFloat()
                Row(kept.map { if (it === widest) it.copy(width = it.width + freed) else it })
            },
    )

private val languageKey = KeyDef.LayoutSwitch(BuiltInLayouts.NEXT_LANGUAGE)
