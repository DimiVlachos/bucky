package app.buckykeys.keyboard.ime.engine

import android.view.KeyEvent
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Key
import app.buckykeys.keyboard.ime.engine.fake.FakeInputSink.Emitted.Text
import app.buckykeys.keyboard.ime.geometry.KeyboardGeometry
import app.buckykeys.keyboard.ime.layout.BuiltInLayouts
import app.buckykeys.keyboard.ime.layout.FallbackLayout
import app.buckykeys.keyboard.ime.layout.KeyCode
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
    private var engine = KeyboardEngine(geometry, LONG_PRESS_MS)
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
        engine = KeyboardEngine(geometry, LONG_PRESS_MS)
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
        tick(300)
        tick(350)
        tick(1000)
        up("a", pointer = 0)
        assertEquals(listOf(Text("A")), sink.emitted)
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

    @Test
    fun `holding a letter past the long-press timeout types its shifted form`() {
        down("a", pointer = 0)
        assertEquals(300L, engine.nextDeadline)
        tick(300)
        up("a", pointer = 0)
        assertEquals(listOf(Text("A")), sink.emitted)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `a letter released before the long-press timeout types normally`() {
        down("a", pointer = 0)
        tick(299)
        up("a", pointer = 0)
        assertEquals(listOf(Text("a")), sink.emitted)
    }

    @Test
    fun `holding 1 opens its alternates and lifting picks f1`() {
        down("1", pointer = 0)
        tick(300)
        assertTrue(sink.emitted.isEmpty())
        assertEquals(indexOf("1"), engine.popup.keyIndex)
        assertEquals(1, engine.popup.count)
        assertEquals(0, engine.popup.selected)
        up("1", pointer = 0)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F1, 0)), sink.emitted)
        assertFalse(engine.popup.isOpen)
    }

    @Test
    fun `the popup sits one row above its key`() {
        down("1", pointer = 0)
        tick(300)
        val i = indexOf("1") * 4
        val r = geometry.rects
        assertEquals(r[i], engine.popup.left, 0.01f)
        assertEquals(r[i + 1] - 120f, engine.popup.top, 0.01f)
        assertEquals(r[i + 2] - r[i], engine.popup.cellWidth, 0.01f)
        assertEquals(120f, engine.popup.cellHeight, 0.01f)
    }

    @Test
    fun `sliding below the key dismisses the popup without typing`() {
        down("1", pointer = 0)
        tick(300)
        val (x, y) = centre("1")
        engine.onPointer(0, x, y + 120f, PointerAction.MOVE, now, sink)
        assertEquals(AlternatesPopup.NONE, engine.popup.selected)
        engine.onPointer(0, x, y + 120f, PointerAction.UP, now, sink)
        assertTrue(sink.emitted.isEmpty())
        assertFalse(engine.popup.isOpen)
    }

    @Test
    fun `sliding along the popup selects an alternate`() {
        useRow(accented, char("x"), char("y"), char("z"))
        down("e", pointer = 0)
        tick(300)
        engine.onPointer(0, 150f, 60f, PointerAction.MOVE, now, sink)
        assertEquals(1, engine.popup.selected)
        engine.onPointer(0, 150f, 60f, PointerAction.UP, now, sink)
        assertEquals(listOf(Text("è")), sink.emitted)
    }

    @Test
    fun `a popup near the right edge stays inside the keyboard`() {
        useRow(char("x"), char("y"), char("z"), accented)
        down("e", pointer = 0)
        tick(300)
        assertEquals(100f, engine.popup.left, 0.01f)
        assertEquals(400f, engine.popup.left + 3 * engine.popup.cellWidth, 0.01f)
    }

    @Test
    fun `near the right edge the first alternate still sits above the key`() {
        useRow(char("x"), char("y"), char("z"), accented)
        down("e", pointer = 0)
        tick(300)
        assertTrue(engine.popup.reversed)
        up("e", pointer = 0)
        assertEquals(listOf(Text("é")), sink.emitted)
    }

    @Test
    fun `near the right edge sliding left walks through the alternates`() {
        useRow(char("x"), char("y"), char("z"), accented)
        down("e", pointer = 0)
        tick(300)
        engine.onPointer(0, 250f, 60f, PointerAction.MOVE, now, sink)
        assertEquals(1, engine.popup.selected)
        engine.onPointer(0, 150f, 60f, PointerAction.UP, now, sink)
        assertEquals(listOf(Text("ê")), sink.emitted)
    }

    @Test
    fun `holding a letter with accents types its capital`() {
        geometry = KeyboardGeometry(FallbackLayout.pcFullEnUs, 1080, 120)
        engine = KeyboardEngine(geometry, LONG_PRESS_MS)
        down("o", pointer = 0)
        tick(LONG_PRESS_MS)
        up("o", pointer = 0)
        assertEquals(listOf(Text("O")), sink.emitted)
    }

    @Test
    fun `a popup wider than the keyboard shrinks its cells`() {
        useRow(char("x"), accented)
        down("e", pointer = 0)
        tick(300)
        assertEquals(0f, engine.popup.left, 0.01f)
        assertEquals(400f / 3, engine.popup.cellWidth, 0.01f)
    }

    @Test
    fun `a key with no alternates and no shifted form does not long press`() {
        useRow(KeySpec(KeyDef.Char("-", "-"), "-"))
        down("-", pointer = 0)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `pressing the next key before the long-press timeout cancels the long press`() {
        down("a", pointer = 0)
        now = 100
        down("s", pointer = 1)
        tick(300)
        up("a", pointer = 0)
        assertEquals(400L, engine.nextDeadline)
        up("s", pointer = 1)
        assertEquals(listOf(Text("a"), Text("s")), sink.emitted)
    }

    @Test
    fun `a one-shot ctrl applies to the chosen alternate and then clears`() {
        tap("Ctrl")
        down("1", pointer = 0)
        tick(300)
        up("1", pointer = 0)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F1, ctrl)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `a cancel closes the popup without typing`() {
        down("1", pointer = 0)
        tick(300)
        cancel(pointer = 0)
        assertFalse(engine.popup.isOpen)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `a second finger closes the popup and the first finger types nothing`() {
        down("1", pointer = 0)
        tick(300)
        tap("a", pointer = 1)
        up("1", pointer = 0)
        assertFalse(engine.popup.isOpen)
        assertEquals(listOf(Text("a")), sink.emitted)
    }

    @Test
    fun `reset closes the popup`() {
        down("1", pointer = 0)
        tick(300)
        engine.reset()
        assertFalse(engine.popup.isOpen)
    }

    @Test
    fun `pressing a letter previews it until it is lifted`() {
        down("a", pointer = 0)
        assertEquals(indexOf("a"), engine.previewKey)
        up("a", pointer = 0)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `modifiers and special keys are not previewed`() {
        down("Ctrl", pointer = 0)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
        up("Ctrl", pointer = 0)
        down("⌫", pointer = 0)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `the latest finger owns the preview`() {
        down("a", pointer = 0)
        down("s", pointer = 1)
        assertEquals(indexOf("s"), engine.previewKey)
        up("s", pointer = 1)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `sliding off the previewed key hides the preview`() {
        down("a", pointer = 0)
        move("s", pointer = 0)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `an open popup hides the preview`() {
        down("1", pointer = 0)
        tick(300)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `a long press that types the shifted letter hides the preview`() {
        down("a", pointer = 0)
        tick(300)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `cancel and reset hide the preview`() {
        down("a", pointer = 0)
        cancel(pointer = 0)
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
        down("a", pointer = 0)
        engine.reset()
        assertEquals(KeyboardEngine.NO_PREVIEW, engine.previewKey)
    }

    @Test
    fun `tonos then alpha types the accented alpha`() {
        useGreek()
        tap("΄")
        tap("α")
        assertEquals(listOf(Text("ά")), sink.emitted)
    }

    @Test
    fun `shift then the tonos key gives dialytika for the next letter only`() {
        useGreek()
        tap("Shift")
        tap("΄")
        tap("ι")
        assertEquals(listOf(Text("ϊ")), sink.emitted)
    }

    @Test
    fun `shift after the tonos gives the accented capital`() {
        useGreek()
        tap("΄")
        tap("Shift")
        tap("α")
        assertEquals(listOf(Text("Ά")), sink.emitted)
    }

    @Test
    fun `a letter with no accented form types the mark and then the letter`() {
        useGreek()
        tap("΄")
        tap("ψ")
        assertEquals(listOf(Text("΄"), Text("ψ")), sink.emitted)
    }

    @Test
    fun `the tonos key twice types the tonos once`() {
        useGreek()
        tap("΄")
        tap("΄")
        assertEquals(listOf(Text("΄")), sink.emitted)
        assertEquals(KeyboardEngine.NO_DEAD_KEY, engine.pendingDeadKey)
    }

    @Test
    fun `tonos then space types the tonos alone`() {
        useGreek()
        tap("΄")
        tap("")
        assertEquals(listOf(Text("΄")), sink.emitted)
    }

    @Test
    fun `backspace cancels a pending tonos and sends nothing`() {
        useGreek()
        tap("΄")
        tap("⌫")
        tap("α")
        assertEquals(listOf(Text("α")), sink.emitted)
    }

    @Test
    fun `enter after a pending tonos clears it and still sends enter`() {
        useGreek()
        tap("΄")
        tap("Enter")
        tap("α")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_ENTER, 0), Text("α")), sink.emitted)
    }

    @Test
    fun `a pending dead key is reported for highlighting`() {
        useGreek()
        tap("΄")
        assertEquals(indexOf("΄"), engine.pendingDeadKey)
        tap("α")
        assertEquals(KeyboardEngine.NO_DEAD_KEY, engine.pendingDeadKey)
    }

    @Test
    fun `ctrl on a greek letter sends the latin letter at that position`() {
        useGreek()
        tap("Ctrl")
        tap("ψ")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
    }

    @Test
    fun `ctrl on the tonos key sends its latin key and leaves nothing pending`() {
        useGreek()
        tap("Ctrl")
        tap("΄")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_SEMICOLON, ctrl)), sink.emitted)
        assertEquals(KeyboardEngine.NO_DEAD_KEY, engine.pendingDeadKey)
    }

    @Test
    fun `the language key requests a switch once`() {
        useGreek()
        tap("🌐")
        assertEquals("@language", engine.takeSwitchRequest())
        assertEquals(null, engine.takeSwitchRequest())
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `reset clears a pending dead key`() {
        useGreek()
        tap("΄")
        engine.reset()
        tap("α")
        assertEquals(listOf(Text("α")), sink.emitted)
    }

    @Test
    fun `locked and one-shot modifiers survive a geometry change`() {
        tap("Ctrl")
        tap("Ctrl")
        tap("Shift")
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 720, 100))
        tap("↑")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_DPAD_UP, ctrl or shift)), sink.emitted)
        assertEquals(ctrlBit, engine.lockedMask)
    }

    @Test
    fun `a pending dead key survives a geometry change`() {
        useGreek()
        tap("΄")
        swapGeometry(KeyboardGeometry(lastLayout, 800, 90))
        assertEquals(indexOf("΄"), engine.pendingDeadKey)
        tap("α")
        assertEquals(listOf(Text("ά")), sink.emitted)
    }

    @Test
    fun `a geometry with no matching dead key drops the pending mark`() {
        useGreek()
        tap("΄")
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 1080, 120))
        assertEquals(KeyboardEngine.NO_DEAD_KEY, engine.pendingDeadKey)
        tap("a")
        assertEquals(listOf(Text("a")), sink.emitted)
    }

    @Test
    fun `a finger held through a geometry change types nothing when it lifts`() {
        down("a", pointer = 0)
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 720, 100))
        assertFalse(engine.isPressed(indexOf("a")))
        up("a", pointer = 0)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `a held modifier does not stick after a geometry change`() {
        down("Ctrl", pointer = 0)
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 720, 100))
        up("Ctrl", pointer = 0)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `a geometry change stops the long press and closes the popup`() {
        down("1", pointer = 0)
        tick(300)
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 720, 100))
        assertFalse(engine.popup.isOpen)
        down("a", pointer = 0)
        swapGeometry(KeyboardGeometry(FallbackLayout.pcFullEnUs, 1080, 120))
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `two ctrl keys held and released first to last leave ctrl off`() {
        useTwoCtrls()
        down("Ctrl", pointer = 0)
        down("RCtrl", pointer = 1)
        up("Ctrl", pointer = 0)
        assertEquals(ctrlBit, engine.modifierMask)
        up("RCtrl", pointer = 1)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `two ctrl keys held and released last to first leave ctrl off`() {
        useTwoCtrls()
        down("Ctrl", pointer = 0)
        down("RCtrl", pointer = 1)
        up("RCtrl", pointer = 1)
        assertEquals(ctrlBit, engine.modifierMask)
        up("Ctrl", pointer = 0)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `releasing one of two held ctrl keys keeps ctrl for the next key`() {
        useTwoCtrls()
        down("Ctrl", pointer = 0)
        down("RCtrl", pointer = 1)
        up("Ctrl", pointer = 0)
        tap("c", pointer = 2)
        up("RCtrl", pointer = 1)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_C, ctrl)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `ctrl on the tonos key clears a tonos that was already pending`() {
        useGreek()
        tap("΄")
        tap("Ctrl")
        tap("΄")
        tap("α")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_SEMICOLON, ctrl), Text("α")), sink.emitted)
    }

    @Test
    fun `ctrl on a dead key with no latin key code still arms the dead key`() {
        useRow(
            KeySpec(KeyDef.Dead("΄", "¨", latin = "§"), "΄"),
            KeySpec(KeyDef.Char("α", "Α", composed = mapOf("΄" to "ά")), "α"),
            KeySpec(KeyDef.Modifier(ModifierKind.CTRL), "Ctrl"),
        )
        tap("Ctrl")
        tap("΄")
        tap("α")
        assertEquals(listOf(Text("ά")), sink.emitted)
    }

    @Test
    fun `with shift locked the tonos still gives the accented capital`() {
        useGreek()
        tap("Shift")
        tap("Shift")
        tap("΄")
        tap("α")
        assertEquals(listOf(Text("Ά")), sink.emitted)
    }

    @Test
    fun `holding shift on the tonos key gives dialytika`() {
        useGreek()
        down("Shift", pointer = 1)
        tap("΄", pointer = 0)
        up("Shift", pointer = 1)
        tap("ι")
        assertEquals(listOf(Text("ϊ")), sink.emitted)
    }

    @Test
    fun `the dead key shows its shifted mark for one-shot shift but not locked shift`() {
        useGreek()
        assertFalse(engine.deadKeyShifted)
        tap("Shift")
        assertTrue(engine.deadKeyShifted)
        tap("Shift")
        assertFalse(engine.deadKeyShifted)
    }

    @Test
    fun `lifting on a popup cell commits that cell even without a move`() {
        useRow(accented, char("x"), char("y"), char("z"))
        down("e", pointer = 0)
        tick(300)
        engine.onPointer(0, 250f, 60f, PointerAction.UP, now, sink)
        assertEquals(listOf(Text("ê")), sink.emitted)
    }

    @Test
    fun `lifting below the key after sliding along the popup types nothing`() {
        useRow(accented, char("x"), char("y"), char("z"))
        down("e", pointer = 0)
        tick(300)
        engine.onPointer(0, 150f, 60f, PointerAction.MOVE, now, sink)
        engine.onPointer(0, 150f, 300f, PointerAction.UP, now, sink)
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `a long press that types the shifted letter is counted`() {
        down("a", pointer = 0)
        assertEquals(0, engine.longPressCount)
        tick(300)
        assertEquals(1, engine.longPressCount)
    }

    @Test
    fun `a long press that opens the popup is counted`() {
        down("1", pointer = 0)
        tick(300)
        assertEquals(1, engine.longPressCount)
    }

    @Test
    fun `a key repeat is not counted as a long press`() {
        down("⌫", pointer = 0)
        tick(400)
        tick(450)
        assertEquals(0, engine.longPressCount)
    }

    @Test
    fun `swiping right on the space bar requests the next language and types nothing`() {
        engine.languageSwipe = true
        swipeSpace(by = 100f)
        assertEquals(BuiltInLayouts.NEXT_LANGUAGE, engine.takeSwitchRequest())
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `swiping left on the space bar requests the previous language`() {
        engine.languageSwipe = true
        swipeSpace(by = -100f)
        assertEquals(BuiltInLayouts.PREVIOUS_LANGUAGE, engine.takeSwitchRequest())
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `a short slide on the space bar still types a space`() {
        engine.languageSwipe = true
        swipeSpace(by = 40f)
        assertEquals(null, engine.takeSwitchRequest())
        assertEquals(listOf(Text(" ")), sink.emitted)
    }

    @Test
    fun `with one language a swipe on the space bar types a space`() {
        swipeSpace(by = 100f)
        assertEquals(null, engine.takeSwitchRequest())
        assertEquals(listOf(Text(" ")), sink.emitted)
    }

    @Test
    fun `one swipe switches the language once`() {
        engine.languageSwipe = true
        val (x, y) = centre("")
        engine.onPointer(0, x, y, PointerAction.DOWN, now, sink)
        engine.onPointer(0, x + 100f, y, PointerAction.MOVE, now, sink)
        assertEquals(BuiltInLayouts.NEXT_LANGUAGE, engine.takeSwitchRequest())
        engine.onPointer(0, x + 200f, y, PointerAction.MOVE, now, sink)
        engine.onPointer(0, x + 200f, y, PointerAction.UP, now, sink)
        assertEquals(null, engine.takeSwitchRequest())
        assertTrue(sink.emitted.isEmpty())
    }

    @Test
    fun `a swipe after the space bar started repeating does not switch`() {
        engine.languageSwipe = true
        val (x, y) = centre("")
        engine.onPointer(0, x, y, PointerAction.DOWN, now, sink)
        tick(400L)
        engine.onPointer(0, x + 100f, y, PointerAction.MOVE, now, sink)
        assertEquals(null, engine.takeSwitchRequest())
        assertEquals(listOf(Text(" ")), sink.emitted)
    }

    @Test
    fun `a swipe keeps a one-shot shift for the next key`() {
        engine.languageSwipe = true
        tap("Shift")
        swipeSpace(by = 100f)
        tap("c")
        assertEquals(listOf(Text("C")), sink.emitted)
    }

    @Test
    fun `fn then a number sends the f key and clears fn`() {
        tap("Fn")
        tap("1")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F1, 0)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `a locked fn keeps sending f keys`() {
        tap("Fn")
        tap("Fn")
        tap("1")
        tap("2")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F1, 0), Key(KeyEvent.KEYCODE_F2, 0)), sink.emitted)
    }

    @Test
    fun `holding fn while pressing up sends page up`() {
        down("Fn", pointer = 0)
        tap("↑", pointer = 1)
        up("Fn", pointer = 0)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_PAGE_UP, 0)), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `ctrl and fn together send ctrl with the f key`() {
        tap("Ctrl")
        tap("Fn")
        tap("5")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F5, ctrl)), sink.emitted)
    }

    @Test
    fun `fn then a key without an fn variant types the key`() {
        tap("Fn")
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
        assertEquals(0, engine.modifierMask)
    }

    @Test
    fun `fn and an arrow repeat the fn variant`() {
        tap("Fn")
        down("↓", pointer = 0)
        tick(400L)
        tick(450L)
        up("↓", pointer = 0)
        assertEquals(
            listOf(Key(KeyEvent.KEYCODE_PAGE_DOWN, 0), Key(KeyEvent.KEYCODE_PAGE_DOWN, 0)),
            sink.emitted,
        )
    }

    @Test
    fun `letting go of fn before the key still sends the fn variant`() {
        down("Fn", pointer = 0)
        down("1", pointer = 1)
        up("Fn", pointer = 0)
        up("1", pointer = 1)
        assertEquals(listOf(Key(KeyEvent.KEYCODE_F1, 0)), sink.emitted)
    }

    @Test
    fun `fn on the number row does not open the long-press popup`() {
        tap("Fn")
        down("1", pointer = 0)
        assertEquals(KeyboardEngine.NO_DEADLINE, engine.nextDeadline)
    }

    @Test
    fun `holding the keyboard key requests the full layout`() {
        useRow(keyboardKey, char("c"))
        down("⌨", pointer = 0)
        tick(LONG_PRESS_MS)
        up("⌨", pointer = 0)
        assertEquals(BuiltInLayouts.FULL_LAYOUT, engine.takeSwitchRequest())
        assertEquals(null, engine.takeSwitchRequest())
    }

    @Test
    fun `tapping the keyboard key requests the other keyboard`() {
        useRow(keyboardKey, char("c"))
        tap("⌨")
        assertEquals(BuiltInLayouts.OTHER_KEYBOARD, engine.takeSwitchRequest())
    }

    @Test
    fun `a key with then types and then requests the switch`() {
        useRow(
            KeySpec(KeyDef.Char(" ", " "), "space", then = BuiltInLayouts.PAGE_LETTERS),
            char("c"),
        )
        tap("space")
        assertEquals(listOf(Text(" ")), sink.emitted)
        assertEquals(BuiltInLayouts.PAGE_LETTERS, engine.takeSwitchRequest())
    }

    @Test
    fun `auto shift capitalises the next letter and clears`() {
        engine.setAutoShift(true)
        tap("c")
        assertEquals(listOf(Text("C")), sink.emitted)
        assertFalse(engine.autoShift)
    }

    @Test
    fun `tapping shift while auto shift is on turns it off`() {
        engine.setAutoShift(true)
        tap("Shift")
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
        assertEquals(0, engine.lockedMask)
    }

    @Test
    fun `auto shift does not survive reset`() {
        engine.setAutoShift(true)
        engine.reset()
        assertFalse(engine.autoShift)
        tap("c")
        assertEquals(listOf(Text("c")), sink.emitted)
    }

    @Test
    fun `auto shift shows as an active shift`() {
        engine.setAutoShift(true)
        assertTrue(engine.modifierMask and (1 shl ModifierKind.SHIFT.ordinal) != 0)
    }

    @Test
    fun `auto shift does not reach enter`() {
        engine.setAutoShift(true)
        tap("Enter")
        assertEquals(listOf(Key(KeyEvent.KEYCODE_ENTER, 0)), sink.emitted)
    }

    @Test
    fun `auto shift does not reach backspace`() {
        engine.setAutoShift(true)
        tap("⌫")
        assertEquals(listOf(del), sink.emitted)
    }

    @Test
    fun `a greek sentence can start with an accented capital`() {
        useGreek()
        engine.setAutoShift(true)
        tap("΄")
        tap("α")
        assertEquals(listOf(Text("Ά")), sink.emitted)
    }

    @Test
    fun `holding the keyboard key counts as a long press`() {
        useRow(keyboardKey, char("c"))
        down("⌨", pointer = 0)
        tick(LONG_PRESS_MS)
        assertEquals(1, engine.longPressCount)
    }

    private val keyboardKey =
        KeySpec(KeyDef.LayoutSwitch(BuiltInLayouts.OTHER_KEYBOARD, BuiltInLayouts.FULL_LAYOUT), "⌨")

    private fun swipeSpace(by: Float) {
        val (x, y) = centre("")
        engine.onPointer(0, x, y, PointerAction.DOWN, now, sink)
        engine.onPointer(0, x + by, y, PointerAction.MOVE, now, sink)
        engine.onPointer(0, x + by, y, PointerAction.UP, now, sink)
    }

    private fun swapGeometry(next: KeyboardGeometry) {
        geometry = next
        engine.setGeometry(next)
    }

    private var lastLayout: Layout = FallbackLayout.pcFullEnUs

    private fun useTwoCtrls() {
        useRow(
            KeySpec(KeyDef.Modifier(ModifierKind.CTRL), "Ctrl"),
            char("c"),
            KeySpec(KeyDef.Modifier(ModifierKind.CTRL), "RCtrl"),
        )
    }

    private fun useGreek() {
        useRow(
            KeySpec(KeyDef.Dead("΄", "¨", latin = ";"), "΄"),
            KeySpec(
                KeyDef.Char(
                    "α",
                    "Α",
                    composed = mapOf("΄" to "ά"),
                    composedShifted = mapOf("΄" to "Ά"),
                ),
                "α",
            ),
            KeySpec(
                KeyDef.Char(
                    "ι",
                    "Ι",
                    composed = mapOf("΄" to "ί", "¨" to "ϊ"),
                    composedShifted = mapOf("΄" to "Ί", "¨" to "Ϊ"),
                ),
                "ι",
            ),
            KeySpec(KeyDef.Char("ψ", "Ψ", latin = "c"), "ψ"),
            KeySpec(KeyDef.Char(" ", " "), ""),
            KeySpec(KeyDef.Special(KeyCode.BACKSPACE), "⌫"),
            KeySpec(KeyDef.Special(KeyCode.ENTER), "Enter"),
            KeySpec(KeyDef.Modifier(ModifierKind.SHIFT), "Shift"),
            KeySpec(KeyDef.Modifier(ModifierKind.CTRL), "Ctrl"),
            KeySpec(KeyDef.LayoutSwitch("@language"), "🌐"),
        )
    }

    private val accented =
        KeySpec(
            KeyDef.Char(
                "e",
                "E",
                listOf(KeyDef.Char("é", "É"), KeyDef.Char("è", "È"), KeyDef.Char("ê", "Ê")),
            ),
            "e",
        )

    private fun char(c: String): KeySpec = KeySpec(KeyDef.Char(c, c.uppercase()), c)

    private fun useRow(vararg keys: KeySpec) {
        lastLayout = Layout("test", "test", listOf(Row(keys.toList())))
        geometry = KeyboardGeometry(lastLayout, 400, 120)
        engine = KeyboardEngine(geometry, LONG_PRESS_MS)
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

    private companion object {
        const val LONG_PRESS_MS = 300L
    }
}
