package app.buckykeys.keyboard.profile

import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.settings.keyboard.domain.model.FullLayoutStyle
import app.buckykeys.keyboard.settings.keyboard.domain.model.KeyboardLanguage

internal fun fullLayoutIds(
    languages: List<KeyboardLanguage>,
    style: FullLayoutStyle,
): List<String> = languages.map {
    if (style == FullLayoutStyle.HK_CLASSIC) it.classicLayoutId else it.layoutId
}

internal fun fullLayoutFor(
    language: KeyboardLanguage,
    style: FullLayoutStyle,
    loaded: (String) -> Layout?,
): Layout? {
    val classic =
        if (style == FullLayoutStyle.HK_CLASSIC) loaded(language.classicLayoutId) else null
    return classic ?: loaded(language.layoutId)
}

// These have their own fallback, so a failed load must stay missing instead of becoming English.
internal fun optionalLayoutIds(
    languages: List<KeyboardLanguage>,
    style: FullLayoutStyle,
): Set<String> =
    if (style == FullLayoutStyle.HK_CLASSIC) {
        (languages.map { it.classicLayoutId } + fullPageIds(style)).toSet()
    } else {
        emptySet()
    }

internal fun fullPageIds(style: FullLayoutStyle): List<String> =
    if (style == FullLayoutStyle.HK_CLASSIC) listOf(BuiltInLayouts.PC_HK_FN) else emptyList()

internal fun fullPagesFor(
    style: FullLayoutStyle,
    loaded: (String) -> Layout?,
): Map<String, Layout> {
    if (style != FullLayoutStyle.HK_CLASSIC) return emptyMap()
    val fnPage = loaded(BuiltInLayouts.PC_HK_FN) ?: return emptyMap()
    return mapOf(BuiltInLayouts.PAGE_FN to fnPage)
}
