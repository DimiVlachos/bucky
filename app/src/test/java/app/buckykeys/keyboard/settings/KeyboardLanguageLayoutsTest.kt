package app.buckykeys.keyboard.settings

import app.buckykeys.keyboard.ime.layout.LayoutParser
import app.buckykeys.keyboard.ime.layout.parseExtraRow
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardLanguageLayoutsTest {

    @Test
    fun `every settings language names a built-in layout that parses`() {
        KeyboardLanguage.entries.forEach { language ->
            val file = File("../ime/src/main/assets/layouts/${language.layoutId}.json")
            assertEquals(
                language.name,
                language.layoutId,
                LayoutParser.parse(file.readText()).getOrThrow().id,
            )
        }
    }

    @Test
    fun `the termux default extra row is fourteen valid keys`() {
        val parsed = parseExtraRow(KeyboardSettings.TERMUX_DEFAULT_EXTRA_ROW.split(" "))
        assertEquals(emptyList<String>(), parsed.unknown)
        assertEquals(14, parsed.keys.size)
        assertEquals(false, parsed.overLimit)
    }
}
