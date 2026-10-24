package app.buckykeys.keyboard.settings

import app.buckykeys.keyboard.ime.layout.LayoutParser
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage
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
}
