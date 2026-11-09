package app.buckykeys.keyboard.settings.keyboard.data.serializer

import androidx.datastore.core.CorruptionException
import app.buckykeys.keyboard.settings.keyboard.domain.model.TouchOffset
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchOffsetSerializerTest {

    private val serializer = TouchOffsetSerializer()

    private fun read(json: String): TouchOffset = runBlocking {
        serializer.readFrom(ByteArrayInputStream(json.encodeToByteArray()))
    }

    @Test
    fun `an offset survives a write and a read`() {
        val offset = TouchOffset(x = -0.12f, y = 0.2f)
        val out = ByteArrayOutputStream()
        runBlocking { serializer.writeTo(offset, out) }
        assertEquals(offset, read(out.toByteArray().decodeToString()))
    }

    @Test
    fun `a new install starts with no offset`() {
        assertEquals(TouchOffset.NONE, serializer.defaultValue)
    }

    @Test
    fun `a missing field reads as zero`() {
        assertEquals(TouchOffset(x = 0.1f, y = 0f), read("""{ "x": 0.1 }"""))
    }

    @Test
    fun `invalid json is reported as corruption`() {
        val error = runCatching { read("""{ "x": """) }.exceptionOrNull()
        assertTrue("$error", error is CorruptionException)
    }
}
