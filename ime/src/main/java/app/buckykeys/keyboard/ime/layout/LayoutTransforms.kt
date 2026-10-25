package app.buckykeys.keyboard.ime.layout

public fun Layout.withoutLanguageKey(): Layout = withoutSwitchKey(BuiltInLayouts.NEXT_LANGUAGE)

public fun Layout.withoutSwitchKey(targetId: String): Layout =
    copy(
        rows =
            rows.mapNotNull { row ->
                val removed = row.keys.filter { it.switchesTo(targetId) }
                if (removed.isEmpty()) return@mapNotNull row
                val kept = row.keys.filter { !it.switchesTo(targetId) }
                val widest = kept.maxByOrNull { it.width } ?: return@mapNotNull null
                val freed = removed.sumOf { it.width.toDouble() }.toFloat()
                row.copy(
                    keys =
                        kept.map { if (it === widest) it.copy(width = it.width + freed) else it },
                )
            },
    )

private fun KeySpec.switchesTo(targetId: String): Boolean =
    (def as? KeyDef.LayoutSwitch)?.targetId == targetId

public fun Layout.withSwitchTarget(from: String, to: String): Layout =
    copy(
        rows =
            rows.map { row ->
                row.copy(
                    keys =
                        row.keys.map {
                            if (it.switchesTo(from)) it.copy(def = KeyDef.LayoutSwitch(to)) else it
                        },
                )
            },
    )
