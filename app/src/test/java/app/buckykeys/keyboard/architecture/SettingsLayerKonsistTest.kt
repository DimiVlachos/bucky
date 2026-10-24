package app.buckykeys.keyboard.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class SettingsLayerKonsistTest {

    private val settingsPackage = "app.buckykeys.keyboard.settings."

    private val allowedInDomain =
        listOf("kotlin.", "kotlinx.serialization.", "kotlinx.coroutines.flow.")

    private fun String.isSettingsLayer(layer: String): Boolean =
        startsWith(settingsPackage) && split('.').contains(layer)

    @Test
    fun `settings domain never imports data or presentation`() {
        Konsist.scopeFromPackage("..domain..", moduleName = "settings", sourceSetName = "main")
            .files
            .assertFalse(strict = false) { file ->
                file.imports.any { import ->
                    import.name.isSettingsLayer("data") ||
                        import.name.isSettingsLayer("presentation")
                }
            }
    }

    @Test
    fun `settings domain imports only kotlin, serialization, flow and its own domain`() {
        Konsist.scopeFromPackage("..domain..", moduleName = "settings", sourceSetName = "main")
            .files
            .assertFalse(strict = false) { file ->
                file.imports.any { import ->
                    !import.name.isSettingsLayer("domain") &&
                        allowedInDomain.none { import.name.startsWith(it) }
                }
            }
    }

    @Test
    fun `settings presentation never imports data`() {
        Konsist.scopeFromPackage(
                "..presentation..",
                moduleName = "settings",
                sourceSetName = "main",
            )
            .files
            .assertFalse(strict = false) { file ->
                file.imports.any { import -> import.name.isSettingsLayer("data") }
            }
    }
}
