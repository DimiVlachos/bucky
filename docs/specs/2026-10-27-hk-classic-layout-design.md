# Hacker's Keyboard (classic) layout: design

Date: 2026-10-27. Status: approved by Dimitris on 2026-10-27 and built on feat/hk-classic-layout. Decisions move into `PLAN.md` (§3.2, §4.3, §4.6) in the same PR that implements them.

## Why

ketas (Discussion #57, hackerskeyboard#875) used Hacker's Keyboard for ten years and has its key positions memorised, bugs included. alpha4 moved Bucky's full layout toward HK ("`~ top left, Esc bottom left") but kept it centred for netllama (#51). ketas still asked "any chance the keys could be realigned to hk pattern?", and later: "at least one of them would need to replicate hk. perhaps without bugs".

HK's real layout is not centred: its letters sit about one key left of the middle. netllama only saw it as balanced because their HK clipped the right edge (the #957 width bug). One layout can't serve both, so Bucky gets a second full layout.

**Who it's for:** HK veterans like ketas, in all three languages (Dimitris, 2026-10-27).

## Goals

1. A full layout named "Hacker's Keyboard (classic)" that puts every key where HK's 5-row full layout (`kbd_full.xml`) puts it, for English (US), Greek and Estonian.
2. Fn opens HK's Fn page (`kbd_full_fn.xml`): F1–F12 in a 4×3 block, a numpad, Home/End/PgUp/PgDn, Ins/Del, SysRq/ScrollLock/Break/NumLock.
3. ❖ Meta is a real modifier, and ○ sends D-pad centre.
4. One setting picks the full-layout style for every language: "Centred" (default) or "Hacker's Keyboard (classic)".

## Non-goals

- HK's options key on the Fn page's top-left slot (`key_f1`, -103). It is `` ` ``~ there instead. The main-layer options key came back as ⚙ (see Grid).
- Any change to the compact layout, its pages or the per-app profiles.
- A per-language style choice. One switch covers all languages.
- The layout editor (PLAN v1.1).
- HK's bugs: the clipped right edge (#957), fixed widths that ignore insets.

## Anti-goals (a failure even if it works)

- Any key in the classic layout landing somewhere other than HK's position on the 14-unit grid. That breaks the muscle memory this layout exists for.
- Any change to the centred layout or to existing users' keyboards. Existing settings load as "Centred".
- Breaking the CLAUDE.md rules: no allocations in `onDraw`/`onTouchEvent`, the IME never crashes, `KeyboardEngine` imports only `KeyEvent` constants from Android, and `:ime` stays free of settings and DI.

## Decided

### Grid (from HK's `kbd_full.xml`, 14 units; the default key is 1u)

| Row | Keys (width in units, 1 if not given) |
|---|---|
| Numbers | `` ` ``~ 0.64, 1–0, - =, ⌫ 1.36 |
| Q | Tab 1, q–p, [ ], `\` 1 |
| A | Ctrl 1.5, a–', Enter 1.5 |
| Shift (EN, EL) | Shift 2, z–/, ↑, Shift |
| Shift (ET) | Shift, `<>`, z–m, `,` `.` `-`, ↑, Shift |
| Bottom | Esc 1.5, ⚙, Alt, ❖, space 4.5, Fn, ○, ← ↓ → |

- HK shows the ISO `<>` key only for languages that have one (its Finnish keymap does, US and Greek don't). When the key is empty, Shift takes its width. Estonian follows the Finnish case.
- HK's 1u options key comes back as ⚙ (`switchTo: "@settings"`), which opens Bucky's settings, so Esc keeps HK's 1.5 width. The first build left it out and widened Esc to 2.5; ketas found that Esc "massive" (Dimitris, 2026-10-27).
- No Home, End or Del on the main layer. They're on the Fn page, as in HK.
- The right Shift behaves exactly like the left one.

### Languages

- **EN:** the US characters in the positions above.
- **EL:** the same Greek characters, dead keys (΄ ¨ on the `;` key) and `latin` mappings as `pc_full_el.json`, on the classic grid.
- **ET:** the same characters, dead keys, AltGr-symbol long-presses and `latin` mappings as `pc_full_et.json`, on the classic grid, with `<>` left of z.
- Long-press alternates, hints, `fn`-free number-row long-presses (F1–F12 on hold) and dead keys carry over from each language's centred layout, key for key.

### Fn page (from HK's `kbd_full_fn.xml`, 14 units)

| Row | Keys (width in units) |
|---|---|
| 1 | `` ` ``~ 1.5, F1 F2 F3 F4 1.125 each, 7 8 9 / 1.125 each, Home 1.5, ⌫ 2 |
| 2 | Tab 1.5, F5–F8, 4 5 6 *, End 1.5, Ins 1, Del 1 |
| 3 | Ctrl 1.5, F9–F12, 1 2 3 -, PgUp 1.5, Enter 2 |
| 4 | Shift 1.5, SysRq ScrollLock Break NumLock, 0 , . +, PgDn 1.5, ↑ 1, Shift 1 |
| Bottom | same as the main layer |

- HK has its options key top left on row 1. Bucky puts a plain `` ` ``~ character key there instead (my call). That includes Estonian, whose main layer has the ˇ/~ dead key in that spot, so one shared Fn page serves all three languages.
- The numpad keys type plain characters, as in HK.
- The Fn page is the same for all three languages: one shared asset.

### Behaviour

- **Fn** keeps Bucky's modifier behaviour: tap for one key, tap twice to lock, or hold. While Fn is active in the classic layout, the keyboard shows the Fn page in place of the main layer. After a one-shot key it returns to the main layer.
- Ctrl, Alt, Shift and ❖ apply to Fn-page keys. Ctrl+Fn+F5 sends Ctrl+F5.
- **❖ Meta** is a new modifier with the same tap, lock and hold behaviour as Ctrl and Alt. It adds `META_META_ON | META_META_LEFT_ON`.
- **○** sends `KEYCODE_DPAD_CENTER`.
- **New key codes:** SYSRQ, SCROLL_LOCK, BREAK, NUM_LOCK and DPAD_CENTER. They're also usable in custom layouts and Termux-style extra rows.
- **Labels:** ⇧ ⇥ as in the centred layout, ❖ and ○ as in HK. The new keys get short labels (SysRq, ScrLk, Brk, NumLk).
- **Special-key colours** follow the existing rule: specials and modifiers draw darker.
- **Extra rows** (F1–F12 row, the user's row) still go above the classic layout, as they do above the centred one.

### Settings

- A "Full layout" choice next to Languages, with two options: "Centred" (default) and "Hacker's Keyboard (classic)". EN and EL strings.
- Stored in `KeyboardSettings` (DataStore). An unknown or missing value loads as Centred.
- Applies wherever the full layout is shown: terminal apps, the Full profile, and holding ⌨ on the compact layout.

### Settled during review (my calls)

- **Meta on a character key** sends a key event with `META_META_ON`, as Ctrl and Alt do. `TypingRules` already counts `META_META_MASK` as a chord.
- **Extra rows stay** while the Fn page is shown, so the keyboard height doesn't jump.
- **A held Fn survives the page swap.** `setGeometry(keepPointers = true)` must keep the modifier pointer that is holding Fn.
- **The classic main layer has no `fn` fields,** because Fn opens the page instead.
- **The classic layout is free,** like every built-in layout (PLAN §6.1).
- **Existing tests:** the centring test and the EN/EL same-geometry test apply to the centred layouts only.
- **Fallback:** if a classic asset fails to load, the language's centred layout is used. If that fails too, the existing `FallbackLayout` is used.

## Left to the builder

- **How the layout declares its Fn page.** For example a layout field naming a page, or a convention in `PageSet`, as long as `:ime` stays settings-free and the engine stays pure.
- **File and ID names,** for example `pc_hk_en_us`, `pc_hk_el`, `pc_hk_et` and `pc_hk_fn`.
- **How `KeyboardLanguage` maps to the classic IDs.**
- **Tests:**
  - every classic row spans 14 units;
  - the EL and ET classic layouts keep the EN classic geometry (except ET's `<>` row);
  - each key's x position matches HK's grid;
  - engine tests for Meta, the Fn page with one-shot, lock and chords, and the new key codes.
- **Device checks:** the S23 and the Android 17 emulator. Pin adb to the emulator's own serial; see the memory note.
