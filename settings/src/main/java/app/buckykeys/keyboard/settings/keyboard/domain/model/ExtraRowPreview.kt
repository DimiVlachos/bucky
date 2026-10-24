package app.buckykeys.keyboard.settings.keyboard.domain.model

public data class ExtraRowPreview(
    val labels: List<String>,
    val unknown: List<String>,
    val overLimit: Boolean,
) {
    val valid: Boolean
        get() = unknown.isEmpty()
}
