package app.buckykeys.keyboard.settings.keyboard.domain.interpreter

import app.buckykeys.keyboard.settings.keyboard.domain.model.ExtraRowPreview

public fun interface ExtraRowInterpreter {
    public fun interpret(words: List<String>): ExtraRowPreview
}
