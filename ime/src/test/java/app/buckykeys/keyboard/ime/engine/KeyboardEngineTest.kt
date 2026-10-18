package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Key
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Text
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.KeySpec
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.ModifierKind
import app.buckykeys.keyboard.ime.layout.Row
import app.buckykeys.keyboard.ime.layout.SpikeLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardEngineTest {

    private val ctrl = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
    private val shift = KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON

    private var geometry = KeyboardGeometry(SpikeLayout.pcFullEnUs, 1080, 120)
    private var engine = KeyboardEngine(geometry)
    private val sink = FakeInputSink()

    @Test
    fun `tapping a letter commits it as text`() {
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
    }

    @Test
    fun `ctrl then c sends the c key with ctrl meta and clears ctrl`() {
        tap("Ctrl")
        tap("c")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `escape sends the escape key`() {
        tap("Esc")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_ESCAPE, 0)), sink.emitted)
    }

    @Test
    fun `enter sends the raw enter key`() {
        tap("Enter")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_ENTER, 0)), sink.emitted)
    }

    @Test
    fun `backspace sends the del key`() {
        tap("⌫")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_DEL, 0)), sink.emitted)
    }

    @Test
    fun `shift then a commits an uppercase a`() {
        tap("Shift")
        tap("a")
        assertEquals(listOf(Text("A")), sink.emitted)
    }

    @Test
    fun `ctrl and shift then up combine both metas`() {
        tap("Ctrl")
        tap("Shift")
        tap("↑")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_DPAD_UP, ctrl or shift)), sink.emitted)
    }

    @Test
    fun `a modifier tap alone emits nothing`() {
        tap("Ctrl")
        assertTrue(sink.emitted.isEmpty())
        assertEquals(1 shl ModifierKind.CTRL.ordinal, engine.modifierMask)
    }

    @Test
    fun `tapping ctrl twice turns it off`() {
        tap("Ctrl")
        tap("Ctrl")
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
    }

    @Test
    fun `ctrl then a char with no key code commits the text and clears ctrl`() {
        geometry =
            KeyboardGeometry(
                Layout(
                    "test",
                    "test",
                    listOf(
                        Row(
                            listOf(
                                KeySpec(KeyDef.Modifier(ModifierKind.CTRL), "Ctrl"),
                                KeySpec(KeyDef.Char("§", "§"), "§"),
                            ),
                        ),
                    ),
                ),
                200,
                100,
            )
        engine = KeyboardEngine(geometry)
        tap("Ctrl")
        tap("§")
        assertEquals(listOf(Text("§")), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `lifting on a different key than the one pressed emits nothing`() {
        tap("Ctrl")
        val (ax, ay) = centre("a")
        val (sx, sy) = centre("s")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, sink)
        engine.onPointer(0, sx, sy, PointerAction.UP, sink)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(1 shl ModifierKind.CTRL.ordinal, engine.modifierMask)
    }

    @Test
    fun `a cancelled press emits nothing and keeps the pending modifier`() {
        tap("Ctrl")
        val (cx, cy) = centre("c")
        engine.onPointer(0, cx, cy, PointerAction.DOWN, sink)
        engine.onPointer(0, cx, cy, PointerAction.CANCEL, sink)
        assertTrue(sink.emitted.isEmpty())
        tap("c")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
    }

    @Test
    fun `two overlapping pointers each emit their own key`() {
        val (ax, ay) = centre("a")
        val (sx, sy) = centre("s")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, sink)
        engine.onPointer(1, sx, sy, PointerAction.DOWN, sink)
        engine.onPointer(0, ax, ay, PointerAction.UP, sink)
        engine.onPointer(1, sx, sy, PointerAction.UP, sink)
        assertEquals(listOf(Text("a"), Text("s")), sink.emitted)
    }

    @Test
    fun `a pressed key reports pressed until it is released`() {
        val index = indexOf("a")
        val (ax, ay) = centre("a")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, sink)
        assertTrue(engine.isPressed(index))
        engine.onPointer(0, ax, ay, PointerAction.UP, sink)
        assertFalse(engine.isPressed(index))
    }

    @Test
    fun `a pointer id beyond the tracked range is ignored`() {
        val (ax, ay) = centre("a")
        engine.onPointer(42, ax, ay, PointerAction.DOWN, sink)
        engine.onPointer(42, ax, ay, PointerAction.UP, sink)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `reset clears pressed keys and modifiers`() {
        tap("Ctrl")
        val (ax, ay) = centre("a")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, sink)
        engine.reset()
        engine.onPointer(0, ax, ay, PointerAction.UP, sink)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(0, engine.modifierMask)
    }

    private fun tap(label: String) {
        val (x, y) = centre(label)
        engine.onPointer(0, x, y, PointerAction.DOWN, sink)
        engine.onPointer(0, x, y, PointerAction.UP, sink)
    }

    private fun indexOf(label: String): Int =
        (0 until geometry.keyCount).first { geometry.spec(it).label == label }

    private fun centre(label: String): Pair<Float, Float> {
        val i = indexOf(label) * 4
        val r = geometry.rects
        return (r[i] + r[i + 2]) / 2f to (r[i + 1] + r[i + 3]) / 2f
    }
}
