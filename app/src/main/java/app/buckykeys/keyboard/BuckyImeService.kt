package app.buckykeys.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import app.buckykeys.keyboard.ime.dispatch.InputDispatcher
import app.buckykeys.keyboard.ime.layout.SpikeLayout
import app.buckykeys.keyboard.ime.view.KeyboardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BuckyImeService : InputMethodService() {

    @Inject lateinit var billingRepository: BillingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var keyboardView: KeyboardView? = null

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            billingRepository.entitlement.collect { Timber.d("entitlement: %s", it) }
        }
    }

    override fun onCreateInputView(): View =
        KeyboardView(
                context = this,
                layout = SpikeLayout.pcFullEnUs,
                sink = InputDispatcher { currentInputConnection },
                latencyLogging = BuildConfig.DEBUG,
            )
            .also { keyboardView = it }

    override fun onFinishInputView(finishingInput: Boolean) {
        keyboardView?.resetState()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        keyboardView = null
        super.onDestroy()
    }
}
