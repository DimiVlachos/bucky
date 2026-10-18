package dev.pckeys.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class HardcodedColorRuleTest {

    private val rule = HardcodedColorRule(Config.empty)

    @Test
    fun `hex color literal is reported`() {
        assertEquals(1, rule.lint("val c = Color(0xFF112233)").size)
    }

    @Test
    fun `named color is reported`() {
        assertEquals(1, rule.lint("val c = Color.White").size)
    }

    @Test
    fun `named color used as an alpha overlay is allowed`() {
        assertEquals(0, rule.lint("val c = Color.Black.copy(alpha = 0.5f)").size)
    }

    @Test
    fun `theme token is not reported`() {
        assertEquals(0, rule.lint("val c = PcKeysTheme.colors.textPrimary").size)
    }
}
