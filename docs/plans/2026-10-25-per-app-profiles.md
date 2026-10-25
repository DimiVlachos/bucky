# Per-app Profiles and Compact Layout Implementation Plan

> **Done (#47, 2026-10-25), and superseded by the code where they differ:** `rawEnter` follows the terminal list (and `TYPE_NULL`), not the profile; the compact height is `compactRowHeightPx(rowHeightPx(full rows))`; a hidden ⌨ becomes tap-to-full. See PLAN.md §4.4–§4.6.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show the full PC layout in terminal apps and, elsewhere, whatever the user picked in Settings: a phone-style compact layout (the default), the full layout, or a hand-off to their other keyboard.

**Architecture:**
- The layout model gains the pieces compact pages need: row insets, page-switch targets, a hold target on switch keys, and a key-level `then` switch. The compact pages are JSON assets like today's layouts.
- A pure `profileFor` function in `:app` picks the profile for each field. `BuckyImeService` applies it in `onStartInputView`, rebuilding the input view only when the profile changes.
- Phone conveniences live at the edges, so `KeyboardEngine` stays Android-free:
  - Enter actions and double-space-period go in `InputDispatcher`.
  - Auto-capitalisation is driven by the service from `onUpdateSelection`, through a new engine call.

**Tech Stack:** Kotlin, Android InputMethodService, Canvas view, Jetpack Compose (Settings), DataStore with kotlinx.serialization, Hilt, JUnit4.

**Spec:** `docs/specs/2026-10-25-per-app-profiles-design.md` (mockups in `docs/specs/2026-10-25-per-app-profiles-mockups/`).

## Global Constraints

- `:ime` never imports Compose, billing, Play Services, settings, DataStore, DI or coroutines. `KeyboardEngine` imports nothing from Android except `KeyEvent` constants.
- `:settings` may not depend on `:ime`.
- No allocations in `KeyboardView.onDraw` or `onTouchEvent`. Precompute in `buildLabels` and geometry.
- The IME never crashes. A layout that fails to parse falls back to `FallbackLayout.pcFullEnUs`.
- No user-facing hardcoded strings: EN in `values/`, EL in `values-el/`.
- No `INTERNET`, no `QUERY_ALL_PACKAGES`, no `<queries>`.
- No deprecated APIs. On API 26–27, hand-off and ⌨ are unavailable: Bucky uses Compact and hides ⌨.
- No `!!`, no comments unless a one-line non-obvious why, explicit API mode.
- Terminal packages: `com.termux`, `org.connectbot`, `com.sonelli.juicessh`, `com.server.auditor.ssh.client`.
- Tests: JUnit4, `org.junit.Assert`, backtick sentence names, hand-written fakes in `fake/`.
- Gate before each commit: `./gradlew spotlessApply`, then `./gradlew detekt :detekt-rules:test testFossDebugUnitTest testDebugUnitTest :app:architectureTest lintFossDebug assembleFossDebug assemblePlayDebug`, then `scripts/check-manifest-permissions.sh`. Commit only on BUILD SUCCESSFUL. Use `git commit -s` with the session attribution lines.
- Branch: `feat/per-app-profiles`. One commit per task, one PR at the end.

## Review Focus

1. **Switching apps mid-gesture or with a modifier latched** (for example Shift locked in WhatsApp, then switch to Termux): the new view must start clean, with no stuck Shift and no auto-shift carried across. Test in Task 8.
2. **A symbol page open when the field changes:** the next field opens on the letters page. Device check in Task 8.
3. **Fields with no `InputConnection`, or `getCursorCapsMode` / `getTextBeforeCursor` returning null** (some apps): auto-cap and double-space do nothing, with no crash. Tests in Tasks 5 and 8.
4. **"Switch to my other keyboard" with no other keyboard enabled, or on API 26–27:** stay on Compact and never call a switch that fails silently and leaves the field keyboard-less. Test in Task 7.
5. **Enter in a multi-line field** that also declares `IME_ACTION_SEND` (many chat apps set `IME_FLAG_NO_ENTER_ACTION`): it must insert a newline, not send. Test in Task 5.

---

### Task 1: Layout model and parser for compact pages

**Files:**
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/layout/Layout.kt`, `KeyDef.kt`, `LayoutParser.kt`, `BuiltInLayouts.kt`, `DefaultLabels.kt`, `LayoutTransforms.kt`
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/geometry/KeyboardGeometry.kt`
- Test: `ime/src/test/java/app/buckykeys/keyboard/ime/layout/LayoutParserTest.kt`, `LayoutTransformsTest.kt`, `ime/src/test/java/app/buckykeys/keyboard/ime/geometry/KeyboardGeometryTest.kt`

**Interfaces:**
- Produces:
  - `Row(keys: List<KeySpec>, inset: Float = 0f)`: `inset` is the empty width, in key units, at each end of the row.
  - `KeySpec(..., then: String? = null)`: a switch target requested right after this key types.
  - `KeyDef.LayoutSwitch(targetId: String, holdTargetId: String? = null)`.
  - Constants in `BuiltInLayouts`: `PAGE_LETTERS = "@page:letters"`, `PAGE_SYMBOLS = "@page:symbols"`, `PAGE_SYMBOLS_MORE = "@page:symbols-more"`, `OTHER_KEYBOARD = "@keyboard"`, `FULL_LAYOUT = "@full"`.
  - `Layout.withoutSwitchKey(targetId: String): Layout`, generalised from `withoutLanguageKey()`. Keep `withoutLanguageKey()` as `withoutSwitchKey(NEXT_LANGUAGE)`.
- JSON:
  - row field `"inset": 0.5`
  - key field `"then": "@page:letters"`
  - switch key `{ "switchTo": "@keyboard", "holdTo": "@full", "label": "⌨" }`
  - `switchTo` accepts `@language`, `@page:letters`, `@page:symbols`, `@page:symbols-more` and `@keyboard`; `holdTo` accepts only `@full`.

- [ ] **Step 1: Write failing tests**
  - `LayoutParserTest`:
    - `` `a row can have an inset` ``: `{"inset":0.5,"keys":[…]}` gives `row.inset == 0.5f`.
    - `` `a key can name a switch to follow it` ``: `"then":"@page:letters"` gives `spec.then == "@page:letters"`.
    - `` `a switch key can name a hold target` ``: gives `LayoutSwitch("@keyboard", "@full")`.
    - `` `page targets parse` ``, one per page constant.
    - `` `an unknown page target is rejected` ``: `"@page:emoji"` fails, path `$.rows[0].keys[0].switchTo`.
    - `` `holdTo only accepts the full layout` ``.
    - `` `a negative inset is rejected` ``.
  - `KeyboardGeometryTest`:
    - `` `an inset row starts half a key in` ``: one row with inset 0.5, 9 keys, 1000 px. The first key's left is `1000/10*0.5 = 50f` and its width is `100f`.
    - `` `a tap in the inset goes to the edge key` ``: `keyAt(10f, …)` is the first key, and `keyAt(995f, …)` is the last.
  - `LayoutTransformsTest`: `` `removing the keyboard key gives its width to the widest key` ``.
- [ ] **Step 2: Run** `./gradlew :ime:testDebugUnitTest`. Expected: the new tests FAIL (unresolved fields).
- [ ] **Step 3: Implement.**
  - `KeyboardGeometry`: lays out each row as `unit = widthPx / (sum(widths) + 2*inset)`, with the first key starting at `inset * unit`. `keyAt` maps x left of the first key's left edge to the first key, and x right of the last key's right edge to the last key.
  - `DefaultLabels.of(LayoutSwitch)`: `"?123"` for `PAGE_SYMBOLS`, `"ABC"` for `PAGE_LETTERS`, `"=\\<"` for `PAGE_SYMBOLS_MORE`, and `"⌨"` for `OTHER_KEYBOARD`.
- [ ] **Step 4: Run** `./gradlew :ime:testDebugUnitTest`. Expected: PASS, with no existing test changed.
- [ ] **Step 5: Gate, then commit** `feat(ime): layout model for compact pages (insets, page switches, hold targets)`.

### Task 2: Engine support: hold-to-switch, `then`, auto-shift

**Files:**
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/engine/KeyboardEngine.kt`
- Test: `ime/src/test/java/app/buckykeys/keyboard/ime/engine/KeyboardEngineTest.kt`

**Interfaces:**
- Consumes: Task 1's `LayoutSwitch.holdTargetId` and `KeySpec.then`.
- Produces:
  - `KeyboardEngine.setAutoShift(on: Boolean)`: sets or clears an automatic one-shot Shift. It is consumed like a user one-shot by the next key. Tapping Shift while it is on clears it instead of locking. It is cleared by `reset()`.
  - `KeyboardEngine.autoShift: Boolean`, read-only.
  - Switch requests: a held `LayoutSwitch` with a hold target requests `holdTargetId` at the long-press deadline and does not request `targetId` on release. A key with `then` requests `then` after it emits.

- [ ] **Step 1: Write failing tests** (use `useRow` with synthetic keys):
  - `` `holding the keyboard key requests the full layout` ``: down, tick past `LONG_PRESS_MS`, up. `takeSwitchRequest() == "@full"`, then `null`.
  - `` `tapping the keyboard key requests the other keyboard` ``: `"@keyboard"`.
  - `` `a key with then types and then requests the switch` ``: a space with `then = "@page:letters"`. Emitted `[Text(" ")]` and the request `"@page:letters"`.
  - `` `auto shift capitalises the next letter and clears` ``: `setAutoShift(true)`, tap `c`. `[Text("C")]`, then `autoShift == false`.
  - `` `tapping shift while auto shift is on turns it off` ``: then tap `c` gives `[Text("c")]` and `lockedMask == 0`.
  - `` `auto shift does not survive reset` ``.
- [ ] **Step 2: Run** `./gradlew :ime:testDebugUnitTest --tests '*KeyboardEngineTest*'`. Expected: the new tests FAIL.
- [ ] **Step 3: Implement.**
  - Hold timer: start the long-press timer in `press` for a `LayoutSwitch` with a non-null `holdTargetId`. Fire it in `onTick`'s non-repeat branch.
  - Auto shift: keep it as its own bit merged into `modifierMask`, so `tapModifier` can tell it apart from a user one-shot.
- [ ] **Step 4: Run** the full `:ime` tests. Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(ime): hold-to-switch keys, follow-up switches and auto shift in the engine`.

### Task 3: Compact layout assets

**Files:**
- Create: `ime/src/main/assets/layouts/compact_en_us.json`, `compact_el.json`, `compact_symbols.json`, `compact_symbols_more.json`
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/layout/BuiltInLayouts.kt` (constants `COMPACT_EN_US = "compact_en_us"`, `COMPACT_EL = "compact_el"`, `COMPACT_SYMBOLS = "compact_symbols"`, `COMPACT_SYMBOLS_MORE = "compact_symbols_more"`)
- Test: `ime/src/test/java/app/buckykeys/keyboard/ime/layout/CompactLayoutsTest.kt`

**Interfaces:**
- Produces four layouts with these exact rows (widths in units):
  - **`compact_en_us`** (`language: "English (US)"`):
    - row 1: `q`–`p` (each `longPress` its digit, `1` to `0`, plus the letter alternates that `pc_full_en_us` has for that letter, capital first)
    - row 2: `inset 0.5`, `a`–`l` with the same alternates as `pc_full_en_us`
    - row 3: `SHIFT` 1.5, `z`–`m`, `BACKSPACE` 1.5
    - row 4: `@page:symbols` 1.5, `,` (`longPress ["!"]`), `@keyboard` with `holdTo @full`, space 4, `.` (`longPress ["?"]`), `ENTER` 1.5
  - **`compact_el`** (`language: "Ελληνικά"`):
    - row 1: `; ς ε ρ τ υ θ ι ο π` (digits `1`–`0` first in `longPress`, then the vowel alternates that `pc_full_el` has)
    - row 2: `α σ δ φ γ η ξ κ λ` plus `{ "dead": ["΄", "¨"] }`, no inset
    - rows 3 and 4 as English, with `ζ χ ψ ω β ν μ`, and Greek letters keeping their `latin` fields from `pc_full_el`
  - **`compact_symbols`:**
    - `1 2 3 4 5 6 7 8 9 0`
    - `@ # € _ & - + ( ) /`, with `longPress`: `€→$`, `-→–`, `/→\`
    - `@page:symbols-more` 1.5, `* " ' : ; ! ? `, `BACKSPACE` 1.5, with `"→“` and `'→’`
    - `@page:letters` 1.5, `,`, `@keyboard`/`@full`, space 4 with `then: "@page:letters"`, `.` (`→…`), `ENTER` 1.5
  - **`compact_symbols_more`:**
    - ``~ ` | • √ π ÷ × ¶ ∆``
    - `£ ¥ ^ ° = { } \ % ©`
    - `inset 0.5`: `@page:symbols` 1.5, `® ™ [ ] < >`, `BACKSPACE` 1.5
    - the same bottom row as `compact_symbols`

- [ ] **Step 1: Write failing tests** in `CompactLayoutsTest` (read the files from `src/main/assets/layouts/`, as `BuiltInLayoutsTest` does):
  - `` `every compact page parses` ``
  - `` `every compact row spans ten units` `` (sum of widths + 2×inset == 10)
  - `` `holding a top-row letter offers its digit` ``: `q` alternates contain `1`, and `p` contains `0`.
  - `` `the greek compact layout has the tonos dead key` ``
  - `` `every compact page can get back to letters or symbols` ``: each page has at least one `@page:*` key.
  - `` `the keyboard key holds to the full layout on every page` ``
  - `` `a space on a symbol page returns to letters` ``
- [ ] **Step 2: Run.** Expected: FAIL (files missing).
- [ ] **Step 3: Write the four JSON files** as specified.
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(ime): phone-style compact layouts for EN and EL with symbol pages`.

### Task 4: KeyboardView pages and actions

**Files:**
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/view/KeyboardView.kt`
- Create: `ime/src/main/java/app/buckykeys/keyboard/ime/view/KeyboardActions.kt`, `ime/src/main/java/app/buckykeys/keyboard/ime/view/PageSet.kt`
- Test: `ime/src/test/java/app/buckykeys/keyboard/ime/view/PageSetTest.kt`

**Interfaces:**
- Consumes: Task 1's constants and Task 2's switch requests.
- Produces:
  - `public fun interface KeyboardActions { public fun onAction(target: String) }`. The view forwards `@keyboard` and `@full` to it and handles language and page targets itself.
  - `public class PageSet(letters: List<Layout>, pages: Map<String, Layout>)`, a pure class:
    - `current(languageIndex: Int, page: String): Layout`
    - `isPage(target: String): Boolean`
    - Unknown pages fall back to letters.
  - `KeyboardView(... , pages: Map<String, Layout> = emptyMap(), actions: KeyboardActions = KeyboardActions {})`
  - `KeyboardView.setAutoShift(on: Boolean)` forwards to the engine and invalidates.
  - `KeyboardView.resetState()` also returns to the letters page.

- [ ] **Step 1: Write failing tests** in `PageSetTest`:
  - `` `letters follow the language` ``
  - `` `a symbol page is shared across languages` ``
  - `` `an unknown page falls back to letters` ``
  - `` `page targets are recognised and others are not` ``
- [ ] **Step 2: Run.** Expected: FAIL.
- [ ] **Step 3: Implement `PageSet`, then wire `KeyboardView`.**
  - A page switch changes the current page and rebuilds geometry, labels and the engine geometry, like a language switch, posted with a pre-built `Runnable`.
  - A language swipe on a symbol page switches language and returns to letters.
  - `languageSwipe` stays on only when there are 2+ languages.
  - The space label shows the language only on the letters page.
- [ ] **Step 4: Run** the `:ime` tests. Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(ime): page switching and keyboard actions in KeyboardView`.

### Task 5: Dispatcher: Enter actions and double-space period

**Files:**
- Modify: `ime/src/main/java/app/buckykeys/keyboard/ime/dispatch/InputDispatcher.kt`
- Create: `ime/src/main/java/app/buckykeys/keyboard/ime/dispatch/TypingRules.kt`
- Test: `ime/src/test/java/app/buckykeys/keyboard/ime/dispatch/TypingRulesTest.kt`

**Interfaces:**
- Produces:
  - `public fun enterAction(imeOptions: Int, inputType: Int, rawEnter: Boolean): Int`. It returns an `EditorInfo.IME_ACTION_*` value to perform, or `NO_ENTER_ACTION = -1` to send `KEYCODE_ENTER`.
  - `public fun shouldInsertPeriod(before: CharSequence?, sinceLastSpaceMs: Long): Boolean`. It is true when `before` ends with a letter or digit followed by one space and `sinceLastSpaceMs <= 1000`.
  - `InputDispatcher` gains settable fields, set by the service per field: `rawEnter: Boolean = true`, `imeOptions: Int = 0`, `inputType: Int = 0`, `doubleSpacePeriod: Boolean = false`.
  - `sendKey(KEYCODE_ENTER, 0)` performs the editor action when `enterAction(...) != NO_ENTER_ACTION`.
  - `commitText(" ")` replaces the previous space with `". "` when `doubleSpacePeriod && shouldInsertPeriod(getTextBeforeCursor(2, 0), elapsed)`.
  - Every `InputConnection` call no-ops on null.

- [ ] **Step 1: Write failing tests** in `TypingRulesTest`. The `EditorInfo` and `InputType` constants are compile-time, so these are pure JVM tests.
  - `` `raw enter always sends the key` ``
  - `` `a send field performs send` ``: `IME_ACTION_SEND` gives `IME_ACTION_SEND`.
  - `` `no enter action flag sends the key` ``: `IME_ACTION_SEND or IME_FLAG_NO_ENTER_ACTION` gives `-1`.
  - `` `a multi line field sends the key` ``: `TYPE_TEXT_FLAG_MULTI_LINE` with `IME_ACTION_SEND` gives `-1`.
  - `` `an unspecified action sends the key` ``
  - `` `a letter and a space then a quick space inserts a period` ``: `"hi "`, 300 gives true.
  - `` `a slow second space does not` ``: `"hi "`, 1500 gives false.
  - `` `two spaces in a row do not` ``: `"  "` gives false.
  - `` `nothing before the cursor does not` ``: `null` and `""` give false.
- [ ] **Step 2: Run.** Expected: FAIL.
- [ ] **Step 3: Implement** the two functions and the dispatcher changes. Use `SystemClock.uptimeMillis()` for the space timing.
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(ime): honour Enter actions and double-space period outside terminals`.

### Task 6: Settings: Apps section

**Files:**
- Create: `settings/src/main/java/app/buckykeys/keyboard/settings/keyboard/domain/model/OutsideTerminals.kt`
- Modify: `KeyboardSettings.kt`, `KeyboardLanguage.kt`, `KeyboardSettingsViewModel.kt`, `KeyboardSettingsEntry.kt`, `KeyboardSettingsScreen.kt`, `components/SettingRows.kt`, `values/strings.xml`, `values-el/strings.xml`
- Test: `KeyboardSettingsTest.kt`, `KeyboardSettingsSerializerTest.kt`, `KeyboardSettingsViewModelTest.kt`

**Interfaces:**
- Produces:
  - `@Serializable public enum class OutsideTerminals { @SerialName("COMPACT") COMPACT, @SerialName("FULL") FULL, @SerialName("OTHER_KEYBOARD") OTHER_KEYBOARD }`
  - New `KeyboardSettings` fields: `outsideTerminals: OutsideTerminals = OutsideTerminals.COMPACT`, `autoCapitalize: Boolean = true`, `doubleSpacePeriod: Boolean = true`.
  - `KeyboardLanguage(layoutId: String, compactLayoutId: String)`: `EN_US("pc_full_en_us", "compact_en_us")`, `EL("pc_full_el", "compact_el")`.
  - ViewModel handlers: `onOutsideTerminalsChange(OutsideTerminals)`, `onAutoCapitalizeChange(Boolean)`, `onDoubleSpacePeriodChange(Boolean)`.
  - `RadioRow(text: String, description: String?, selected: Boolean, onClick: () -> Unit)` in `SettingRows.kt`.
- Strings (EN / EL):

| Key | EN | EL |
|---|---|---|
| `settings_apps` | Apps | Εφαρμογές |
| `settings_outside_terminals` | Outside terminal apps | Εκτός εφαρμογών τερματικού |
| `settings_outside_compact` | Compact layout | Συμπαγής διάταξη |
| `settings_outside_full` | Full layout | Πλήρης διάταξη |
| `settings_outside_other` | Switch to my other keyboard | Εναλλαγή στο άλλο μου πληκτρολόγιο |
| `settings_outside_other_hint` | To get Bucky back in Termux, use the keyboard switcher button. | Για να επιστρέψει το Bucky στο Termux, χρησιμοποιήστε το κουμπί εναλλαγής πληκτρολογίου. |
| `settings_auto_capitalize` | Capital at the start of a sentence | Κεφαλαίο στην αρχή της πρότασης |
| `settings_double_space_period` | Double space for a full stop | Διπλό space για τελεία |

- [ ] **Step 1: Write failing tests:**
  - `` `apps default to compact with both helpers on` ``
  - `` `settings saved before profiles load with compact` `` (serializer)
  - `` `an unknown outside terminals value falls back to compact` `` (serializer)
  - `` `an outside terminals change is saved` `` (view model)
- [ ] **Step 2: Run** `./gradlew :settings:testDebugUnitTest`. Expected: FAIL.
- [ ] **Step 3: Implement** the model, serializer fallback, ViewModel, `RadioRow`, and the "Apps" section placed after "Extra rows". The two switches are enabled only when the choice isn't `FULL`.
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(settings): Apps section for the layout outside terminal apps`.

### Task 7: Profile selection

**Files:**
- Create: `app/src/main/java/app/buckykeys/keyboard/profile/Profiles.kt`
- Test: `app/src/test/java/app/buckykeys/keyboard/profile/ProfilesTest.kt`

**Interfaces:**
- Consumes: Task 6's `OutsideTerminals`.
- Produces:
  - `enum class Profile { FULL, COMPACT, HAND_OFF }`
  - `fun profileFor(packageName: String?, outside: OutsideTerminals, ownPackage: String, forcedFull: Boolean, canHandOff: Boolean): Profile`
  - `val TERMINAL_PACKAGES: Set<String>` (the four in Global Constraints)

- [ ] **Step 1: Write failing tests:**
  - `` `each terminal app gets the full layout whatever the setting` `` (all four × all three settings)
  - `` `other apps follow the setting` ``
  - `` `bucky's own app always gets the full layout` ``
  - `` `a forced full layout wins outside terminals` ``
  - `` `hand off falls back to compact when it can't hand off` ``
  - `` `a null package follows the setting` ``
- [ ] **Step 2: Run** `./gradlew :app:testFossDebugUnitTest --tests '*ProfilesTest*'`. Expected: FAIL.
- [ ] **Step 3: Implement `profileFor`.**
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5: Gate, then commit** `feat(app): pick the keyboard profile per app`.

### Task 8: Service wiring and device check

**Files:**
- Modify: `app/src/main/java/app/buckykeys/keyboard/BuckyImeService.kt`
- Modify: `PLAN.md` (§3.2: compact layout and built-in profiles done; §4.5: Enter, auto-cap and double space; §4.6: the three options, the ⌨ key, hand-off limits; §4.3: page JSON fields), `README.md` (What works and Planned)

**Interfaces:**
- Consumes everything above.

- [ ] **Step 1: Implement in `BuckyImeService`:**
  - `onStartInputView(info, restarting)`:
    - Clear `forcedFull` when `info.packageName` differs from the last package.
    - Compute `canHandOff = Build.VERSION.SDK_INT >= 28 && shouldOfferSwitchingToNextInputMethod()`.
    - Then `profileFor(...)`. On `HAND_OFF`, call `switchToPreviousInputMethod()`; if it returns false, use `COMPACT`.
    - Rebuild with `setInputView(onCreateInputView())` only when the profile or settings changed. Otherwise call `resetState()`.
    - Configure the dispatcher: `rawEnter = profile == FULL`, `imeOptions`, `inputType`, and `doubleSpacePeriod` (compact, setting on, and not a password or `TYPE_TEXT_FLAG_NO_SUGGESTIONS` field).
  - `onCreateInputView()`:
    - FULL builds today's view.
    - COMPACT builds it from each language's `compactLayoutId`, with `pages = {PAGE_SYMBOLS → compact_symbols, PAGE_SYMBOLS_MORE → compact_symbols_more}`, no extra rows, and `withoutSwitchKey(OTHER_KEYBOARD)` when `!canHandOff`.
    - Row height: `cappedRowHeightPx(fullRowPx * 5 / 4, 4, …)`, where `fullRowPx` is today's chosen height.
  - Actions: `@keyboard` calls `switchToPreviousInputMethod()`. `@full` sets `forcedFull = true` and rebuilds.
  - `onUpdateSelection(...)`: in COMPACT with `autoCapitalize` on, call `view.setAutoShift(currentInputConnection?.getCursorCapsMode(inputType) ?: 0 != 0)`. Also run this once at `onStartInputView`.
  - Preload the compact assets in `preload`, with the same fallback.
- [ ] **Step 2: Gate.** Expected: BUILD SUCCESSFUL.
- [ ] **Step 3: Device check on the S23 Ultra** (release build, `adb install -r`). Restore the Samsung IME, rotation and settings afterwards.
  - Termux shows full; Samsung Notes or WhatsApp shows compact; switching back and forth needs no taps.
  - `?123`, `=\<` and `ABC` work; a space on `?123` returns to letters.
  - Long-press ⌨ shows full until the next app; tap ⌨ goes to Samsung's keyboard.
  - "Full layout" gives full in WhatsApp; "Switch to my other keyboard" hands off in WhatsApp and Termux needs the switcher.
  - Enter sends in WhatsApp and inserts a newline in a Notes body.
  - A sentence capital and double space work in Notes, and never in Termux (check with `cat -v`).
  - The Greek compact layout and tonos work.
  - Review Focus 1: Shift locked in WhatsApp, then switch to Termux, starts clean.
  - Review Focus 2: leave a field on `?123`, then open another field; it starts on letters.
- [ ] **Step 4: Update PLAN.md and README.md** as listed.
- [ ] **Step 5: Gate, commit** `feat: per-app profiles with a phone-style compact layout`, push, open the PR, then merge after CI (changes, android, site, dco) passes.
