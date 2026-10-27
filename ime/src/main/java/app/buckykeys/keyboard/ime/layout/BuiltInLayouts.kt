package app.buckykeys.keyboard.ime.layout

import android.content.res.AssetManager
import java.io.IOException
import timber.log.Timber

public object BuiltInLayouts {

    public const val PC_FULL_EN_US: String = "pc_full_en_us"
    public const val PC_FULL_EL: String = "pc_full_el"
    public const val PC_FULL_ET: String = "pc_full_et"
    public const val PC_HK_EN_US: String = "pc_hk_en_us"
    public const val PC_HK_EL: String = "pc_hk_el"
    public const val PC_HK_ET: String = "pc_hk_et"
    public const val PC_HK_FN: String = "pc_hk_fn"
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
    public const val PAGE_FN: String = "@page:fn"
    public const val OTHER_KEYBOARD: String = "@keyboard"
    public const val FULL_LAYOUT: String = "@full"

    public fun load(assets: AssetManager, id: String): Layout = loadOrFallback { read(assets, id) }

    // For layouts with their own fallback (the classic ones and the Fn page): null, not English.
    public fun loadOrNull(assets: AssetManager, id: String): Layout? = loadOrNull {
        read(assets, id)
    }

    internal fun loadOrFallback(read: () -> String): Layout =
        loadOrNull(read) ?: FallbackLayout.pcFullEnUs

    internal fun loadOrNull(read: () -> String): Layout? {
        val result =
            try {
                LayoutParser.parse(read())
            } catch (e: IOException) {
                Result.failure(e)
            }
        return result.getOrElse { error ->
            Timber.e(error, "built-in layout failed to load")
            null
        }
    }

    private fun read(assets: AssetManager, id: String): String =
        assets.open("layouts/$id.json").bufferedReader().use { it.readText() }
}
