# Gate 0 signal log

## Gate 0 rule (PLAN §10, fixed 2026-10-19 before any results)

Decision date: 2026-11-02. Count only people other than Dimitris.

| Outcome | Condition |
|---|---|
| **Continue** | ≥ 100 waitlist signups, **or** all of: ≥ 40 signups, ≥ 8 distinct people with a concrete feature request or a beta-test offer (#875, r/termux, GitHub issues), ≥ 25 repo stars |
| **Extend one week** (once only) | 25–39 signups, **or** two of the three "all of" conditions above met |
| **Stop** | < 25 signups **and** fewer than 3 substantive replies |

Anything else (for example 40+ signups but few replies and stars) is a judgement call, written down with reasons here before deciding.

Gate 0 does not test willingness to pay; Gate 2 does.

Sources for the numbers: signups from `ZCARD waitlist` (Vercel → Storage → bucky-waitlist → CLI), stars and watchers from the repo page, replies from the two threads and the repo's issues.

## Posts

| Where | Posted | Link |
|---|---|---|
| klausw/hackerskeyboard#875 | 2026-10-19 | https://github.com/klausw/hackerskeyboard/issues/875#issuecomment-6257364796 |
| r/termux (Showcase) | 2026-10-19 | https://www.reddit.com/r/termux/comments/1x9jwut/building_a_fromscratch_successor_to_hackers/ |

Gate 0 check due: 2026-11-02 (a cloud routine posts a summary at 17:07 Athens time).

## Running numbers

| Date | r/termux | #875 | Repo | Waitlist |
|---|---|---|---|---|
| 2026-10-19 (≈ half a day in) | 23 upvotes, 9 comments, 3.9K views | 0 replies, 1 🚀 | 5 stars (KaiRo-at, ShevT, Holton181, pfmiller0, litao91), 0 issues | not read yet (`ZCARD waitlist`, needs 2FA) |
| 2026-10-20 | 25 comments (16 not by Dimitris); upvotes and views not re-read | 1 reply (netllama), 1 🚀 | 6 stars (+gsmitheidw), 0 issues | 6 signups, all on 2026-10-19 (07:04–21:19 Athens), none since |

Engaged people so far (concrete request, answer or interest, excluding Dimitris): EDGE223x, ExplanationNo5955, pfmiller0, AdImmediate2565, d333d, BillGossAU, netllama (#875), gsmitheidw1, Easy-Nothing-6735 (9). Under the strict Gate 0 wording (a concrete feature request or a beta-test offer) pfmiller0 and Easy-Nothing-6735 only show interest, which leaves 7 of the 8 needed. Objections or competitor use: vanshisnotgay, Different-Injury-884, rpheuts (built keyfold for the Samsung Fold). Counterpoint to the objection: Optimal-Savings-4505.

Where Gate 0 stands on 2026-10-20: Stop needs fewer than 25 signups **and** fewer than 3 substantive replies, and replies are already past 3, so Stop cannot trigger. Continue (a) needs 100 signups; Continue (b) needs 40 signups, 8 people and 25 stars; Extend needs 25–39 signups or two of those three. Only the people condition is close. If signups stay under 25, the outcome falls into the written judgement-call case.

## Feature requests (tally)

| Request | Count | Sources | Plan status |
|---|---|---|---|
| Custom / own layouts (more keys by default) | 1 (+1 pointing to HeliBoard, which has it) | r/termux EDGE223x, Proteu__ | Planned (JSON layouts, week 3). PLAN makes the editor Pro; reconsider, since HeliBoard offers it free |
| Configurable extra row inside the keyboard, any key or symbol | 1 (+1 who keeps Termux's own row) | r/termux d333d, BillGossAU | Not in PLAN; candidate for week 4–6 |
| Clipboard manager, pasting into password fields | 1 | r/termux ExplanationNo5955 | Not in PLAN v1; keep password-field clips out of history if built |
| Floating keyboard on tablets (movable and smaller; split is not enough) | 1 | r/termux AdImmediate2565 | PLAN has split mode for tablets, not floating |
| Easy access to F1–F12 (one asks for a dedicated row) | 2 | #875 netllama, r/termux gsmitheidw1 (F10 in Midnight Commander) | PLAN has an Fn layer, not built yet; reconsider a row |
| Home, End and Esc on the main layer, not behind Fn | 1 (+1 whose Termux extra-keys row has them) | r/termux EDGE223x (vim, helix), BillGossAU | PLAN's PC Full layout has them (v1) |

## Notable replies

- **Cramped layout is the main objection.** vanshisnotgay left Hacker's Keyboard because "everything is so cramped" and uses Unexpected Keyboard. Different-Injury-884 is happy with Unexpected Keyboard plus Termux's extra-keys row. Implication: per-app profiles (full layout only in terminal apps), key height and landscape matter more than F-keys.
- **Interest:** pfmiller0, "This is great, I can't wait to try it!", and starred the repo.
- **Termux's extra-keys row coexists with Bucky:** both people who answered question 2 want an extra row (Termux's or the keyboard's own).
- **Question 1 was too open on #875.** netllama (US English, Termux, wants a dedicated F-key row) answered it with "Unsure what the choices are". A follow-up should list concrete Hacker's Keyboard features (5-row layout, F-keys, long-press symbols, key repeat, per-app layouts) and ask which one they could not do without.
- **Full layout only where it is needed.** BillGossAU swipe-types emails and messages and uses extra keys only in Termux (`TAB CTRL HOME END ← ↑ ↓ →` / `ESC \ ~ ^ ' - /`). This supports the built-in per-app profiles: PC Full in terminal apps, the user's usual keyboard or a compact layout elsewhere.
- **Cramped is a matter of degree.** Optimal-Savings-4505 has used Hacker's Keyboard for years and got used to it. Easy-Nothing-6735 calls a familiar PC-like keyboard essential and configurability a great addition.
- **Peer project:** rpheuts built keyfold (https://github.com/rpheuts/keyfold) for the Samsung Fold. Worth a look for foldable layouts.
