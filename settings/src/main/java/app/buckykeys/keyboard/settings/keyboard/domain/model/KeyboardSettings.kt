package app.buckykeys.keyboard.settings.keyboard.domain.model

import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
public data class KeyboardSettings(
    @Serializable(with = KeyboardLanguageListSerializer::class)
    val languages: List<KeyboardLanguage> = listOf(KeyboardLanguage.EN_US),
    val rowHeightDp: Int = DEFAULT_ROW_HEIGHT_DP,
    val landscapeRowHeightDp: Int = DEFAULT_LANDSCAPE_ROW_HEIGHT_DP,
    val labelScalePercent: Int = DEFAULT_LABEL_SCALE_PERCENT,
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

    public fun withLabelScale(percent: Int): KeyboardSettings =
        copy(labelScalePercent = snapLabelScale(percent))

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
            labelScalePercent = snapLabelScale(labelScalePercent),
            extraRow = extraRow.filter { it.isNotBlank() }.take(MAX_EXTRA_ROW_WORDS),
        )

    public companion object {
        public const val DEFAULT_ROW_HEIGHT_DP: Int = 48
        public const val DEFAULT_LANDSCAPE_ROW_HEIGHT_DP: Int = 34
        public val ROW_HEIGHT_RANGE: IntRange = 36..64
        public val LANDSCAPE_ROW_HEIGHT_RANGE: IntRange = 26..56
        public const val DEFAULT_LABEL_SCALE_PERCENT: Int = 100
        public val LABEL_SCALE_RANGE: IntRange = 80..140
        public const val LABEL_SCALE_STEP: Int = 10
        public const val MAX_EXTRA_ROW_WORDS: Int = 64
        public const val TERMUX_DEFAULT_EXTRA_ROW: String =
            "ESC / - HOME UP END PGUP TAB CTRL ALT LEFT DOWN RIGHT PGDN"
        private val WHITESPACE = Regex("\\s+")

        private fun snapLabelScale(percent: Int): Int =
            ((percent / LABEL_SCALE_STEP.toFloat()).roundToInt() * LABEL_SCALE_STEP).coerceIn(
                LABEL_SCALE_RANGE,
            )

        public fun defaults(systemLanguage: String): KeyboardSettings =
            KeyboardSettings(
                languages =
                    if (systemLanguage == "el") listOf(KeyboardLanguage.EN_US, KeyboardLanguage.EL)
                    else listOf(KeyboardLanguage.EN_US),
            )
    }
}
