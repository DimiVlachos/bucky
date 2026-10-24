package app.buckykeys.keyboard.ime.layout

import java.text.Normalizer

internal object DeadKeys {

    private val asciiMarks = mapOf("^" to "\u0302", "`" to "\u0300", "~" to "\u0303")

    fun compose(base: String, mark: String): String? {
        val combining = combining(mark)
        if (combining.isEmpty()) return null
        val composed = Normalizer.normalize(base + combining, Normalizer.Form.NFC)
        return composed.takeIf { it != base && it.codePointCount(0, it.length) == 1 }
    }

    private fun combining(mark: String): String =
        asciiMarks[mark]
            ?: Normalizer.normalize(mark, Normalizer.Form.NFKD).filter {
                Character.getType(it) == Character.NON_SPACING_MARK.toInt()
            }
}
