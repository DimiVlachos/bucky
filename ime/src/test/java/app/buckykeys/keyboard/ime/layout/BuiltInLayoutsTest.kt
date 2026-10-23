package app.buckykeys.keyboard.ime.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInLayoutsTest {

    private fun asset(name: String): String = File("src/main/assets/layouts/$name.json").readText()

    @Test
    fun `the pc full asset is the layout the spike shipped`() {
        assertEquals(
            SpikeLayout.pcFullEnUs,
            LayoutParser.parse(asset("pc_full_en_us")).getOrThrow(),
        )
    }
}
