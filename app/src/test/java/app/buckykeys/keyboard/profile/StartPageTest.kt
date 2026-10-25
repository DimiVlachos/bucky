package app.buckykeys.keyboard.profile

import android.text.InputType
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import org.junit.Assert.assertEquals
import org.junit.Test

class StartPageTest {

    @Test
    fun `number phone and date fields open on the symbol page`() {
        listOf(
                InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_PHONE,
                InputType.TYPE_CLASS_DATETIME,
            )
            .forEach {
                assertEquals(BuiltInLayouts.PAGE_SYMBOLS, startPageFor(it))
            }
    }

    @Test
    fun `text and raw fields open on letters`() {
        assertEquals(BuiltInLayouts.PAGE_LETTERS, startPageFor(InputType.TYPE_CLASS_TEXT))
        assertEquals(BuiltInLayouts.PAGE_LETTERS, startPageFor(InputType.TYPE_NULL))
    }
}
