package app.buckykeys.keyboard.ime.layout

import android.content.res.AssetManager
import java.io.IOException
import timber.log.Timber

public object BuiltInLayouts {

    public const val PC_FULL_EN_US: String = "pc_full_en_us"

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
