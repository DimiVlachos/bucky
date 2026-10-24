package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlinx.serialization.Serializable

@Serializable
public data class KeyboardSettings(
    @Serializable(with = KeyboardLanguageListSerializer::class)
    val languages: List<KeyboardLanguage> = listOf(KeyboardLanguage.EN_US),
    val rowHeightDp: Int = DEFAULT_ROW_HEIGHT_DP,
    val landscapeRowHeightDp: Int = DEFAULT_LANDSCAPE_ROW_HEIGHT_DP,
    val keyPreview: Boolean = true,
    val haptics: Boolean = true,
    val fKeyRow: Boolean = false,
    val extraRow: List<String> = emptyList(),
) {

    public fun withExtraRow(text: String): KeyboardSettings =
        copy(extraRow = text.split(WHITESPACE).filter { it.isNotEmpty() }.take(MAX_EXTRA_ROW_WORDS))

    public fun withLanguage(language: KeyboardLanguage, enabled: Boolean): KeyboardSettings {
        val updated = if (enabled) (languages + language).distinct() else languages - language
        return if (updated.isEmpty()) this else copy(languages = updated)
    }

    public fun withRowHeight(dp: Int): KeyboardSettings =
        copy(rowHeightDp = dp.coerceIn(ROW_HEIGHT_RANGE))

    public fun withLandscapeRowHeight(dp: Int): KeyboardSettings =
        copy(landscapeRowHeightDp = dp.coerceIn(LANDSCAPE_ROW_HEIGHT_RANGE))

    public fun sanitized(
        fallbackLanguages: List<KeyboardLanguage> = listOf(KeyboardLanguage.EN_US),
    ): KeyboardSettings =
        copy(
            languages =
                languages.distinct().ifEmpty {
                    fallbackLanguages.distinct().ifEmpty { listOf(KeyboardLanguage.EN_US) }
                },
            rowHeightDp = rowHeightDp.coerceIn(ROW_HEIGHT_RANGE),
            landscapeRowHeightDp = landscapeRowHeightDp.coerceIn(LANDSCAPE_ROW_HEIGHT_RANGE),
            extraRow = extraRow.filter { it.isNotBlank() }.take(MAX_EXTRA_ROW_WORDS),
        )

    public companion object {
        public const val DEFAULT_ROW_HEIGHT_DP: Int = 48
        public const val DEFAULT_LANDSCAPE_ROW_HEIGHT_DP: Int = 34
        public val ROW_HEIGHT_RANGE: IntRange = 36..64
        public val LANDSCAPE_ROW_HEIGHT_RANGE: IntRange = 26..56
        public const val MAX_EXTRA_ROW_WORDS: Int = 64
        private val WHITESPACE = Regex("\\s+")

        public fun defaults(systemLanguage: String): KeyboardSettings =
            KeyboardSettings(
                languages =
                    if (systemLanguage == "el") listOf(KeyboardLanguage.EN_US, KeyboardLanguage.EL)
                    else listOf(KeyboardLanguage.EN_US),
            )
    }
}
