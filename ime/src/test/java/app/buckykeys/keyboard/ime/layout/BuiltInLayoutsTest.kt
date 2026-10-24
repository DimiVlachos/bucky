package app.buckykeys.keyboard.ime.layout

import java.io.File
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInLayoutsTest {

    private fun asset(id: String): String = File("src/main/assets/layouts/$id.json").readText()

    @Test
    fun `the pc full asset matches the fallback layout`() {
        assertEquals(
            FallbackLayout.pcFullEnUs,
            LayoutParser.parse(asset(BuiltInLayouts.PC_FULL_EN_US)).getOrThrow(),
        )
    }

    @Test
    fun `a layout that parses is used as it is`() {
        val json = asset(BuiltInLayouts.PC_FULL_EN_US).replace("PC Full (US)", "From the asset")

        assertEquals("From the asset", BuiltInLayouts.loadOrFallback { json }.name)
    }

    @Test
    fun `a layout that fails to parse falls back to the built-in layout`() {
        assertEquals(FallbackLayout.pcFullEnUs, BuiltInLayouts.loadOrFallback { """{ "id": """ })
    }

    @Test
    fun `a missing asset falls back to the built-in layout`() {
        assertEquals(
            FallbackLayout.pcFullEnUs,
            BuiltInLayouts.loadOrFallback { throw IOException("layouts/pc_full_en_us.json") },
        )
    }
}
