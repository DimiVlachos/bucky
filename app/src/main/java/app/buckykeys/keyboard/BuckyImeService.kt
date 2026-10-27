package app.buckykeys.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.Insets
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import app.buckykeys.keyboard.ime.dispatch.InputDispatcher
import app.buckykeys.keyboard.ime.geometry.cappedRowHeightPx
import app.buckykeys.keyboard.ime.geometry.compactRowHeightPx
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.withExtraRows
import app.buckykeys.keyboard.ime.layout.withSwitchTarget
import app.buckykeys.keyboard.ime.layout.withoutLanguageKey
import app.buckykeys.keyboard.ime.view.KeyboardActions
import app.buckykeys.keyboard.ime.view.KeyboardView
import app.buckykeys.keyboard.ime.view.LanguageChangeListener
import app.buckykeys.keyboard.ime.view.isPasswordInput
import app.buckykeys.keyboard.profile.Profile
import app.buckykeys.keyboard.profile.fieldFlags
import app.buckykeys.keyboard.profile.fullLayoutFor
import app.buckykeys.keyboard.profile.fullLayoutIds
import app.buckykeys.keyboard.profile.fullPageIds
import app.buckykeys.keyboard.profile.fullPagesFor
import app.buckykeys.keyboard.profile.profileFor
import app.buckykeys.keyboard.profile.startPageFor
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
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

    private var profile = Profile.FULL

    private var lastPackage: String? = null

    private var forcedFull = false

    private var autoCapitalizeField = false

    private val layouts = ImeSession.layouts

    private val dispatcher = InputDispatcher { currentInputConnection }

    private val languageChangeListener = LanguageChangeListener { ImeSession.languageIndex = it }

    private val actions = KeyboardActions { target ->
        when (target) {
            BuiltInLayouts.OTHER_KEYBOARD -> handOff()
            BuiltInLayouts.FULL_LAYOUT -> {
                forcedFull = true
                applyProfile(Profile.FULL)
                configureField(currentInputEditorInfo)
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
                    ImeSession.languageIndex =
                        ImeSession.languageIndex.coerceIn(0, maxOf(0, latest.languages.lastIndex))
                    if (isInputViewShown) startField(currentInputEditorInfo) else refreshView()
                }
        }
    }

    override fun onCreateInputView(): View = createView().also(::adopt)

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        startField(info)
    }

    private fun startField(info: EditorInfo?) {
        val packageName = info?.packageName
        if (packageName != lastPackage) {
            lastPackage = packageName
            forcedFull = false
            if (packageName != ImeSession.handedOffFrom) ImeSession.handedOffFrom = null
        }
        val chosen =
            profileFor(
                packageName,
                settings.outsideTerminals,
                this.packageName,
                forcedFull,
                settings.outsideTerminals == OutsideTerminals.OTHER_KEYBOARD && canHandOff(),
                ImeSession.handedOffFrom,
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
        if (autoCapitalizeField) updateAutoShift()
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
        refreshView()
    }

    private fun configureField(info: EditorInfo?) {
        val inputType = info?.inputType ?: 0
        val flags = fieldFlags(profile, info?.packageName, inputType, settings)
        dispatcher.rawEnter = flags.rawEnter
        dispatcher.imeOptions = info?.imeOptions ?: 0
        dispatcher.inputType = inputType
        dispatcher.doubleSpacePeriod = flags.doubleSpacePeriod
        autoCapitalizeField = flags.autoCapitalize
        keyboardView?.previewEnabled = previewAllowed(info)
        if (profile == Profile.COMPACT) keyboardView?.showPage(startPageFor(inputType))
        updateAutoShift()
    }

    private fun updateAutoShift() {
        val view = keyboardView ?: return
        val inputType = currentInputEditorInfo?.inputType ?: 0
        val on =
            autoCapitalizeField && (currentInputConnection?.getCursorCapsMode(inputType) ?: 0) != 0
        view.setAutoShift(on)
    }

    private fun canHandOff(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && shouldOfferSwitchingToNextInputMethod()

    private fun handOff(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        ImeSession.handedOffFrom = currentInputEditorInfo?.packageName
        return switchToPreviousInputMethod()
    }

    private suspend fun preload(target: KeyboardSettings) {
        val wanted =
            target.languages.flatMap { listOf(it.layoutId, it.compactLayoutId) } +
                fullLayoutIds(target.languages, target.fullLayoutStyle) +
                fullPageIds(target.fullLayoutStyle) +
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
        val upToDate =
            viewSettings?.let { viewKey(it) == viewKey(settings) } == true && profile == viewProfile
        if (upToDate && !viewUsedFallback) return
        rebuildView()
    }

    private fun rebuildView() {
        try {
            val view = createView()
            setInputView(view)
            adopt(view)
        } catch (e: RuntimeException) {
            Timber.e(e, "rebuilding the keyboard failed, keeping the current one")
        }
    }

    private fun adopt(view: KeyboardView) {
        view.previewEnabled = previewAllowed(currentInputEditorInfo)
        keyboardView = view
        viewSettings = settings
        viewProfile = profile
    }

    private fun createView(): KeyboardView =
        try {
            keyboardView(profile == Profile.COMPACT)
        } catch (e: RuntimeException) {
            Timber.e(e, "building the keyboard failed, using the built-in layout")
            viewUsedFallback = true
            keyboardView(compact = false, fallback = true)
        }

    private fun keyboardView(compact: Boolean, fallback: Boolean = false): KeyboardView {
        val languages =
            when {
                fallback -> listOf(FallbackLayout.pcFullEnUs)
                compact -> compactLanguages()
                else -> fullLanguages()
            }
        val rows = languages.maxOf { it.rows.size }
        val compactRows = compact && rows <= COMPACT_ROWS
        return KeyboardView(
            context = this,
            languages = languages,
            initialLanguageIndex = ImeSession.languageIndex,
            languageChangeListener = languageChangeListener,
            sink = dispatcher,
            latencyLogging = BuildConfig.DEBUG,
            rowHeightPx =
                if (compactRows) compactRowHeightPx(rowHeightPx(fullRowCount()))
                else rowHeightPx(rows),
            haptics = settings.haptics,
            labelScale = settings.labelScalePercent / PERCENT,
            pages =
                when {
                    fallback -> emptyMap()
                    compact -> compactPages()
                    else -> fullPages()
                },
            actions = actions,
        )
    }

    private fun fullLanguages(): List<Layout> {
        val style = settings.fullLayoutStyle
        val switchable =
            switchable(settings.languages.map { fullLayoutFor(it, style, layouts::get) })
        return switchable.map { it.withExtraRows(settings.fKeyRow, settings.extraRow) }
    }

    // The Fn page gets the same extra rows, so the keyboard keeps its height when Fn toggles.
    private fun fullPages(): Map<String, Layout> =
        fullPagesFor(settings.fullLayoutStyle, layouts::get).mapValues { (_, page) ->
            page.withExtraRows(settings.fKeyRow, settings.extraRow)
        }

    private fun compactLanguages(): List<Layout> {
        val switchable = switchable(settings.languages.map { layouts[it.compactLayoutId] })
        return if (canHandOff()) switchable else switchable.map { it.withKeyboardKeyToFull() }
    }

    private fun compactPages(): Map<String, Layout> {
        val handOff = canHandOff()
        return mapOf(
                BuiltInLayouts.PAGE_SYMBOLS to layouts[BuiltInLayouts.COMPACT_SYMBOLS],
                BuiltInLayouts.PAGE_SYMBOLS_MORE to layouts[BuiltInLayouts.COMPACT_SYMBOLS_MORE],
            )
            .mapNotNull { (page, layout) -> layout?.let { page to it } }
            .toMap()
            .mapValues { (_, layout) -> if (handOff) layout else layout.withKeyboardKeyToFull() }
    }

    // Without another keyboard ⌨ can't hand off, so a tap shows the full layout instead.
    private fun Layout.withKeyboardKeyToFull(): Layout =
        withSwitchTarget(BuiltInLayouts.OTHER_KEYBOARD, BuiltInLayouts.FULL_LAYOUT)

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

    private fun fullRowCount(): Int =
        FallbackLayout.pcFullEnUs.withExtraRows(settings.fKeyRow, settings.extraRow).rows.size

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun previewAllowed(info: EditorInfo?): Boolean =
        settings.keyPreview && !isPasswordInput(info?.inputType ?: 0)

    private companion object {
        const val PERCENT = 100f
        const val COMPACT_ROWS = 4
    }
}

private fun viewKey(settings: KeyboardSettings): KeyboardSettings =
    settings.copy(
        outsideTerminals = OutsideTerminals.COMPACT,
        autoCapitalize = true,
        doubleSpacePeriod = true,
    )
