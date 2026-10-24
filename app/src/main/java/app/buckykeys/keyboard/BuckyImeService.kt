package app.buckykeys.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.Insets
import android.view.View
import android.view.inputmethod.EditorInfo
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import app.buckykeys.keyboard.ime.dispatch.InputDispatcher
import app.buckykeys.keyboard.ime.geometry.cappedRowHeightPx
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.withExtraRows
import app.buckykeys.keyboard.ime.layout.withoutLanguageKey
import app.buckykeys.keyboard.ime.view.KeyboardView
import app.buckykeys.keyboard.ime.view.LanguageChangeListener
import app.buckykeys.keyboard.ime.view.isPasswordInput
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class BuckyImeService : InputMethodService() {

    @Inject lateinit var billingRepository: BillingRepository

    @Inject lateinit var keyboardSettingsRepository: KeyboardSettingsRepository

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Main.immediate +
                CoroutineExceptionHandler { _, throwable ->
                    Timber.e(throwable, "keyboard service coroutine failed")
                },
        )

    private var keyboardView: KeyboardView? = null

    private var settings = KeyboardSettings.defaults(Locale.getDefault().language)

    private var viewSettings: KeyboardSettings? = null

    private var viewUsedFallback = false

    private var languageIndex = 0

    private val layouts = mutableMapOf<String, Layout>()

    private val languageChangeListener = LanguageChangeListener { languageIndex = it }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            billingRepository.entitlement.collect { Timber.d("entitlement: %s", it) }
        }
        serviceScope.launch {
            preload(settings)
            refreshView()
        }
        serviceScope.launch {
            keyboardSettingsRepository.settings
                .distinctUntilChanged()
                .catch { Timber.e(it, "keyboard settings failed, keeping the current ones") }
                .collect { latest ->
                    preload(latest)
                    settings = latest
                    languageIndex = languageIndex.coerceIn(0, maxOf(0, latest.languages.lastIndex))
                    refreshView()
                }
        }
    }

    override fun onCreateInputView(): View {
        val languages = languages()
        return KeyboardView(
                context = this,
                languages = languages,
                initialLanguageIndex = languageIndex,
                languageChangeListener = languageChangeListener,
                sink = InputDispatcher { currentInputConnection },
                latencyLogging = BuildConfig.DEBUG,
                rowHeightPx = rowHeightPx(languages.maxOf { it.rows.size }),
                haptics = settings.haptics,
                labelScale = settings.labelScalePercent / PERCENT,
            )
            .also {
                it.previewEnabled = previewAllowed(currentInputEditorInfo)
                keyboardView = it
                viewSettings = settings
            }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardView?.previewEnabled = previewAllowed(info)
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val view = keyboardView ?: return
        if (!isInputViewShown) return
        outInsets.contentTopInsets += view.keysTop
        outInsets.visibleTopInsets += view.keysTop
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        keyboardView?.resetState()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        keyboardView = null
        super.onDestroy()
    }

    private suspend fun preload(target: KeyboardSettings) {
        val missing = target.languages.map { it.layoutId }.distinct().filter { it !in layouts }
        if (missing.isEmpty()) return
        val loaded =
            withContext(Dispatchers.Default) {
                missing.associateWith { BuiltInLayouts.load(assets, it) }
            }
        layouts.putAll(loaded)
    }

    private fun refreshView() {
        if (keyboardView == null) return
        if (settings == viewSettings && !viewUsedFallback) return
        try {
            setInputView(onCreateInputView())
        } catch (e: RuntimeException) {
            Timber.e(e, "rebuilding the keyboard failed, keeping the current one")
        }
    }

    private fun languages(): List<Layout> {
        val cached = settings.languages.map { layouts[it.layoutId] }
        viewUsedFallback = cached.any { it == null }
        val enabled =
            cached
                .map { it ?: FallbackLayout.pcFullEnUs }
                .ifEmpty { listOf(FallbackLayout.pcFullEnUs) }
        val switchable = if (enabled.size == 1) enabled.map { it.withoutLanguageKey() } else enabled
        return switchable.map { it.withExtraRows(settings.fKeyRow, settings.extraRow) }
    }

    private fun rowHeightPx(rows: Int): Int {
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val dp = if (landscape) settings.landscapeRowHeightDp else settings.rowHeightDp
        val chosen = (dp * resources.displayMetrics.density).roundToInt()
        return cappedRowHeightPx(chosen, rows, resources.displayMetrics.heightPixels, landscape)
    }

    private fun previewAllowed(info: EditorInfo?): Boolean =
        settings.keyPreview && !isPasswordInput(info?.inputType ?: 0)

    private companion object {
        const val PERCENT = 100f
    }
}
