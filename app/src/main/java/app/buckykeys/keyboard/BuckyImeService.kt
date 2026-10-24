package app.buckykeys.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.Insets
import android.os.Build
import android.text.InputType
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
import app.buckykeys.keyboard.ime.layout.withoutSwitchKey
import app.buckykeys.keyboard.ime.view.KeyboardActions
import app.buckykeys.keyboard.ime.view.KeyboardView
import app.buckykeys.keyboard.ime.view.LanguageChangeListener
import app.buckykeys.keyboard.ime.view.isPasswordInput
import app.buckykeys.keyboard.profile.Profile
import app.buckykeys.keyboard.profile.TERMINAL_PACKAGES
import app.buckykeys.keyboard.profile.profileFor
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

    private var viewProfile: Profile? = null

    private var viewUsedFallback = false

    private var languageIndex = 0

    private var profile = Profile.FULL

    private var lastPackage: String? = null

    private var forcedFull = false

    private val layouts = mutableMapOf<String, Layout>()

    private val dispatcher = InputDispatcher { currentInputConnection }

    private val languageChangeListener = LanguageChangeListener { languageIndex = it }

    private val actions = KeyboardActions { target ->
        when (target) {
            BuiltInLayouts.OTHER_KEYBOARD -> handOff()
            BuiltInLayouts.FULL_LAYOUT -> {
                forcedFull = true
                applyProfile(Profile.FULL)
            }
        }
    }

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
        val compact = profile == Profile.COMPACT
        val languages = if (compact) compactLanguages() else fullLanguages()
        return KeyboardView(
                context = this,
                languages = languages,
                initialLanguageIndex = languageIndex,
                languageChangeListener = languageChangeListener,
                sink = dispatcher,
                latencyLogging = BuildConfig.DEBUG,
                rowHeightPx =
                    if (compact) compactRowHeightPx()
                    else rowHeightPx(languages.maxOf { it.rows.size }),
                haptics = settings.haptics,
                labelScale = settings.labelScalePercent / PERCENT,
                pages = if (compact) compactPages() else emptyMap(),
                actions = actions,
            )
            .also {
                it.previewEnabled = previewAllowed(currentInputEditorInfo)
                keyboardView = it
                viewSettings = settings
                viewProfile = profile
            }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val packageName = info?.packageName
        if (packageName != lastPackage) {
            lastPackage = packageName
            forcedFull = false
        }
        val chosen =
            profileFor(
                packageName,
                settings.outsideTerminals,
                this.packageName,
                forcedFull,
                canHandOff(),
            )
        if (chosen == Profile.HAND_OFF && handOff()) return
        applyProfile(if (chosen == Profile.HAND_OFF) Profile.COMPACT else chosen)
        configureField(info)
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd,
        )
        updateAutoShift()
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

    private fun applyProfile(next: Profile) {
        profile = next
        if (keyboardView == null || viewProfile == profile) {
            keyboardView?.resetState()
            return
        }
        rebuildView()
    }

    private fun configureField(info: EditorInfo?) {
        val inputType = info?.inputType ?: 0
        dispatcher.rawEnter = info?.packageName in TERMINAL_PACKAGES
        dispatcher.imeOptions = info?.imeOptions ?: 0
        dispatcher.inputType = inputType
        dispatcher.doubleSpacePeriod =
            profile == Profile.COMPACT &&
                settings.doubleSpacePeriod &&
                !isPasswordInput(inputType) &&
                inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS == 0
        keyboardView?.previewEnabled = previewAllowed(info)
        updateAutoShift()
    }

    private fun updateAutoShift() {
        val view = keyboardView ?: return
        val inputType = currentInputEditorInfo?.inputType ?: 0
        val on =
            profile == Profile.COMPACT &&
                settings.autoCapitalize &&
                inputType != 0 &&
                (currentInputConnection?.getCursorCapsMode(inputType) ?: 0) != 0
        view.setAutoShift(on)
    }

    private fun canHandOff(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && shouldOfferSwitchingToNextInputMethod()

    private fun handOff(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && switchToPreviousInputMethod()

    private suspend fun preload(target: KeyboardSettings) {
        val wanted =
            target.languages.flatMap { listOf(it.layoutId, it.compactLayoutId) } +
                BuiltInLayouts.COMPACT_SYMBOLS +
                BuiltInLayouts.COMPACT_SYMBOLS_MORE
        val missing = wanted.distinct().filter { it !in layouts }
        if (missing.isEmpty()) return
        val loaded =
            withContext(Dispatchers.Default) {
                missing.associateWith { BuiltInLayouts.load(assets, it) }
            }
        layouts.putAll(loaded)
    }

    private fun refreshView() {
        if (keyboardView == null) return
        if (settings == viewSettings && profile == viewProfile && !viewUsedFallback) return
        rebuildView()
    }

    private fun rebuildView() {
        try {
            setInputView(onCreateInputView())
        } catch (e: RuntimeException) {
            Timber.e(e, "rebuilding the keyboard failed, keeping the current one")
        }
    }

    private fun fullLanguages(): List<Layout> {
        val switchable = switchable(settings.languages.map { layouts[it.layoutId] })
        return switchable.map { it.withExtraRows(settings.fKeyRow, settings.extraRow) }
    }

    private fun compactLanguages(): List<Layout> {
        val switchable = switchable(settings.languages.map { layouts[it.compactLayoutId] })
        return if (canHandOff()) switchable else switchable.map { it.withoutKeyboardKey() }
    }

    private fun compactPages(): Map<String, Layout> =
        mapOf(
                BuiltInLayouts.PAGE_SYMBOLS to layouts[BuiltInLayouts.COMPACT_SYMBOLS],
                BuiltInLayouts.PAGE_SYMBOLS_MORE to layouts[BuiltInLayouts.COMPACT_SYMBOLS_MORE],
            )
            .mapNotNull { (page, layout) -> layout?.let { page to it } }
            .toMap()
            .mapValues { (_, layout) -> if (canHandOff()) layout else layout.withoutKeyboardKey() }

    private fun Layout.withoutKeyboardKey(): Layout =
        withoutSwitchKey(BuiltInLayouts.OTHER_KEYBOARD)

    private fun switchable(cached: List<Layout?>): List<Layout> {
        viewUsedFallback = cached.any { it == null }
        val enabled =
            cached
                .map { it ?: FallbackLayout.pcFullEnUs }
                .ifEmpty { listOf(FallbackLayout.pcFullEnUs) }
        return if (enabled.size == 1) enabled.map { it.withoutLanguageKey() } else enabled
    }

    private fun chosenRowHeightPx(): Int {
        val dp = if (isLandscape()) settings.landscapeRowHeightDp else settings.rowHeightDp
        return (dp * resources.displayMetrics.density).roundToInt()
    }

    private fun rowHeightPx(rows: Int): Int =
        cappedRowHeightPx(
            chosenRowHeightPx(),
            rows,
            resources.displayMetrics.heightPixels,
            isLandscape(),
        )

    private fun compactRowHeightPx(): Int =
        cappedRowHeightPx(
            chosenRowHeightPx() * FULL_MAIN_ROWS / COMPACT_ROWS,
            COMPACT_ROWS,
            resources.displayMetrics.heightPixels,
            isLandscape(),
        )

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun previewAllowed(info: EditorInfo?): Boolean =
        settings.keyPreview && !isPasswordInput(info?.inputType ?: 0)

    private companion object {
        const val PERCENT = 100f
        const val FULL_MAIN_ROWS = 5
        const val COMPACT_ROWS = 4
    }
}
