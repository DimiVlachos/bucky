package app.buckykeys.keyboard.ime.layout

public class LayoutParseException(
    public val path: String,
    public val reason: String,
    cause: Throwable? = null,
) : IllegalArgumentException("$path: $reason", cause)
