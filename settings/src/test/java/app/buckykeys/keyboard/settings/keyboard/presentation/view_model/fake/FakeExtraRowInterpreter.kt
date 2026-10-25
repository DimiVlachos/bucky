package app.buckykeys.keyboard.settings.keyboard.presentation.view_model.fake

import app.buckykeys.keyboard.settings.keyboard.domain.interpreter.ExtraRowInterpreter
import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview

internal class FakeExtraRowInterpreter : ExtraRowInterpreter {

    var lastWords: List<String> = emptyList()
        private set

    override fun interpret(words: List<String>): ExtraRowPreview {
        lastWords = words
        return ExtraRowPreview(labels = words, unknown = emptyList(), overLimit = false, limit = 15)
    }
}
