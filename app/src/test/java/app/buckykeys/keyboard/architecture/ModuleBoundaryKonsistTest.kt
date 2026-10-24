package app.buckykeys.keyboard.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class ModuleBoundaryKonsistTest {

    private val forbiddenInIme =
        listOf(
            "androidx.compose",
            "app.buckykeys.keyboard.billing",
            "com.android.billingclient",
            "com.google.android.play",
        )

    @Test
    fun `ime never imports compose, billing or play services`() {
        Konsist.scopeFromModule("ime").files.assertFalse(strict = false) { file ->
            file.imports.any { import -> forbiddenInIme.any { import.name.startsWith(it) } }
        }
    }

    private val forbiddenInImeRuntime =
        listOf(
            "app.buckykeys.keyboard.settings",
            "androidx.datastore",
            "dagger",
            "javax.inject",
            "kotlinx.coroutines",
            "androidx.lifecycle",
        )

    @Test
    fun `ime never imports settings, datastore, dependency injection, coroutines or lifecycle`() {
        Konsist.scopeFromModule("ime").files.assertFalse(strict = false) { file ->
            file.imports.any { import -> forbiddenInImeRuntime.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `keyboard engine imports nothing from android except KeyEvent`() {
        Konsist.scopeFromPackage("app.buckykeys.keyboard.ime.engine..", moduleName = "ime")
            .files
            .assertFalse(strict = false) { file ->
                file.imports.any { import ->
                    import.name.startsWith("android") && import.name != "android.view.KeyEvent"
                }
            }
    }

    @Test
    fun `no file uses android util Log`() {
        Konsist.scopeFromProject().files.assertFalse(strict = false) { file ->
            file.hasImport { it.name == "android.util.Log" }
        }
    }
}
