package app.buckykeys.keyboard.profile

import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.settings.keyboard.domain.model.FullLayoutStyle
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class FullLayoutsTest {

    private fun layout(id: String) = Layout(id, id, emptyList())

    private val all =
        listOf("pc_full_en_us", "pc_full_el", "pc_hk_en_us", "pc_hk_el", BuiltInLayouts.PC_HK_FN)
            .associateWith(::layout)

    private val languages = listOf(KeyboardLanguage.EN_US, KeyboardLanguage.EL)

    @Test
    fun `the centred style keeps the full layout ids`() {
        assertEquals(
            listOf("pc_full_en_us", "pc_full_el"),
            fullLayoutIds(languages, FullLayoutStyle.CENTRED),
        )
    }

    @Test
    fun `the classic style uses the classic ids`() {
        assertEquals(
            listOf("pc_hk_en_us", "pc_hk_el"),
            fullLayoutIds(languages, FullLayoutStyle.HK_CLASSIC),
        )
    }

    @Test
    fun `a classic layout that fails to load falls back to the centred one`() {
        val missingClassic = all - "pc_hk_el"
        assertEquals(
            "pc_full_el",
            fullLayoutFor(KeyboardLanguage.EL, FullLayoutStyle.HK_CLASSIC, missingClassic::get)?.id,
        )
    }

    @Test
    fun `nothing loaded gives no full layout`() {
        assertEquals(
            null,
            fullLayoutFor(
                KeyboardLanguage.EL,
                FullLayoutStyle.HK_CLASSIC,
                emptyMap<String, Layout>()::get,
            ),
        )
    }

    @Test
    fun `the classic style adds the fn page`() {
        assertEquals(
            mapOf(BuiltInLayouts.PAGE_FN to all.getValue(BuiltInLayouts.PC_HK_FN)),
            fullPagesFor(FullLayoutStyle.HK_CLASSIC, all::get),
        )
    }

    @Test
    fun `the centred style has no fn page`() {
        assertEquals(emptyMap<String, Layout>(), fullPagesFor(FullLayoutStyle.CENTRED, all::get))
    }

    @Test
    fun `only the classic style preloads the fn page`() {
        assertEquals(listOf(BuiltInLayouts.PC_HK_FN), fullPageIds(FullLayoutStyle.HK_CLASSIC))
        assertEquals(emptyList<String>(), fullPageIds(FullLayoutStyle.CENTRED))
    }
}
