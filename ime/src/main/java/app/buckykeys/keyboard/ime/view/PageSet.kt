package app.buckykeys.keyboard.ime.view

import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.Layout

public class PageSet(letters: List<Layout>, private val pages: Map<String, Layout>) {

    private val letters: List<Layout> = letters.ifEmpty { listOf(FallbackLayout.pcFullEnUs) }

    public val languageCount: Int
        get() = letters.size

    public fun current(languageIndex: Int, page: String): Layout {
        val language = letters[languageIndex.coerceIn(0, letters.lastIndex)]
        return if (page == BuiltInLayouts.PAGE_LETTERS) language else pages[page] ?: language
    }

    public fun isPage(target: String): Boolean =
        target == BuiltInLayouts.PAGE_LETTERS || target in pages
}
