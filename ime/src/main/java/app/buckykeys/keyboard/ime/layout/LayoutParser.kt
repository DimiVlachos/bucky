package app.buckykeys.keyboard.ime.layout

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull

public object LayoutParser {

    private val layoutFields = setOf("id", "name", "rows")
    private val rowFields = setOf("keys")
    private val keyFields = setOf("chars", "key", "switchTo", "label", "width", "longPress")
    private val keyKinds = listOf("chars", "key", "switchTo")

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
            rows =
                obj.nonEmptyArray("rows", path).mapIndexed { index, row ->
                    row(row, "$path.rows[$index]")
                },
        )
    }

    private fun row(element: JsonElement, path: String): Row {
        val obj = element.asObject(path, rowFields)
        return Row(
            obj.nonEmptyArray("keys", path).mapIndexed { index, key ->
                key(key, "$path.keys[$index]")
            },
        )
    }

    private fun key(element: JsonElement, path: String): KeySpec {
        val obj = element.asObject(path, keyFields)
        val kind = keyKinds.filter { it in obj }.singleOrNull()
        val def =
            when (kind) {
                "chars" -> chars(obj, path)
                "key" -> named(obj.string("key", path), "$path.key")
                "switchTo" -> KeyDef.LayoutSwitch(obj.string("switchTo", path))
                else -> fail(path, "needs exactly one of chars, key or switchTo")
            }
        if (def !is KeyDef.Char && "longPress" in obj) fail("$path.longPress", "needs chars")
        val label = if ("label" in obj) obj.string("label", path) else DefaultLabels.of(def)
        val width = obj["width"]?.let { width(it, "$path.width") } ?: 1f
        return KeySpec(def, label, width)
    }

    private fun chars(obj: JsonObject, path: String): KeyDef.Char {
        val chars = obj.strings("chars", path)
        if (chars.size !in 1..2) fail("$path.chars", "needs one or two strings")
        val alternates = if ("longPress" in obj) obj.strings("longPress", path) else emptyList()
        return KeyDef.Char(chars[0], chars.getOrElse(1) { chars[0].uppercase() }, alternates)
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
