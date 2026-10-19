# Gate 0 posts (drafts)

PLAN.md §8: post as a question, not an ad. Disclose that you built it. Link the waitlist and the source. Both posts need the repo public first, because they link it.

Before posting:

- [ ] `DimiVlachos/bucky` is public (`gh repo edit DimiVlachos/bucky --visibility public --accept-visibility-change-consequences`)
- [ ] r/termux sidebar rules read: self-promotion allowed, required flair chosen
- [ ] Screenshot attached: `site/public/hero-vim.png`

---

## 1. Comment on klausw/hackerskeyboard#875

> **Rewriting it from scratch: what would you need from a successor?**
>
> This thread has been asking since 2021 for something that keeps Hacker's Keyboard's layout but runs on current Android. I'm building one from scratch rather than forking: new Kotlin code on today's APIs (target SDK 37), not a patched 2011 AOSP keyboard. <!-- optional: one sentence on your own HK use, only if true -->
>
> Where it stands, honestly: an early build that already types in Termux on my S23 Ultra. Ctrl, Alt and Shift are one-shot modifiers that send real key events, so Ctrl+C interrupts, Esc leaves vim's insert mode and the arrows walk shell history. The full 5-row layout has `~`, `|` and `\` where you expect them, and touch-to-screen latency is about 13 ms at the median. Not done yet: long-press, key repeat, F1–F12 and other layouts.
>
> The ground rules: Apache-2.0, no INTERNET permission at all, and everything Hacker's Keyboard did stays free. Optional extras later (a layout editor, themes) would pay for the work.
>
> Questions for the people in this thread:
>
> 1. Which Hacker's Keyboard feature would make you refuse a replacement if it were missing?
> 2. F-keys: a dedicated row, or an Fn layer?
> 3. Which layouts do you need? Dvorak came up in #939; what else?
> 4. Which apps do you mostly use it in (Termux, ConnectBot, JuiceSSH, something else)?
>
> Screenshot below: real vim in Termux, Esc just pressed, Ctrl latched.
> Source: https://github.com/DimiVlachos/bucky · Waitlist for the beta: https://buckykeys.app
>
> Klaus, thank you for HK. If you'd rather this not live in your tracker, say so and I'll move it.

---

## 2. r/termux post

**Title:** Building a from-scratch successor to Hacker's Keyboard for Termux. What do you actually need from it?

**Body:**

> Hacker's Keyboard is still the keyboard people recommend for Termux because it sends real Ctrl/Esc/Tab/arrow keys the way a terminal expects. It's also a 2011 codebase that its author no longer maintains and that is falling behind current Android. So I'm writing a successor from scratch, in Kotlin on current APIs. <!-- optional: one sentence on how you use Termux, only if true -->
>
> **What works today (early build):**
> - Full 5-row PC layout, with `~ | \` in the usual places
> - Ctrl/Alt/Shift as one-shot modifiers that send real KeyEvents: Ctrl+C interrupts `sleep`, Esc leaves vim's insert mode, arrows recall history
> - About 13 ms median touch-to-screen latency on an S23 Ultra
>
> **Not yet:** long-press alternates, key repeat, F1–F12, Dvorak/Colemak/other layouts, per-app profiles
>
> **Ground rules:** Apache-2.0, no INTERNET permission, and everything Hacker's Keyboard did stays free. A paid "Pro" later only covers extras like a layout editor and themes.
>
> I'd rather build what this sub needs than guess, so:
> 1. What's the one thing a terminal keyboard gets wrong for you today?
> 2. Do you also use Termux's extra-keys row? Would you turn it off if the keyboard had those keys?
> 3. F-keys: a dedicated row, or an Fn layer?
> 4. Do you use other apps with it (ConnectBot, JuiceSSH, Termius) that need anything special?
>
> Source: https://github.com/DimiVlachos/bucky
> Beta waitlist (one email when it opens, nothing else): https://buckykeys.app
>
> Disclosure: I'm the developer.

**Attach:** `site/public/hero-vim.png`

---

## After posting

- Reply to every comment within a day for the first week.
- Log every feature request with a count in `docs/launch/gate0-signal.md` (PLAN week 1 deliverable: top-10 list).
- Gate 0 check after two weeks (PLAN §10): at least 100 waitlist signups, or a clear qualitative signal from #875 and r/termux.
