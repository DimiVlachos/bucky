package app.buckykeys.keyboard.settings.keyboard.presentation.view_model

import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import app.buckykeys.keyboard.settings.keyboard.presentation.state.KeyboardSettingsState
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake.FakeExtraRowInterpreter
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake.FakeKeyboardSettingsRepository
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake.FakeTouchOffsetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardSettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val defaults = KeyboardSettings.defaults(systemLanguage = "en")
    private val repository = FakeKeyboardSettingsRepository(defaults)
    private val touchOffsets = FakeTouchOffsetRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the state carries the stored settings`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            assertEquals(KeyboardSettingsState(settings = defaults), viewModel.state.value)
        }

    @Test
    fun `a change is saved and shows up in the state`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onRowHeightChange(52)
            assertEquals(52, viewModel.state.value.settings?.rowHeightDp)
            assertEquals(52, repository.stored.value.rowHeightDp)
        }

    @Test
    fun `a label size change is saved`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onLabelScaleChange(120)
            assertEquals(120, repository.stored.value.labelScalePercent)
        }

    @Test
    fun `an outside terminals change is saved`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onOutsideTerminalsChange(OutsideTerminals.FULL)
            viewModel.onAutoCapitalizeChange(false)
            viewModel.onDoubleSpacePeriodChange(false)
            assertEquals(OutsideTerminals.FULL, repository.stored.value.outsideTerminals)
            assertEquals(false, repository.stored.value.autoCapitalize)
            assertEquals(false, repository.stored.value.doubleSpacePeriod)
        }

    @Test
    fun `an fn toggle change is saved`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onFnTogglesChange(true)
            assertEquals(true, repository.stored.value.fnToggles)
        }

    @Test
    fun `a failed save sets the error flag instead of crashing`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            repository.failUpdates = true
            viewModel.onHapticsChange(false)
            assertTrue(viewModel.state.value.saveFailed)
            assertEquals(defaults, viewModel.state.value.settings)
        }

    @Test
    fun `the error flag clears once shown`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            repository.failUpdates = true
            viewModel.onHapticsChange(false)
            viewModel.onSaveErrorShown()
            assertFalse(viewModel.state.value.saveFailed)
        }

    @Test
    fun `the extra row preview splits the text into words for the interpreter`() {
        val interpreter = FakeExtraRowInterpreter()
        val viewModel = KeyboardSettingsViewModel(repository, interpreter, touchOffsets)
        viewModel.previewExtraRow("  ESC   git ")
        assertEquals(listOf("ESC", "git"), interpreter.lastWords)
    }

    @Test
    fun `nothing learned yet shows no correction to reset`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            assertFalse(viewModel.state.value.touchCorrectionLearned)
        }

    @Test
    fun `a learned offset shows a correction to reset`() =
        runTest(dispatcher) {
            touchOffsets.stored.value = TouchOffset(x = -0.1f, y = 0.2f)
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            assertTrue(viewModel.state.value.touchCorrectionLearned)
        }

    @Test
    fun `reset clears the learned offset`() =
        runTest(dispatcher) {
            touchOffsets.stored.value = TouchOffset(x = -0.1f, y = 0.2f)
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onTouchCorrectionReset()
            assertEquals(TouchOffset.NONE, touchOffsets.stored.value)
            assertFalse(viewModel.state.value.touchCorrectionLearned)
        }

    @Test
    fun `a reset that fails to save reports it`() =
        runTest(dispatcher) {
            touchOffsets.failSaves = true
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onTouchCorrectionReset()
            assertTrue(viewModel.state.value.saveFailed)
        }

    private fun viewModel(): KeyboardSettingsViewModel =
        KeyboardSettingsViewModel(repository, FakeExtraRowInterpreter(), touchOffsets)
}
