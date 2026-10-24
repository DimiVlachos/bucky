package app.buckykeys.keyboard.settings.keyboard.data.repository

import app.buckykeys.keyboard.settings.keyboard.data.repository.fake.FakeKeyboardSettingsDataStore
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardSettingsRepositoryImplTest {

    private val defaults = KeyboardSettings.defaults(systemLanguage = "en")
    private val stored = defaults.withRowHeight(52)
    private val dataStore = FakeKeyboardSettingsDataStore(stored)
    private val repository = KeyboardSettingsRepositoryImpl(dataStore, defaults)

    @Test
    fun `a read error emits the defaults and later values still arrive`() = runTest {
        dataStore.readFailuresLeft = 1
        val values = mutableListOf<KeyboardSettings>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.settings.toList(values)
        }
        advanceTimeBy(RETRY_WAIT_MS)
        runCurrent()
        val updated = stored.copy(haptics = false)
        repository.update { updated }
        runCurrent()
        assertEquals(listOf(defaults, stored, updated), values)
    }

    @Test
    fun `updates reach the stored settings`() = runTest {
        repository.update { it.copy(fKeyRow = true) }
        assertEquals(stored.copy(fKeyRow = true), dataStore.stored.value)
    }

    private companion object {
        const val RETRY_WAIT_MS = 2_000L
    }
}
