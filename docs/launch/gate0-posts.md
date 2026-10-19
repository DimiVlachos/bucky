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
> This thread has been asking since 2021 for something that keeps Hacker's Keyboard's layout but runs on current Android, and nothing maintained has filled that gap. I'm a senior Android developer (https://dimvlachos.dev), and to be upfront, I'm not a long-time HK user. I'm building this because the demand is clearly here and nobody has answered it. That's also why I'm asking rather than assuming.
>
> It's written from scratch rather than forked: new Kotlin code on today's APIs (target SDK 37), not a patched 2011 AOSP keyboard.
>
> Where it stands, honestly: an early build that already types in Termux on my S23 Ultra. Ctrl, Alt and Shift are one-shot modifiers that send real key events, so Ctrl+C interrupts, Esc leaves vim's insert mode and the arrows walk shell history. The full 5-row layout has `~`, `|` and `\` where you expect them, and it takes about 13 ms from touch to redraw at the median. Not done yet: long-press, key repeat, F1–F12 and other layouts.
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

> Hacker's Keyboard is still the keyboard people recommend for Termux because it sends real Ctrl/Esc/Tab/arrow keys the way a terminal expects. It's also a 2011 codebase that its author no longer maintains and that is falling behind current Android. Several forks keep the old code alive (AlainKnaff's had releases this year), but none rewrites it for current Android, so I'm writing a successor from scratch, in Kotlin on current APIs. I'm an Android developer, not a long-time HK user, which is exactly why I'm asking this sub what it needs instead of guessing.
>
> **What works today (early build):**
> - Full 5-row PC layout, with `~ | \` in the usual places
> - Ctrl/Alt/Shift as one-shot modifiers that send real KeyEvents: Ctrl+C interrupts `sleep`, Esc leaves vim's insert mode, arrows recall history
> - About 13 ms from touch to drawing the key (median, debug build on an S23 Ultra, before the screen's own frame)
>
> **Not yet:** long-press alternates, key repeat, F1–F12, Dvorak/Colemak/other layouts, per-app profiles
>
> **Ground rules:** Apache-2.0, no INTERNET permission, and every key, layout and modifier Hacker's Keyboard had stays free. A paid "Pro" on Google Play later covers extras like a layout editor and themes, but all of it is in the same open-source repo, and the `foss` build (from source now, F-Droid planned) unlocks everything for free.
>
> I'd rather build what this sub needs than guess, so (comments here or issues on GitHub both work):
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

## 3. #875 correction (not posted as a comment)

Decided instead to edit the original #875 comment in place on 2026-10-19 (credits the forks, quotes Klaus's #939 suggestion, scopes the free promise and the latency figure, adds an "Edit:" note), so the thread's subscribers were not notified twice. The draft below is kept for reference.

> A correction to my comment above. I wrote that nothing maintained has filled the gap, and that wasn't fair. @AlainKnaff's fork has had regular releases this year (1.42.9ak in September), and @codenameberyl and @jcb1ee have shipped builds too. If you need Hacker's Keyboard on a current phone today, those are the places to look.
>
> What I'm building is a different kind of thing: a new keyboard written from scratch on current Android APIs, not a patched version of the 2011 code. It's the direction Klaus suggested in #939: "putting the efforts into a more modern keyboard implementation".
>
> Two more corrections while I'm at it:
> - "Everything HK did stays free" was too broad. I mean every key, layout and modifier. Word prediction may end up as a paid extra later.
> - The 13 ms figure is touch-to-draw measured in a debug build. Add roughly one frame for the screen itself.
>
> The questions above still stand, and I'd especially like to hear from people who've tried the forks: what's still missing?

---

## After posting

- Reply to every comment within a day for the first week.
- Log every feature request with a count in `docs/launch/gate0-signal.md` (PLAN week 1 deliverable: top-10 list).
- Gate 0 check after two weeks (PLAN §10): at least 100 waitlist signups, or a clear qualitative signal from #875 and r/termux.
