package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Key
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Text
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.KeyDef
import app.buckykeys.keyboard.ime.layout.KeySpec
import app.buckykeys.keyboard.ime.layout.Layout
import app.buckykeys.keyboard.ime.layout.ModifierKind
import app.buckykeys.keyboard.ime.layout.Row
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardEngineTest {

    private val ctrl = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
    private val shift = KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
    private val ctrlBit = 1 shl ModifierKind.CTRL.ordinal

    private var geometry = KeyboardGeometry(FallbackLayout.pcFullEnUs, 1080, 120)
    private var engine = KeyboardEngine(geometry)
    private val sink = FakeInputSink()
    private var now = 0L
    private val del = Key(KeyEvent.KEYCODE_DEL, 0)

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
    fun `tapping ctrl twice locks it for every following key`() {
        tap("Ctrl")
        tap("Ctrl")
        tap("c")
        tap("d")
        assertEquals(
            listOf(Key(KeyEvent.KEYCODE_C, ctrl), Key(KeyEvent.KEYCODE_D, ctrl)),
            sink.emitted,
        )
        assertEquals(ctrlBit, engine.lockedMask)
    }

    @Test
    fun `tapping ctrl a third time turns it off`() {
        tap("Ctrl")
        tap("Ctrl")
        tap("Ctrl")
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
        assertEquals(0, engine.modifierMask)
        assertEquals(0, engine.lockedMask)
    }

    @Test
    fun `a one-shot modifier is not locked`() {
        tap("Ctrl")
        assertEquals(ctrlBit, engine.modifierMask)
        assertEquals(0, engine.lockedMask)
    }

    @Test
    fun `a locked modifier and a one-shot one combine, and only the one-shot one clears`() {
        tap("Ctrl")
        tap("Ctrl")
        tap("Shift")
        tap("↑")
        tap("↑")
        assertEquals(
            listOf(
                Key(KeyEvent.KEYCODE_DPAD_UP, ctrl or shift),
                Key(KeyEvent.KEYCODE_DPAD_UP, ctrl),
            ),
            sink.emitted,
        )
    }

    @Test
    fun `holding ctrl while tapping c chords them`() {
        down("Ctrl", pointer = 0)
        tap("c", pointer = 1)
        up("Ctrl", pointer = 0)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
    }

    @Test
    fun `releasing a chorded modifier leaves it off`() {
        down("Ctrl", pointer = 0)
        tap("c", pointer = 1)
        up("Ctrl", pointer = 0)
        tap("d")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl), Text("d")), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `a held modifier applies to every key tapped while it is down`() {
        down("Shift", pointer = 0)
        tap("a", pointer = 1)
        tap("b", pointer = 1)
        up("Shift", pointer = 0)
        assertEquals(listOf(Text("A"), Text("B")), sink.emitted)
    }

    @Test
    fun `a held modifier shows as active`() {
        down("Ctrl", pointer = 0)
        assertEquals(ctrlBit, engine.modifierMask)
    }

    @Test
    fun `a cancelled modifier press changes nothing`() {
        down("Ctrl", pointer = 0)
        cancel(pointer = 0)
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `sliding off a modifier before lifting does not toggle it`() {
        down("Ctrl", pointer = 0)
        up("c", pointer = 0)
        assertEquals(0, engine.modifierMask)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `reset clears locked modifiers too`() {
        tap("Ctrl")
        tap("Ctrl")
        engine.reset()
        assertEquals(0, engine.lockedMask)
        assertEquals(0, engine.modifierMask)
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
        engine.onPointer(0, ax, ay, PointerAction.DOWN, now, sink)
        engine.onPointer(0, sx, sy, PointerAction.UP, now, sink)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(1 shl ModifierKind.CTRL.ordinal, engine.modifierMask)
    }

    @Test
    fun `a cancelled press emits nothing and keeps the pending modifier`() {
        tap("Ctrl")
        val (cx, cy) = centre("c")
        engine.onPointer(0, cx, cy, PointerAction.DOWN, now, sink)
        engine.onPointer(0, cx, cy, PointerAction.CANCEL, now, sink)
        assertTrue(sink.emitted.isEmpty())
        tap("c")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
    }

    @Test
    fun `two overlapping pointers each emit their own key`() {
        val (ax, ay) = centre("a")
        val (sx, sy) = centre("s")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, now, sink)
        engine.onPointer(1, sx, sy, PointerAction.DOWN, now, sink)
        engine.onPointer(0, ax, ay, PointerAction.UP, now, sink)
        engine.onPointer(1, sx, sy, PointerAction.UP, now, sink)
        assertEquals(listOf(Text("a"), Text("s")), sink.emitted)
    }

    @Test
    fun `a pressed key reports pressed until it is released`() {
        val index = indexOf("a")
        val (ax, ay) = centre("a")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, now, sink)
        assertTrue(engine.isPressed(index))
        engine.onPointer(0, ax, ay, PointerAction.UP, now, sink)
        assertFalse(engine.isPressed(index))
    }

    @Test
    fun `a pointer id beyond the tracked range is ignored`() {
        val (ax, ay) = centre("a")
        engine.onPointer(42, ax, ay, PointerAction.DOWN, now, sink)
        engine.onPointer(42, ax, ay, PointerAction.UP, now, sink)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `reset clears pressed keys and modifiers`() {
        tap("Ctrl")
        val (ax, ay) = centre("a")
        engine.onPointer(0, ax, ay, PointerAction.DOWN, now, sink)
        engine.reset()
        engine.onPointer(0, ax, ay, PointerAction.UP, now, sink)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `holding backspace repeats after 400 ms and then every 50 ms`() {
        down("⌫", pointer = 0)
        tick(399)
        assertTrue(sink.emitted.isEmpty())
        tick(400)
        tick(450)
        tick(500)
        now = 520
        up("⌫", pointer = 0)
        assertEquals(listOf(del, del, del), sink.emitted)
    }

    @Test
    fun `the next deadline follows the repeat and clears on release`() {
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
        down("⌫", pointer = 0)
        assertEquals(400L, engine.nextDeadline)
        tick(400)
        assertEquals(450L, engine.nextDeadline)
        up("⌫", pointer = 0)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `a late tick sends one repeat rather than a burst`() {
        down("⌫", pointer = 0)
        tick(1000)
        assertEquals(listOf(del), sink.emitted)
        assertEquals(1050L, engine.nextDeadline)
    }

    @Test
    fun `letters do not repeat`() {
        down("a", pointer = 0)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
        tick(1000)
        up("a", pointer = 0)
        assertEquals(listOf(Text("a")), sink.emitted)
    }

    @Test
    fun `space delete and the arrows repeat`() {
        listOf("", "Del", "←", "↑", "↓", "→").forEach { label ->
            down(label, pointer = 0)
            assertEquals(label, now + 400, engine.nextDeadline)
            up(label, pointer = 0)
        }
    }

    @Test
    fun `a one-shot ctrl applies to every repeat and clears on release`() {
        tap("Ctrl")
        down("←", pointer = 0)
        tick(400)
        tick(450)
        up("←", pointer = 0)
        tap("←")
        val left = KeyEvent.KEYCODE_DPAD_LEFT
        assertEquals(listOf(Key(left, ctrl), Key(left, ctrl), Key(left, 0)), sink.emitted)
    }

    @Test
    fun `sliding off a repeating key stops the repeat`() {
        down("⌫", pointer = 0)
        tick(400)
        now = 420
        move("a", pointer = 0)
        tick(450)
        up("a", pointer = 0)
        assertEquals(listOf(del), sink.emitted)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `moving within a repeating key keeps the repeat`() {
        down("⌫", pointer = 0)
        move("⌫", pointer = 0)
        tick(400)
        assertEquals(listOf(del), sink.emitted)
    }

    @Test
    fun `a cancel stops the repeat`() {
        down("⌫", pointer = 0)
        tick(400)
        cancel(pointer = 0)
        tick(450)
        assertEquals(listOf(del), sink.emitted)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `pressing another key stops the repeat`() {
        down("⌫", pointer = 0)
        tick(400)
        now = 420
        tap("a", pointer = 1)
        tick(450)
        up("⌫", pointer = 0)
        assertEquals(listOf(del, Text("a")), sink.emitted)
    }

    @Test
    fun `holding ctrl through a repeat does not leave ctrl on`() {
        down("Ctrl", pointer = 0)
        down("←", pointer = 1)
        tick(400)
        up("Ctrl", pointer = 0)
        up("←", pointer = 1)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_DPAD_LEFT, ctrl)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `pressing a modifier keeps the repeat going with that modifier`() {
        down("←", pointer = 0)
        tick(400)
        now = 420
        down("Shift", pointer = 1)
        tick(450)
        val left = KeyEvent.KEYCODE_DPAD_LEFT
        assertEquals(listOf(Key(left, 0), Key(left, shift)), sink.emitted)
    }

    @Test
    fun `reset stops the repeat`() {
        down("⌫", pointer = 0)
        engine.reset()
        tick(400)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    private fun tap(label: String, pointer: Int = 0) {
        down(label, pointer)
        up(label, pointer)
    }

    private fun down(label: String, pointer: Int) {
        val (x, y) = centre(label)
        engine.onPointer(pointer, x, y, PointerAction.DOWN, now, sink)
    }

    private fun up(label: String, pointer: Int) {
        val (x, y) = centre(label)
        engine.onPointer(pointer, x, y, PointerAction.UP, now, sink)
    }

    private fun move(label: String, pointer: Int) {
        val (x, y) = centre(label)
        engine.onPointer(pointer, x, y, PointerAction.MOVE, now, sink)
    }

    private fun tick(time: Long) {
        now = time
        engine.onTick(now, sink)
    }

    private fun cancel(pointer: Int) {
        engine.onPointer(pointer, 0f, 0f, PointerAction.CANCEL, now, sink)
    }

    private fun indexOf(label: String): Int =
        (0 until geometry.keyCount).first { geometry.spec(it).label == label }

    private fun centre(label: String): Pair<Float, Float> {
        val i = indexOf(label) * 4
        val r = geometry.rects
        return (r[i] + r[i + 2]) / 2f to (r[i + 1] + r[i + 3]) / 2f
    }
}
