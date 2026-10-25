package app.buckykeys.keyboard.profile

import android.text.InputType
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldFlagsTest {

    private val settings = KeyboardSettings()
    private val text = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

    @Test
    fun `compact text fields get both helpers and the field's enter action`() {
        assertEquals(
            FieldFlags(rawEnter = false, doubleSpacePeriod = true, autoCapitalize = true),
            fieldFlags(Profile.COMPACT, "com.whatsapp", text, settings),
        )
    }

    @Test
    fun `a forced full layout outside terminals turns both helpers off`() {
        assertEquals(
            FieldFlags(rawEnter = false, doubleSpacePeriod = false, autoCapitalize = false),
            fieldFlags(Profile.FULL, "com.example.ssh", text, settings),
        )
    }

    @Test
    fun `terminals get raw enter and no helpers`() {
        assertEquals(
            FieldFlags(rawEnter = true, doubleSpacePeriod = false, autoCapitalize = false),
            fieldFlags(Profile.FULL, "com.termux", InputType.TYPE_NULL, settings),
        )
    }

    @Test
    fun `password and no-suggestion fields get no double space period`() {
        val password = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val noSuggestions = text or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        assertEquals(
            false,
            fieldFlags(Profile.COMPACT, "com.whatsapp", password, settings).doubleSpacePeriod,
        )
        assertEquals(
            false,
            fieldFlags(Profile.COMPACT, "com.whatsapp", noSuggestions, settings).doubleSpacePeriod,
        )
    }

    @Test
    fun `a field with no input type gets no helpers`() {
        assertEquals(
            FieldFlags(rawEnter = true, doubleSpacePeriod = false, autoCapitalize = false),
            fieldFlags(Profile.COMPACT, "com.example", InputType.TYPE_NULL, settings),
        )
    }

    @Test
    fun `switched off helpers stay off`() {
        val off = settings.copy(autoCapitalize = false, doubleSpacePeriod = false)
        assertEquals(
            FieldFlags(rawEnter = false, doubleSpacePeriod = false, autoCapitalize = false),
            fieldFlags(Profile.COMPACT, "com.whatsapp", text, off),
        )
    }

    @Test
    fun `number and phone fields get no helpers`() {
        listOf(
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                InputType.TYPE_CLASS_PHONE,
                InputType.TYPE_CLASS_DATETIME,
            )
            .forEach { type ->
                val flags = fieldFlags(Profile.COMPACT, "com.example", type, settings)
                assertEquals(false, flags.autoCapitalize)
                assertEquals(false, flags.doubleSpacePeriod)
            }
    }

    @Test
    fun `email and url fields get no double space period`() {
        listOf(
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
                InputType.TYPE_TEXT_VARIATION_URI,
            )
            .forEach { variation ->
                val type = InputType.TYPE_CLASS_TEXT or variation
                assertEquals(
                    false,
                    fieldFlags(Profile.COMPACT, "com.example", type, settings).doubleSpacePeriod,
                )
            }
    }

    @Test
    fun `a raw field gets raw enter in any app`() {
        assertEquals(
            true,
            fieldFlags(Profile.FULL, "com.example.ssh", InputType.TYPE_NULL, settings).rawEnter,
        )
    }
}
