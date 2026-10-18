package dev.pckeys.keyboard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pckeys.keyboard.billing.domain.repository.BillingRepository
import dev.pckeys.keyboard.billing.foss.data.repository.FossBillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides @Singleton fun provideBillingRepository(): BillingRepository = FossBillingRepository()
}
