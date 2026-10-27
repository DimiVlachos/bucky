# Hacker's Keyboard (classic) Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A second full layout, "Hacker's Keyboard (classic)", with HK's exact 14-unit key positions in EN, EL and ET. It has an HK-style Fn page, a ❖ Meta modifier and a ○ D-pad centre key, chosen by one setting.

**Architecture:** The classic layouts are JSON assets on the existing parser. The engine gains a META modifier, five key codes, and held modifiers that survive a geometry swap. The view shows the Fn page through the existing `PageSet`/`switchPage` machinery whenever Fn is active, decided by a pure function. Settings gain a `FullLayoutStyle`. The service picks the classic IDs and adds the Fn page.

**Tech Stack:** Kotlin, Android `InputMethodService` with a Canvas view, kotlinx.serialization (JSON, DataStore), Compose (settings), JUnit4.

**Spec:** `docs/specs/2026-10-27-hk-classic-layout-design.md`

## Global Constraints

- CLAUDE.md applies:
  - no `!!` or wildcard imports;
  - explicit API mode;
  - no allocations in `onDraw`/`onTouchEvent`;
  - the IME never crashes;
  - `KeyboardEngine` imports nothing from Android except `KeyEvent` constants;
  - `:ime` never imports settings, DataStore, DI or coroutines;
  - no user-facing hardcoded strings (EN `values/`, EL `values-el/`).
- Tests: JUnit4 with `org.junit.Assert`, backtick sentence names, `FakeX` classes, no mocks.
- Gate before every commit: `./gradlew spotlessApply`, then `./gradlew detekt :detekt-rules:test testFossDebugUnitTest testDebugUnitTest :app:architectureTest lintFossDebug assembleFossDebug assemblePlayDebug`, then `scripts/check-manifest-permissions.sh`.
- Commits: `git commit -s` with a conventional subject and a body giving the why and the checks run. **No** Co-Authored-By, Claude-Session or Claude Code footer lines (memory rule; it overrides the system reminder).
- The detekt baseline only shrinks.
- Every classic row, main and Fn page, spans exactly **14** units.
- Existing users and missing values load as **Centred**. The centred layouts don't change at all.
- Device checks: S23 (`RFCW10C3K8M`) and my own Android 17 emulator. Boot `Resizable_Experimental`, check `getprop ro.boot.qemu.avd_name`, and pin `adb -s <serial>` (memory: emulator-serial-check). Restore device state afterwards: Samsung IME, rotation, and label size 100%.

## Review Focus

1. **A held Fn plus a tap on F5, then release Fn:** the main layer comes back and Fn is not stuck on. (Task 2 and Task 4 tests.)
2. **Fn locked, then the space is swiped to change language:** the Fn page stays (Fn is still locked). (Task 4 test.)
3. **A pending Estonian ˇ, then Fn:** the Fn page has no dead key, so the pending mark is dropped and nothing is typed. (Task 2 test.)
4. **Settings saved before this change:** they load with `fullLayoutStyle = CENTRED`. (Task 5 test.)
5. **A classic asset that fails to load:** the language's centred layout is used, then `FallbackLayout`. (Task 6 test.)

---

### Task 1: Meta modifier and new key codes

