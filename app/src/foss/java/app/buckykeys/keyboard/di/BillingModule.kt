package app.buckykeys.keyboard.di

import app.buckykeys.keyboard.billing.domain.repository.BillingRepository
import app.buckykeys.keyboard.billing.foss.data.repository.FossBillingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides @Singleton fun provideBillingRepository(): BillingRepository = FossBillingRepository()
}
