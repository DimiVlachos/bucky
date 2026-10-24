package app.buckykeys.keyboard.settings.keyboard.presentation.view_model

import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.presentation.state.KeyboardSettingsState
import app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake.FakeKeyboardSettingsRepository
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
            val viewModel = KeyboardSettingsViewModel(repository)
            backgroundScope.launch { viewModel.state.collect {} }
            assertEquals(KeyboardSettingsState(settings = defaults), viewModel.state.value)
        }

    @Test
    fun `a change is saved and shows up in the state`() =
        runTest(dispatcher) {
            val viewModel = KeyboardSettingsViewModel(repository)
            backgroundScope.launch { viewModel.state.collect {} }
            viewModel.onRowHeightChange(52)
            assertEquals(52, viewModel.state.value.settings?.rowHeightDp)
            assertEquals(52, repository.stored.value.rowHeightDp)
        }

    @Test
    fun `a failed save sets the error flag instead of crashing`() =
        runTest(dispatcher) {
            val viewModel = KeyboardSettingsViewModel(repository)
            backgroundScope.launch { viewModel.state.collect {} }
            repository.failUpdates = true
            viewModel.onHapticsChange(false)
            assertTrue(viewModel.state.value.saveFailed)
            assertEquals(defaults, viewModel.state.value.settings)
        }

    @Test
    fun `the error flag clears once shown`() =
        runTest(dispatcher) {
            val viewModel = KeyboardSettingsViewModel(repository)
            backgroundScope.launch { viewModel.state.collect {} }
            repository.failUpdates = true
            viewModel.onHapticsChange(false)
            viewModel.onSaveErrorShown()
            assertFalse(viewModel.state.value.saveFailed)
        }
}