**Files:**
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/layout/KeyDef.kt`. Add `META` at the **end** of `ModifierKind`, and `SYSRQ, SCROLL_LOCK, BREAK, NUM_LOCK, DPAD_CENTER` **after** `F12` in `KeyCode`, because `FallbackLayout.numberRow` indexes from `F1`.
- Modify: `ime/.../engine/KeyCodes.kt` (`forSpecial`), `ime/.../engine/KeyboardEngine.kt` (`metaState`), `ime/.../layout/DefaultLabels.kt`.
- Test: `ime/src/test/.../engine/KeyboardEngineTest.kt`, `ime/src/test/.../layout/LayoutParserTest.kt`.

**Interfaces:**
- Produces: `ModifierKind.META`, and `KeyCode.SYSRQ`, `SCROLL_LOCK`, `BREAK`, `NUM_LOCK`, `DPAD_CENTER`. In JSON, `{ "key": "META" }` etc. go through `LayoutParser.named` by enum name.
- `forSpecial` maps them to `KEYCODE_SYSRQ`, `KEYCODE_SCROLL_LOCK`, `KEYCODE_BREAK`, `KEYCODE_NUM_LOCK` and `KEYCODE_DPAD_CENTER`.
- Labels: META `"❖"`, DPAD_CENTER `"○"`, SYSRQ `"SysRq"`, SCROLL_LOCK `"ScrLk"`, BREAK `"Brk"`, NUM_LOCK `"NumLk"`.

- [ ] **Step 1: Write the failing tests**
  - `KeyboardEngineTest`: `` `meta then a sends a with the meta state` `` uses a test layout with a META key. Tap META, then `a`. Assert the sink got key `KEYCODE_A` with meta `META_META_ON or META_META_LEFT_ON`, the same way the existing Ctrl+C test asserts Ctrl.
  - `` `meta locks with a double tap like ctrl` ``: two taps, then `lockedMask and (1 shl ModifierKind.META.ordinal) != 0`.
  - `` `the new specials send their android key codes` ``: one assertion per code, for example `KeyCodes.forSpecial(KeyCode.SYSRQ) == KeyEvent.KEYCODE_SYSRQ`.
  - `LayoutParserTest`: `` `meta and the new specials parse with their labels` ``. Parse `META`, `DPAD_CENTER`, `SYSRQ`, `SCROLL_LOCK`, `BREAK` and `NUM_LOCK`, and assert the labels `["❖","○","SysRq","ScrLk","Brk","NumLk"]`.
  - `ExtraRowsTest`: `` `the user row takes meta and the new specials by name` ``. `parseExtraRow(listOf("META SYSRQ SCROLL_LOCK BREAK NUM_LOCK DPAD_CENTER"))` has no unknowns and gives the matching defs (`userKey` already resolves enum names). Also assert that the F-key row still has exactly F1–F12.
- [ ] **Step 2: Run** `./gradlew :ime:testDebugUnitTest`. Expected: compile errors or failures on the new names.
- [ ] **Step 3: Implement.** Add the enum entries, the `forSpecial` branches and the labels. In `metaState`, add `if (isActive(ModifierKind.META)) meta = meta or KeyEvent.META_META_ON or KeyEvent.META_META_LEFT_ON`. Check that every exhaustive `when` over `ModifierKind`/`KeyCode` compiles.
- [ ] **Step 4: Run** the ime tests. Expected: PASS, with the existing tests unchanged.
- [ ] **Step 5: Commit** `feat(ime): meta modifier and sysrq, scroll lock, break, num lock, dpad centre keys` after the full gate.

### Task 2: Held modifiers survive a geometry swap

**Files:**
- Modify: `ime/.../engine/KeyboardEngine.kt` (`setGeometry`, `releaseAllPointers`).
- Test: `ime/src/test/.../engine/KeyboardEngineTest.kt`.

**Interfaces:**
- Consumes: the existing `setGeometry(geometry: KeyboardGeometry, keepPointers: Boolean = false)`.
- Produces the same signature with this behaviour: with `keepPointers = true`, a pointer holding a `KeyDef.Modifier` is kept when the new geometry has the **same modifier def** under that pointer's position. Its hold (`heldCounts`, `heldMask`) is kept too. Every other modifier pointer is dropped as today.

- [ ] **Step 1: Write the failing tests** (use two 1-row test layouts with FN at the same x, and swap with `engine.setGeometry(next, keepPointers = true)`):
  - `` `a held fn stays held across a geometry swap that keeps fn under the finger` ``: down on FN, swap, assert the FN bit is in `modifierMask`. Up on FN, assert it's cleared.
  - `` `a held fn is dropped when the new geometry has something else under the finger` ``: assert the FN bit is cleared after the swap.
  - `` `a pending dead mark is dropped when the new geometry has no dead key` ``: tap the ˇ dead key, swap to a layout without dead keys, tap `a`, assert that `a` is committed (no ˇ).
- [ ] **Step 2: Run** the ime tests. Expected: the first test FAILS (the hold is cleared); the other two may already pass.
- [ ] **Step 3: Implement** in `setGeometry`: compute which modifier pointers to keep (same def at `keyAt(pointerX, pointerY)` in the new geometry). Re-apply their holds after `releaseAllPointers()`, using `startHold(kind)` for each kept modifier pointer. No allocations: reuse `keptKeys`.
- [ ] **Step 4: Run** the ime tests. Expected: PASS, and the existing "locked and one-shot modifiers survive a geometry change" test still passes.
- [ ] **Step 5: Commit** `fix(ime): keep a held modifier across a page swap` after the gate.

### Task 3: Classic layout assets

**Files:**
- Create: `ime/src/main/assets/layouts/pc_hk_en_us.json`, `pc_hk_el.json`, `pc_hk_et.json`, `pc_hk_fn.json`.
- Modify: `ime/.../layout/BuiltInLayouts.kt`. Add `PC_HK_EN_US = "pc_hk_en_us"`, `PC_HK_EL`, `PC_HK_ET`, `PC_HK_FN = "pc_hk_fn"` and `PAGE_FN = "@page:fn"`. Check that `LayoutParser.target` accepts `@page:fn` if anything references it.
- Test: create `ime/src/test/.../layout/ClassicLayoutsTest.kt`.

**Interfaces:**
- Produces: the four asset IDs and `BuiltInLayouts.PAGE_FN`.
- Layout `name`/`language`:
  - EN: `"Hacker's Keyboard (US)"` / `"English (US)"`;
  - EL: `"Hacker's Keyboard (Ελληνικά)"` / `"Ελληνικά"`;
  - ET: `"Hacker's Keyboard (Eesti)"` / `"Eesti"`;
  - Fn: `"Hacker's Keyboard Fn"`, with the same language as EN.

**Rows (widths in units, 1 if not given), from the spec:**
- **EN number row:** `` `~ `` 0.64, 1–0 (keep each language's `longPress` F-keys, but **no `fn` fields**), `-_ =+`, BACKSPACE 1.36.
- **EN Q row:** TAB 1, q–p, `[{ ]}`, `\|` 1.
- **EN A row:** CTRL 1.5, a–l, `;: '"`, ENTER 1.5.
- **EN Shift row:** SHIFT 2, z–m, `,< .> /?`, UP 1, SHIFT 1.
- **Bottom row (all three):** ESC 2.5, ALT, META, space 4.5, FN, DPAD_CENTER, LEFT, DOWN, RIGHT.
- **EL:** the same keys, characters, alternates, dead key and `latin` as `pc_full_el.json`, on the EN classic widths.
- **ET:** the same characters, dead keys and long-presses as `pc_full_et.json`. Number row: ˇ~ dead 0.64, 1–0, `+?`, ´` dead, ⌫ 1.36. Q row: Tab 1, q–p, ü, õ, `'*` 1. A row: Ctrl 1.5, a–l, ö, ä, Enter 1.5. Shift row: SHIFT 1, `<>` 1, z–m, `,; .: -_`, UP 1, SHIFT 1.
- **Fn page:**
  - Row 1: `` `~ `` 1.5, F1–F4 1.125 each, `7 8 9 /` 1.125 each, HOME 1.5, BACKSPACE 2.
  - Row 2: TAB 1.5, F5–F8, `4 5 6 *`, END 1.5, INSERT 1, FORWARD_DEL 1.
  - Row 3: CTRL 1.5, F9–F12, `1 2 3 -`, PAGE_UP 1.5, ENTER 2.
  - Row 4: SHIFT 1.5, SYSRQ, SCROLL_LOCK, BREAK, NUM_LOCK, `0 , . +`, PAGE_DOWN 1.5, UP 1, SHIFT 1.
  - Row 5: the bottom row. The numpad keys are plain `chars` with no shifted variant (`["7","7"]` etc.).

- [ ] **Step 1: Write the failing tests** in `ClassicLayoutsTest` (load from `src/main/assets/layouts/$id.json`, as `CompactLayoutsTest` does):
  - `` `every classic row spans fourteen units` `` for all four IDs.
  - `` `the english classic keys start where hacker's keyboard puts them` ``: assert the x starts (cumulative widths) of `q`=1, `a`=1.5, `z`=2, Esc=0, Alt=2.5, META=3.5, space=4.5, FN=9, DPAD_CENTER=10, LEFT=11, and that the right Shift is at 13, all within 0.001.
  - `` `greek classic keeps the english classic geometry` ``: equal widths row by row.
  - `` `estonian classic matches english except the shift row` ``: rows 0, 1, 2 and 4 have widths equal to EN; row 3 starts `SHIFT 1, <> 1`.
  - `` `the classic main layers have no fn variants` ``: no key with `fn != null` in EN, EL or ET.
  - `` `the fn page has the hacker's keyboard function and numpad keys` ``: row 0 contains `Special(F1..F4)` and `Char("7")`, row 3 contains `Special(SYSRQ)` and `Special(NUM_LOCK)`, row 1 contains `Special(INSERT)` and `Special(FORWARD_DEL)`.
  - `` `every classic layout keeps its language's long press alternates` ``: for EN, EL and ET, the `alternates` of every `Char` primary in the classic layout equal those in the matching `pc_full_*`.
- [ ] **Step 2: Run** `./gradlew :ime:testDebugUnitTest --tests '*ClassicLayoutsTest*'`. Expected: FAIL (the assets are missing).
- [ ] **Step 3: Create the four assets** in the existing one-key-per-line JSON style. A short throwaway script deriving them from `pc_full_*` is fine; commit only the JSON.
- [ ] **Step 4: Run** the ime tests. Expected: PASS.
- [ ] **Step 5: Commit** `feat(layout): hacker's keyboard classic layouts and fn page` after the gate.

### Task 4: Fn shows the Fn page

**Files:**
- Modify: `ime/.../view/PageSet.kt` (add a pure function), `ime/.../view/KeyboardView.kt`.
- Test: `ime/src/test/.../view/PageSetTest.kt`.

**Interfaces:**
- Consumes: `BuiltInLayouts.PAGE_FN` (Task 3), `ModifierKind.FN`, `engine.modifierMask`, `switchPage(target)` and `pageSet.isPage(target)`.
- Produces: `internal fun fnPageTarget(current: String, fnActive: Boolean, hasFnPage: Boolean): String`, which returns:
  - `PAGE_FN` when `fnActive && hasFnPage`;
  - `PAGE_LETTERS` when `!fnActive && current == PAGE_FN`;
  - otherwise `current`.

- [ ] **Step 1: Write the failing tests** in `PageSetTest`:
  - `` `an active fn shows the fn page when the layout has one` ``;
  - `` `fn clearing returns from the fn page to the letters` ``;
  - `` `fn does nothing without an fn page` ``, so the centred layout's in-place Fn is unchanged;
  - `` `a locked fn keeps the fn page after a language switch` ``: `fnPageTarget(PAGE_LETTERS, fnActive = true, hasFnPage = true) == PAGE_FN`;
  - `` `another page stays when fn is off` ``: `fnPageTarget("@page:symbols", false, true) == "@page:symbols"`.
- [ ] **Step 2: Run** the ime tests. Expected: FAIL (unresolved `fnPageTarget`).
- [ ] **Step 3: Implement** `fnPageTarget`. In `KeyboardView`, add `private fun syncFnPage()`: it computes the target from `engine.modifierMask` and `pageSet.isPage(PAGE_FN)` and calls `switchPage` only when the target differs from `page`. Call it after every engine call that can change modifiers: the touch boundary, `onTimer`, `resetState`, and after a language switch. It must allocate nothing.
- [ ] **Step 4: Run** the ime tests. Expected: PASS.
- [ ] **Step 5: Commit** `feat(ime): fn opens the fn page when the layout has one` after the gate.

### Task 5: Full layout style setting

**Files:**
- Create: `settings/src/main/java/app/buckykeys/keyboard/settings/keyboard/domain/model/FullLayoutStyle.kt`.
- Modify:
  - `KeyboardSettings.kt`: add `val fullLayoutStyle: FullLayoutStyle = FullLayoutStyle.CENTRED`;
  - `KeyboardLanguage.kt`: add a third constructor value `classicLayoutId`, with `EN_US` → `"pc_hk_en_us"`, `EL` → `"pc_hk_el"`, `ET` → `"pc_hk_et"`;
  - `KeyboardSettingsViewModel.kt`: `fun onFullLayoutStyleChange(style: FullLayoutStyle)`, which mirrors `onOutsideTerminalsChange`;
  - `KeyboardSettingsEntry.kt`;
  - `KeyboardSettingsScreen.kt`: a heading plus `RadioRow`s right after the Languages section, mirroring the OutsideTerminals block;
  - `settings/src/main/res/values/strings.xml` and `values-el/strings.xml`.
- Test: `KeyboardSettingsSerializerTest.kt`, `KeyboardSettingsTest.kt`, and `app/src/test/.../settings/KeyboardLanguageLayoutsTest.kt`.

**Interfaces:**
- Produces: `@Serializable public enum class FullLayoutStyle { @SerialName("CENTRED") CENTRED, @SerialName("HK_CLASSIC") HK_CLASSIC }` and `KeyboardLanguage.classicLayoutId: String`.
- Strings:
  - `settings_full_style`: "Full layout style" / "Στυλ πλήρους διάταξης";
  - `settings_full_style_centred`: "Centred" / "Κεντραρισμένη";
  - `settings_full_style_classic`: "Hacker's Keyboard (classic)" / "Hacker's Keyboard (κλασική)".

- [ ] **Step 1: Write the failing tests:**
  - `` `settings saved before the layout style load as centred` ``: deserialize the existing JSON fixture without the field and assert `CENTRED`.
  - `` `an unknown layout style loads as centred` ``: `"fullLayoutStyle":"WHATEVER"`, which `coerceInputValues` handles.
  - `` `the classic style round trips` ``.
  - In `KeyboardLanguageLayoutsTest`: `` `every settings language names a classic layout that parses` ``, mirroring the compact test.
- [ ] **Step 2: Run** `./gradlew :settings:testDebugUnitTest :app:testFossDebugUnitTest`. Expected: FAIL.
- [ ] **Step 3: Implement** the enum, the field, the language IDs, the view model, entry and screen wiring, and the strings.
- [ ] **Step 4: Run** the tests. Expected: PASS.
- [ ] **Step 5: Commit** `feat(settings): full layout style, centred or hacker's keyboard classic` after the gate.

### Task 6: Service uses the classic layouts

**Files:**
- Create: `app/src/main/java/app/buckykeys/keyboard/profile/FullLayouts.kt`.
- Modify: `app/src/main/java/app/buckykeys/keyboard/BuckyImeService.kt` (`preload`, `fullLanguages`, `keyboardView`).
- Test: create `app/src/test/java/app/buckykeys/keyboard/profile/FullLayoutsTest.kt`.

**Interfaces:**
- Consumes: `FullLayoutStyle`, `KeyboardLanguage.classicLayoutId` (Task 5), and `BuiltInLayouts.PC_HK_FN` and `PAGE_FN` (Task 3).
- Produces:
  - `internal fun fullLayoutIds(languages: List<KeyboardLanguage>, style: FullLayoutStyle): List<String>`, which returns the classic IDs for `HK_CLASSIC` and the `layoutId`s otherwise.
  - `internal fun fullLayoutFor(language: KeyboardLanguage, style: FullLayoutStyle, loaded: (String) -> Layout?): Layout?`, which tries the classic ID, then `layoutId`, then returns null. The caller already falls back to `FallbackLayout`.
  - `internal fun fullPagesFor(style: FullLayoutStyle, loaded: (String) -> Layout?): Map<String, Layout>`, which returns `mapOf(PAGE_FN to it)` for classic when `PC_HK_FN` loaded, and empty otherwise.

- [ ] **Step 1: Write the failing tests** in `FullLayoutsTest` (use a lambda over a map, with no Android dependency):
  - `` `the centred style keeps the full layout ids` ``;
  - `` `the classic style uses the classic ids` ``;
  - `` `a classic layout that fails to load falls back to the centred one` ``;
  - `` `the classic style adds the fn page` ``;
  - `` `the centred style has no fn page` ``.
- [ ] **Step 2: Run** `./gradlew :app:testFossDebugUnitTest`. Expected: FAIL.
- [ ] **Step 3: Implement** the three functions. In the service:
  - `preload` also loads `fullLayoutIds(...)` and `PC_HK_FN` when the style is classic;
  - `fullLanguages()` maps through `fullLayoutFor` and keeps `withExtraRows`;
  - `keyboardView` passes `fullPagesFor(settings.fullLayoutStyle, layouts::get)` when not compact and not fallback, with the extra rows applied to the Fn page too, so the height doesn't jump;
  - `viewKey` keeps `fullLayoutStyle`, so changing it rebuilds the view.
- [ ] **Step 4: Run** the full gate. Expected: BUILD SUCCESSFUL.
- [ ] **Step 5: Device check:**
  - **S23** (release build over alpha4): set the style to classic. In Termux, check the positions against the spec table. Fn one-shot: Fn, F5, back to the main layer. Fn lock: two taps, the page stays. Hold Fn and tap F5. In `cat -v`: Fn F1 shows `^[OP`, ESC shows `^[`, and Meta+a reaches the app.
  - **Estonian:** `<>` sits left of z.
  - **Restore:** style Centred, label size 100%, the Samsung IME.
  - **Android 17 emulator:** the same flow, on my own pinned serial.
- [ ] **Step 6: Commit** `feat(app): hacker's keyboard classic layout in terminals and the full profile` after the gate.

### Task 7: Docs and PR

**Files:**
- Modify: `PLAN.md`:
  - §3.2: classic layout in v1;
  - §4.3: the classic grid, the Fn page and the Meta modifier, with the decision date 2026-10-27;
  - §4.6: the style applies wherever the full layout shows.
- Modify: `README.md` ("What works today": the classic layout, ❖ Meta and the Fn page), and commit `docs/specs/2026-10-27-hk-classic-layout-design.md` and this plan.

- [ ] **Step 1:** Update the docs, run the gate and commit `docs: hacker's keyboard classic layout`.
- [ ] **Step 2:** Push the branch `feat/hk-classic-layout`. Open a PR whose body lists Tasks 1–6, the device checks and the spec link, with no Claude footer. Wait for CI (`changes`, `android`, `site`, `dco`), then `gh pr merge --merge --delete-branch`.
