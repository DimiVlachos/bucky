package app.buckykeys.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.InputMethodService.Insets
import android.view.View
import android.view.inputmethod.EditorInfo
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import app.buckykeys.keyboard.ime.dispatch.InputDispatcher
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.withoutLanguageKey
import app.buckykeys.keyboard.ime.view.KeyboardView
import app.buckykeys.keyboard.ime.view.isPasswordInput
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BuckyImeService : InputMethodService() {

    @Inject lateinit var billingRepository: BillingRepository

    @Inject lateinit var keyboardSettingsRepository: KeyboardSettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var keyboardView: KeyboardView? = null

    private var settings = KeyboardSettings.defaults(Locale.getDefault().language)

    private val layouts = mutableMapOf<String, Layout>()

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            billingRepository.entitlement.collect { Timber.d("entitlement: %s", it) }
        }
        serviceScope.launch {
            keyboardSettingsRepository.settings.distinctUntilChanged().collect { latest ->
                settings = latest
                if (keyboardView != null) setInputView(onCreateInputView())
            }
        }
    }

    override fun onCreateInputView(): View =
        KeyboardView(
                context = this,
                languages = languages(),
                sink = InputDispatcher { currentInputConnection },
                latencyLogging = BuildConfig.DEBUG,
                rowHeightPx = rowHeightPx(),
                haptics = settings.haptics,
            )
            .also {
                it.previewEnabled = previewAllowed(currentInputEditorInfo)
                keyboardView = it
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

    private fun languages(): List<Layout> {
        val enabled =
            settings.languages.map { language ->
                layouts.getOrPut(language.layoutId) {
                    BuiltInLayouts.load(assets, language.layoutId)
                }
            }
        return if (enabled.size == 1) enabled.map { it.withoutLanguageKey() } else enabled
    }

    private fun rowHeightPx(): Int {
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val dp = if (landscape) settings.landscapeRowHeightDp else settings.rowHeightDp
        return (dp * resources.displayMetrics.density).roundToInt()
    }

    private fun previewAllowed(info: EditorInfo?): Boolean =
        settings.keyPreview && !isPasswordInput(info?.inputType ?: 0)
}
