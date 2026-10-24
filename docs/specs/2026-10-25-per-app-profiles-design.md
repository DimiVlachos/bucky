# Per-app profiles and the compact layout: design

Date: 2026-10-25. Status: draft for Dimitris's review, after a builder check. Decisions move into `PLAN.md` (§3.2, §4.3, §4.5, §4.6, §4.7) in the same PR that implements them.

## Why

The main objection on r/termux and hackerskeyboard#875 was "it's too cramped": the full PC layout is great in a terminal and too small everywhere else. The r/termux post promised "PC Full in Termux, and a compact layout or your usual keyboard elsewhere". Today Bucky shows the full layout in every app. This is the main gap listed in the alpha1 and alpha2 release notes.

Two kinds of users have shown up:
- **Hacker's Keyboard veterans** (ketas used HK everywhere for 10 years). They want Bucky in every app, smaller outside terminals.
- **Gboard plus Termux users**. They want their usual keyboard (prediction, swipe, voice) outside terminals.

## Goals

1. In terminal apps, Bucky always shows the full PC layout, as today.
2. Outside terminal apps, the user picks one of three behaviours in Settings:
   - **Compact layout** (default): Bucky stays active and shows a phone-style layout.
   - **Full layout**: the PC layout everywhere, like HK.
   - **Switch to my other keyboard**: Bucky hands off to the previous keyboard.
3. Changing apps changes the layout automatically, with no taps, in the Compact and Full modes.
4. The compact layout looks and behaves like a normal phone keyboard. Dimitris: "since it's outside terminals it should look like a normal keyboard".

## Non-goals

- Prediction, autocorrect, swipe typing (PLAN "Won't (v1)").
- Number and phone layouts for `inputType` number and phone, and password-field behaviour. They are separate "Should" items in PLAN §3.2.
- Custom per-app profiles chosen by the user. They stay Pro, v1.1 (PLAN §4.6).
- Listing installed apps in Settings. Not needed for built-in profiles, so no `<queries>` and never `QUERY_ALL_PACKAGES`.

## Anti-goals (a failure even if it works)

- Switching away from Bucky without telling the user they'll have to switch back by hand. Android cannot bring Bucky back on its own (see Constraints).
- A compact layout that looks like a squeezed PC keyboard. Dimitris picked phone-style over an HK-style 4-row PC layout.
- Auto-capitalisation or double-space-to-period ever firing in a terminal app.
- Any allocation in `KeyboardView.onDraw` or `onTouchEvent`, and any crash path (CLAUDE.md rules still apply).

## Constraints

- **Android can switch away but not back.** `InputMethodService.switchToPreviousInputMethod()` (API 28+; minSdk is 26, so on 26–27 use `InputMethodManager.switchToLastInputMethod(token)` or fall back to Compact) hands off to another enabled keyboard. Once that keyboard is active, Bucky isn't running, Android has no per-app keyboard setting, and Gboard and Samsung's keyboard won't switch back. Option 3 therefore costs the user a keyboard-switcher tap every time they return to a terminal. The setting's description must say so.
- `:ime` stays free of settings, DataStore, coroutines and DI (Konsist). `KeyboardEngine` imports nothing from Android except `KeyEvent` constants.
- `:settings` may not depend on `:ime`.
- Every user-facing string goes in EN `values/` and EL `values-el/`.
- No `INTERNET`, no `QUERY_ALL_PACKAGES`.

## Decided (by Dimitris)

- **The three options** above, with Compact as the default.
- **Compact is phone-style (option A in `2026-10-25-per-app-profiles-mockups/compact-layout.html`; the pages are in `compact-pages.html`):**
  - Letters: `q w e r t y u i o p` / half gap, `a s d f g h j k l`, half gap / `Shift z x c v b n m ⌫` / `?123 , ⌨ [space] . Enter`.
  - Top-row letters long-press to digits 1–0, with hints. `,` and `.` have long-press hints too (`!` and `?` in the mockup).
  - **`?123` page:** `1 2 3 4 5 6 7 8 9 0` / `@ # € _ & - + ( ) /` / `=\< * " ' : ; ! ? ⌫` / `ABC , ⌨ [space] . Enter`. Long-press extras: € → $, - → –, / → \, " → “, ' → ’, . → ….
  - **`=\<` page:** the rarer symbols, in rows `~ \` | • √ π ÷ × ¶ ∆` / `£ ¥ ^ ° = { } \ % ©` / `?123 ® ™ [ ] < > ⌫` (half gaps at the row ends) / `ABC , ⌨ [space] . Enter`, the same bottom row as `?123`.
  - **Greek compact:** `; ς ε ρ τ υ θ ι ο π` / `α σ δ φ γ η ξ κ λ ΄` (tonos dead key, ¨ with Shift) / `Shift ζ χ ψ ω β ν μ ⌫` / `?123 , ⌨ [space] . Enter`. The symbol pages are shared with English.
