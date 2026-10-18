package dev.pckeys.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class CatchBlockHygieneRuleTest {

    private val rule = CatchBlockHygieneRule(Config.empty)

    @Test
    fun `broad catch in a suspend function without a guard is reported`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 } catch (e: Exception) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(1, findings.size)
    }

    @Test
    fun `broad catch inside a coroutine builder lambda is reported`() {
        val findings =
            rule.lint(
                """
                class Vm {
                    fun load() {
                        viewModelScope.launch {
                            try { work() } catch (e: Exception) { log(e) }
                        }
                    }
                }
                """
                    .trimIndent(),
            )
        assertEquals(1, findings.size)
    }

    @Test
    fun `broad catch in a plain non-suspend function is not in scope`() {
        val findings =
            rule.lint(
                """
                fun parse(s: String): Int = try { s.toInt() } catch (e: Exception) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `narrow catch that cannot swallow CancellationException is not reported`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 } catch (e: java.io.IOException) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `if guard that rethrows as the first statement passes`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    0
                }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `if guard whose then branch does not throw is reported`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 } catch (e: Exception) {
                    if (e is CancellationException) Timber.e(e, "throw away")
                    0
                }
                """
                    .trimIndent(),
            )
        assertEquals(1, findings.size)
    }

    @Test
    fun `dedicated preceding CancellationException clause that rethrows passes`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `guard or dedicated clause may do cleanup as long as it ends in a rethrow`() {
        val findings =
            rule.lint(
                """
                suspend fun a(f: Future): Int = try { 1 } catch (e: Exception) {
                    if (e is CancellationException) { f.cancel(false); throw e }
                    0
                }
                suspend fun b(f: Future): Int = try { 1 }
                    catch (e: CancellationException) { f.cancel(false); throw e }
                    catch (e: Exception) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `guard may narrow the rethrow with further conjuncts such as excluding timeouts`() {
        val findings =
            rule.lint(
                """
                suspend fun a(): Int = try { 1 } catch (e: Exception) {
                    if (e is CancellationException && e !is TimeoutCancellationException) throw e
                    0
                }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `compose effect, flow operator and lock lambdas are coroutine code`() {
        val findings =
            rule.lint(
                """
                @Composable
                fun Screen(id: Int) {
                    LaunchedEffect(id) { try { load(id) } catch (e: Exception) { log(e) } }
                }
                fun observe() {
                    flow.flatMapLatest { try { work(it) } catch (e: Exception) { log(e) } }.launchIn(scope)
                }
                fun guarded() {
                    mutex.withLock { try { work() } catch (e: Exception) { log(e) } }
                }
                """
                    .trimIndent(),
            )
        assertEquals(3, findings.size)
    }

    @Test
    fun `operators shared with collections stay out of scope in non-suspend code`() {
        val findings =
            rule.lint(
                """
                fun parse(all: List<String>) = all.mapNotNull { try { it.toInt() } catch (e: Exception) { null } }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `dedicated CancellationException clause that swallows is reported`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 }
                    catch (e: CancellationException) { }
                    catch (e: Exception) { 0 }
                """
                    .trimIndent(),
            )
        assertEquals(2, findings.size)
    }

    @Test
    fun `CancellationException clause declared after the broad clause is unreachable and reported`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 }
                    catch (e: Exception) { 0 }
                    catch (e: CancellationException) { throw e }
                """
                    .trimIndent(),
            )
        assertEquals(1, findings.size)
    }

    @Test
    fun `ensureActive as the first statement passes`() {
        val findings =
            rule.lint(
                """
                suspend fun load(): Int = try { 1 } catch (e: Exception) {
                    currentCoroutineContext().ensureActive()
                    0
                }
                """
                    .trimIndent(),
            )
        assertEquals(0, findings.size)
    }

    @Test
    fun `scope no longer depends on the file name`() {
        val content = "suspend fun load(): Int = try { 1 } catch (e: Exception) { 0 }"
        val inRepository = rule.lint(compileContentForTest(content, "UserRepositoryImpl.kt"))
        val elsewhere = rule.lint(compileContentForTest(content, "PlaybackManager.kt"))
        assertEquals(1, inRepository.size)
        assertEquals(1, elsewhere.size)
    }
}
