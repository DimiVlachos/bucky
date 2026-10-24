package app.buckykeys.keyboard.ime.layout

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull

public object LayoutParser {

    private val layoutFields = setOf("id", "name", "language", "rows")
    private val rowFields = setOf("keys", "inset")
    private val keyFields =
        setOf(
            "chars",
            "dead",
            "key",
            "switchTo",
            "holdTo",
            "label",
            "width",
            "longPress",
            "latin",
            "fn",
            "then",
        )
    private val keyKinds = listOf("chars", "dead", "key", "switchTo")
    private const val LATIN_KEYS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-=[];',./\\`"
    private val keyNameLike = Regex("[A-Z][A-Z0-9_]+")
    private val switchTargets =
        setOf(
            BuiltInLayouts.NEXT_LANGUAGE,
            BuiltInLayouts.PAGE_LETTERS,
            BuiltInLayouts.PAGE_SYMBOLS,
            BuiltInLayouts.PAGE_SYMBOLS_MORE,
            BuiltInLayouts.OTHER_KEYBOARD,
        )

    public fun parse(json: String): Result<Layout> =
        try {
            Result.success(layout(Json.parseToJsonElement(json), "$"))
        } catch (e: SerializationException) {
            Result.failure(LayoutParseException("$", "malformed JSON", e))
        } catch (e: LayoutParseException) {
            Result.failure(e)
        }

    private fun layout(element: JsonElement, path: String): Layout {
        val obj = element.asObject(path, layoutFields)
        return Layout(
            id = obj.string("id", path),
            name = obj.string("name", path),
            language =
                if ("language" in obj) obj.string("language", path) else obj.string("name", path),
            rows =
                withCompositions(
                    obj.nonEmptyArray("rows", path).mapIndexed { index, row ->
                        row(row, "$path.rows[$index]")
                    },
                ),
        )
    }

    private fun row(element: JsonElement, path: String): Row {
        val obj = element.asObject(path, rowFields)
        return Row(
            obj.nonEmptyArray("keys", path).mapIndexed { index, key ->
                key(key, "$path.keys[$index]")
            },
            obj["inset"]?.let { inset(it, "$path.inset") } ?: 0f,
        )
    }

    private fun key(element: JsonElement, path: String): KeySpec {
        val obj = element.asObject(path, keyFields)
        val kind = keyKinds.filter { it in obj }.singleOrNull()
        val def =
            when (kind) {
                "chars" -> chars(obj, path)
                "dead" -> dead(obj, path)
                "key" -> named(obj.string("key", path), "$path.key")
                "switchTo" -> switchTo(obj, path)
                else -> fail(path, "needs exactly one of chars, dead, key or switchTo")
            }
        if (def !is KeyDef.Char && "longPress" in obj) fail("$path.longPress", "needs chars")
        if (def !is KeyDef.LayoutSwitch && "holdTo" in obj) fail("$path.holdTo", "needs switchTo")
        if (def !is KeyDef.Char && def !is KeyDef.Dead && "latin" in obj) {
            fail("$path.latin", "needs chars or dead")
        }
        val label = if ("label" in obj) obj.string("label", path) else DefaultLabels.of(def)
        val width = obj["width"]?.let { width(it, "$path.width") } ?: 1f
        val fn = if ("fn" in obj) alternate(obj.string("fn", path), "$path.fn") else null
        val then = if ("then" in obj) target(obj.string("then", path), "$path.then") else null
        return KeySpec(def, label, width, fn, then)
    }

    private fun chars(obj: JsonObject, path: String): KeyDef.Char {
        val chars = obj.strings("chars", path)
        if (chars.size !in 1..2) fail("$path.chars", "needs one or two strings")
        val alternates =
            if ("longPress" in obj) {
                obj.strings("longPress", path).mapIndexed { index, name ->
                    alternate(name, "$path.longPress[$index]")
                }
            } else {
                emptyList()
            }
        return KeyDef.Char(
            chars[0],
            chars.getOrElse(1) { chars[0].uppercase() },
            alternates,
            latin(obj, path),
        )
    }

    private fun dead(obj: JsonObject, path: String): KeyDef.Dead {
        val marks = obj.strings("dead", path)
        if (marks.size !in 1..2) fail("$path.dead", "needs one or two strings")
        marks.forEachIndexed { index, mark ->
            if (!DeadKeys.composes(mark))
                fail("$path.dead[$index]", "dead mark has no combining form")
        }
        return KeyDef.Dead(marks[0], marks.getOrElse(1) { marks[0] }, latin(obj, path))
    }

