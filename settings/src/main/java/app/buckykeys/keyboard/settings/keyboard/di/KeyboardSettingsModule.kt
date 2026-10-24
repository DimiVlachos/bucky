package app.buckykeys.keyboard.settings.keyboard.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import app.buckykeys.keyboard.settings.keyboard.data.repository.KeyboardSettingsRepositoryImpl
import app.buckykeys.keyboard.settings.keyboard.data.serializer.KeyboardSettingsSerializer
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardSettings
import app.buckykeys.keyboard.settings.keyboard.domain.repository.KeyboardSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object KeyboardSettingsModule {

    @Provides
    @Singleton
    fun provideKeyboardSettingsRepository(
        @ApplicationContext context: Context,
    ): KeyboardSettingsRepository {
        val defaults = KeyboardSettings.defaults(Locale.getDefault().language)
        val dataStore =
            DataStoreFactory.create(
                serializer = KeyboardSettingsSerializer(defaults),
                corruptionHandler = ReplaceFileCorruptionHandler { defaults },
                produceFile = { context.dataStoreFile(FILE_NAME) },
            )
        return KeyboardSettingsRepositoryImpl(dataStore, defaults)
    }

    private const val FILE_NAME = "keyboard_settings.json"
}
