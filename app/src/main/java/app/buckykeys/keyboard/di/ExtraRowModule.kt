package app.buckykeys.keyboard.di

import app.buckykeys.keyboard.ime.layout.MAX_EXTRA_ROW_KEYS
import app.buckykeys.keyboard.ime.layout.parseExtraRow
import app.buckykeys.keyboard.settings.keyboard.domain.interpreter.ExtraRowInterpreter
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ExtraRowModule {

    @Provides
    fun provideExtraRowInterpreter(): ExtraRowInterpreter = ExtraRowInterpreter { words ->
        val parsed = parseExtraRow(words)
        ExtraRowPreview(
            labels = parsed.keys.map { it.label },
            unknown = parsed.unknown,
            overLimit = parsed.overLimit,
            limit = MAX_EXTRA_ROW_KEYS,
        )
    }
}
