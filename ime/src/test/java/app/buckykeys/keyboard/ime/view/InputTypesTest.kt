package app.buckykeys.keyboard.ime.view

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputTypesTest {

    @Test
    fun `text password variations are passwords`() {
        listOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            )
            .forEach { variation ->
                assertTrue(isPasswordInput(InputType.TYPE_CLASS_TEXT or variation))
            }
    }

    @Test
    fun `a numeric password is a password`() {
        assertTrue(
            isPasswordInput(
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD,
            ),
        )
    }

    @Test
    fun `plain text, email and terminal-style null input are not passwords`() {
        assertFalse(isPasswordInput(InputType.TYPE_CLASS_TEXT))
        assertFalse(
            isPasswordInput(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            ),
        )
        assertFalse(isPasswordInput(InputType.TYPE_NULL))
    }

    @Test
    fun `a password variation bit on a number field is not read as a text password`() {
        assertFalse(
            isPasswordInput(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD),
        )
    }
}
