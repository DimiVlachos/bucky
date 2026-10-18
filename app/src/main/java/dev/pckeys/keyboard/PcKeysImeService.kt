package dev.pckeys.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import dev.pckeys.keyboard.billing.domain.repository.BillingRepository
import dev.pckeys.keyboard.ime.dispatch.InputDispatcher
import dev.pckeys.keyboard.ime.layout.SpikeLayout
import dev.pckeys.keyboard.ime.view.KeyboardView
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PcKeysImeService : InputMethodService() {

    @Inject lateinit var billingRepository: BillingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
