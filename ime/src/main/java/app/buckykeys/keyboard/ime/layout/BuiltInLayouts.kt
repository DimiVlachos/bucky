package app.buckykeys.keyboard.ime.layout

import android.content.res.AssetManager
import java.io.IOException
import timber.log.Timber

public object BuiltInLayouts {

    public const val PC_FULL_EN_US: String = "pc_full_en_us"
    public const val PC_FULL_EL: String = "pc_full_el"
    public const val PC_FULL_ET: String = "pc_full_et"
    public const val COMPACT_EN_US: String = "compact_en_us"
    public const val COMPACT_EL: String = "compact_el"
    public const val COMPACT_ET: String = "compact_et"
    public const val COMPACT_SYMBOLS: String = "compact_symbols"
    public const val COMPACT_SYMBOLS_MORE: String = "compact_symbols_more"
    public const val NEXT_LANGUAGE: String = "@language"
    public const val PREVIOUS_LANGUAGE: String = "@language-previous"
    public const val PAGE_LETTERS: String = "@page:letters"
    public const val PAGE_SYMBOLS: String = "@page:symbols"
    public const val PAGE_SYMBOLS_MORE: String = "@page:symbols-more"
    public const val OTHER_KEYBOARD: String = "@keyboard"
    public const val FULL_LAYOUT: String = "@full"

    public fun load(assets: AssetManager, id: String): Layout = loadOrFallback {
        assets.open("layouts/$id.json").bufferedReader().use { it.readText() }
    }

    internal fun loadOrFallback(read: () -> String): Layout {
        val result =
            try {
                LayoutParser.parse(read())
            } catch (e: IOException) {
                Result.failure(e)
            }
        return result.getOrElse { error ->
            Timber.e(error, "built-in layout failed to load, using the fallback")
            FallbackLayout.pcFullEnUs
        }
    }
}
