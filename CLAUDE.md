# CLAUDE.md

PcKeys (codename, placeholder until a product name passes trademark checks) is a full PC-layout soft keyboard for Android: an `InputMethodService` with a Canvas keyboard, for Termux, SSH clients and coding.

`PLAN.md` is the source of truth for scope and architecture. Read it before any non-trivial change. Any change of scope or any new module updates `PLAN.md` first, in the same commit.

Conventions follow Dimitris's other Android apps. When a convention is unclear, ask; do not guess, especially on architecture.

## Modules

| Module | Holds | May depend on |
|---|---|---|
| `:app` | Manifest, `play`/`foss` flavors, Hilt `Application`, `PcKeysImeService`, flavor `BillingModule` | everything |
| `:ime` | Layout model, `KeyboardGeometry`, `KeyboardEngine`, `InputDispatcher`, `KeyboardView` | core-ktx, Timber |
| `:settings` | Compose settings and onboarding | `:billing` |
| `:billing` | `ProEntitlement`, `BillingRepository` | coroutines |
| `:billing-play` | Play implementation (`playImplementation` only) | `:billing` |
| `:billing-foss` | Always-unlocked implementation (`fossImplementation` only) | `:billing` |
| `:detekt-rules` | Custom detekt rules from Dimitris's other projects (build tooling, not shipped) | detekt-api |

`:ime` never imports Compose, billing or Play Services. `KeyboardEngine` imports nothing from Android except `KeyEvent` constants.

## Packages

- `:ime` uses component packages under `dev.pckeys.keyboard.ime`: `layout`, `geometry`, `engine`, `dispatch`, `view`.
- `:settings` and billing use feature-first `data/domain/presentation`, with `presentation/{entry,screen,state,view_model,components}`, `FooRepository` + `FooRepositoryImpl`, and an Entry that collects state with a stateless Screen.

## Code rules

- No `!!`, no `GlobalScope`, no `runBlocking` in production, no deprecated APIs, no wildcard imports.
- No comments or KDoc unless a one-line non-obvious "why" needs it. Documentation lives in `PLAN.md`.
- Explicit API mode in every library module.
- No allocations in `KeyboardView.onDraw` or `onTouchEvent`. The `KeyEvent`s the framework requires in `InputDispatcher` are the only exception.
- The IME never crashes. Engine calls from the view go through a boundary that catches and resets. `currentInputConnection` is nullable, so the dispatcher no-ops on null.
- No user-facing hardcoded strings. EN in `values/`, EL in `values-el/`. Key labels are layout data, not UI strings.
- Logging is Timber only, planted in debug builds only. Never `android.util.Log`.
- No `INTERNET` and no `QUERY_ALL_PACKAGES` permission, ever.

## Tests

JUnit4 with `org.junit.Assert`. Test names are backtick plain-English sentences. Hand-written `FakeX` classes go in `fake/` packages; no mocking library. Engine and geometry are pure JVM tests in `ime/src/test`.

## Commands

Gate before every commit (`spotlessApply` first after any Kotlin edit):

```
./gradlew spotlessApply
./gradlew detekt :detekt-rules:test testFossDebugUnitTest testDebugUnitTest :app:architectureTest lintFossDebug assembleFossDebug assemblePlayDebug
./gradlew installFossDebug
```

The Gradle daemon is pinned to JDK 21 (`gradle/gradle-daemon-jvm.properties`) because detekt 1.23 cannot run on JDK 25. Konsist gates live in `app/src/test/java/dev/pckeys/keyboard/architecture/` and run with `:app:architectureTest`. `config/detekt/baseline.xml` only shrinks (`BaselineBudgetTest`).

## Git

Branches are `feat/…`, `fix/…`, `chore/…`. Commits use a conventional subject, a body that explains why and lists the checks run, `git commit -s` (DCO, PLAN §8.2), and the session attribution lines.