- **Capital at the start of a sentence** and **double space → ". "** are in, in the compact layout.
- **Long-press ⌨ shows the full layout** until the next app switch, for SSH apps that aren't on the list. It's free. Remembering that per app stays Pro (custom profiles, v1.1).
- **Existing alpha users get Compact** after the update, like new users. The alpha3 release notes say where to switch back. No settings migration.
- **Compact keeps the keyboard's total height:** its 4 rows share the height the full layout's 5 main rows would take, so compact keys are about 25% taller. It follows the existing key-height sliders, with no new setting. The F-key row and the extra row don't count, since compact doesn't show them.
- The terminal list is built in and free: `com.termux`, `org.connectbot`, `com.sonelli.juicessh`, `com.server.auditor.ssh.client` (PLAN §4.6), with raw Enter.

## My calls (Claude), for Dimitris to check

- **Option 3 target:** the keyboard used before Bucky (`switchToPreviousInputMethod`), so there's nothing to pick. If there's no previous or other enabled keyboard, Bucky stays on Compact for that field.
- **Bucky's own app** (onboarding "try" field and Settings) always gets the full layout and never hands off.
- **`,` and `.` long-press** work like any key with alternates: a plain hold types the first one (`!` and `?` on letters, `…` on `?123`), and sliding picks the others.
- **⌨ key:** on compact pages only. It switches to the previous keyboard and is hidden when no other keyboard is enabled.
- **Symbol pages stay open** until `ABC`. After a space on a symbol page, Bucky returns to letters, as Gboard does.
- **Language swipe, label size, long-press alternates, key preview and vibration** apply to both layouts. The **F-key row and the user's extra row** appear only on the full layout.
- **Enter outside terminals** follows the field's `imeOptions` action (Send, Search, Go, Next, Done) via `performEditorAction`. It falls back to a newline in multi-line fields, and to `KEYCODE_ENTER` when the field gives no action. In terminal apps it is always raw `KEYCODE_ENTER`.
- **Auto-capitalisation** uses `InputConnection.getCursorCapsMode(EditorInfo.inputType)`, so it respects the field's own `TYPE_TEXT_FLAG_CAP_*` flags. It sets a one-shot Shift that the user can cancel by tapping Shift. It is off in the full layout.
- **Double space → ". "** replaces the space just typed with ". " when the character before it is a letter or digit and the second space comes within about 1 s. It is off in the full layout and in fields with `TYPE_TEXT_FLAG_NO_SUGGESTIONS` or a password variation.
- **Settings:** a new "Apps" section on the home screen with the three-way choice (radio buttons, the third with its warning text) and two switches, "Capital at the start of a sentence" and "Double space for a full stop", both on by default.
- **Terminal detection** is the package list only. Detecting unlisted SSH clients from their `inputType` was considered but depends on each app, so it is left out of this work.
- **A layout change on app switch** reuses today's mechanism (rebuild via `setInputView` when the effective layout set changes). It happens in `onStartInputView` before the view is shown, so there's no visible flash. If measuring shows a flash, the builder can keep both view instances.
- **Fallback:** if a compact JSON fails to parse, use the full fallback layout, as today. The IME never ends up without a keyboard.

## What's left to the builder

- How layout families and pages are modelled. Constraints: JSON data, parsed by `LayoutParser`. Page switches are new `switchTo` targets next to `@language` (for example `@page:symbols`). Pages switch inside the view without rebuilding the service view. One `Layout` per page is fine.
- Where profile selection lives. A pure function such as `profileFor(packageName, settings)`, unit-tested, called from `BuckyImeService.onStartInputView`, is the expected shape. `:ime` must not see `KeyboardSettings`.
- The `InputSink` additions for the text-aware features (`performEditorAction`, `getCursorCapsMode`, `textBeforeCursor(n)`, `deleteSurroundingText`) and how the engine asks for them without Android types. The dispatcher still no-ops on a null `InputConnection`.
- Key widths, fonts and visual tuning of the compact pages, matched to the mockups and checked on the S23 Ultra.

## Verification

- Unit tests: profile selection (each terminal package, an unknown package, each setting value); compact and page JSON parsing; page switching in the engine; auto-cap and double-space rules, including that neither ever fires on the full layout; Enter action mapping.
- On the device:
  - Termux → full layout. Chrome or WhatsApp → compact, automatically both ways.
  - The "Full layout" setting gives the full layout in WhatsApp.
  - "Switch to my other keyboard" hands off to Samsung's keyboard in WhatsApp; Termux needs the switcher.
  - Enter sends in WhatsApp and searches in a search box.
  - Sentence capitals and double space work in a notes app, and never in Termux.
  - The Greek compact layout and its tonos key work.
- The full gate, then `check-manifest-permissions.sh`.