    private fun latin(obj: JsonObject, path: String): String? {
        if ("latin" !in obj) return null
        val latin = obj.string("latin", path)
        if (latin.length != 1) fail("$path.latin", "needs exactly one character")
        if (latin !in LATIN_KEYS) {
            fail("$path.latin", "needs an ASCII letter, digit or US punctuation key")
        }
        return latin
    }

    private fun withCompositions(rows: List<Row>): List<Row> {
        val marks =
            rows
                .flatMap { row -> row.keys.map { it.def } }
                .filterIsInstance<KeyDef.Dead>()
                .flatMap { listOf(it.primary, it.shifted) }
                .distinct()
        if (marks.isEmpty()) return rows
        return rows.map { row ->
            Row(
                row.keys.map { spec ->
                    val def = spec.def as? KeyDef.Char ?: return@map spec
                    spec.copy(
                        def =
                            def.copy(
                                composed = compositions(def.primary, marks),
                                composedShifted = compositions(def.shifted, marks),
                            ),
                    )
                },
            )
        }
    }

    private fun compositions(base: String, marks: List<String>): Map<String, String> =
        marks.mapNotNull { mark -> DeadKeys.compose(base, mark)?.let { mark to it } }.toMap()

    private fun switchTo(obj: JsonObject, path: String): KeyDef.LayoutSwitch {
        val target = target(obj.string("switchTo", path), "$path.switchTo")
        val hold = if ("holdTo" in obj) obj.string("holdTo", path) else null
        if (hold != null && hold != BuiltInLayouts.FULL_LAYOUT) {
            fail("$path.holdTo", "unsupported hold target \"$hold\"")
        }
        return KeyDef.LayoutSwitch(target, hold)
    }

    private fun target(target: String, path: String): String {
        if (target !in switchTargets) fail(path, "unsupported switch target \"$target\"")
        return target
    }

    private fun inset(element: JsonElement, path: String): Float {
        val value =
            (element as? JsonPrimitive)?.takeUnless { it.isString }?.floatOrNull
                ?: fail(path, "expected a number")
        if (!value.isFinite() || value < 0f) fail(path, "must be zero or more")
        return value
    }

    private fun alternate(name: String, path: String): KeyDef {
        KeyCode.entries
            .firstOrNull { it.name == name }
            ?.let {
                return KeyDef.Special(it)
            }
        val upper = name.uppercase()
        val looksLikeKeyName =
            keyNameLike.matches(name) ||
                KeyCode.entries.any { it.name == upper } ||
                ModifierKind.entries.any { it.name == upper }
        if (looksLikeKeyName) fail(path, "unknown key \"$name\"")
        return KeyDef.Char(name, upper)
    }

    private fun named(name: String, path: String): KeyDef =
        KeyCode.entries.firstOrNull { it.name == name }?.let { KeyDef.Special(it) }
            ?: ModifierKind.entries.firstOrNull { it.name == name }?.let { KeyDef.Modifier(it) }
            ?: fail(path, "unknown key \"$name\"")

    private fun width(element: JsonElement, path: String): Float {
        val value =
            (element as? JsonPrimitive)?.takeUnless { it.isString }?.floatOrNull
                ?: fail(path, "expected a number")
        if (!value.isFinite() || value <= 0f) fail(path, "must be a positive number")
        return value
    }

    private fun JsonElement.asObject(path: String, fields: Set<String>): JsonObject {
        val obj = this as? JsonObject ?: fail(path, "expected an object")
        obj.keys.firstOrNull { it !in fields }?.let { fail("$path.$it", "unknown field") }
        return obj
    }

    private fun JsonObject.string(name: String, path: String): String =
        (this[name] ?: fail("$path.$name", "missing")).nonEmptyString("$path.$name")

    private fun JsonObject.strings(name: String, path: String): List<String> =
        array(name, path).mapIndexed { index, item -> item.nonEmptyString("$path.$name[$index]") }

    private fun JsonObject.array(name: String, path: String): JsonArray {
        val element = this[name] ?: fail("$path.$name", "missing")
        return element as? JsonArray ?: fail("$path.$name", "expected an array")
    }

    private fun JsonObject.nonEmptyArray(name: String, path: String): JsonArray =
        array(name, path).also { if (it.isEmpty()) fail("$path.$name", "must not be empty") }

    private fun JsonElement.nonEmptyString(path: String): String {
        val primitive = this as? JsonPrimitive
        if (primitive == null || !primitive.isString || primitive.content.isEmpty()) {
            fail(path, "expected a non-empty string")
        }
        return primitive.content
    }

    private fun fail(path: String, reason: String): Nothing =
        throw LayoutParseException(path, reason)
}
