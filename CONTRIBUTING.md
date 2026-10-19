# Contributing to Bucky Keys

Thanks for helping. Bucky is early, so the most useful contributions right now are ideas, layout requests and bug reports. Open them from [the issue chooser](https://github.com/DimiVlachos/bucky/issues/new/choose).

## Before writing code

Open an issue first for anything bigger than a small fix, so we can agree on the approach. [PLAN.md](PLAN.md) holds the scope and architecture (in Greek); [CLAUDE.md](CLAUDE.md) lists the code rules in English.

## Ground rules

- The keyboard never gets the `INTERNET` or `QUERY_ALL_PACKAGES` permission.
- The `:ime` module stays free of Compose, billing and Play Services.
- No allocations in `KeyboardView.onDraw` or `onTouchEvent`.
- Logging is Timber only, in debug builds only.
- User-facing strings live in resources, in English (`values/`) and Greek (`values-el/`).

## Checks

Run these before opening a pull request.

```sh
./gradlew spotlessApply
./gradlew detekt :detekt-rules:test testFossDebugUnitTest testDebugUnitTest :app:architectureTest lintFossDebug assembleFossDebug assemblePlayDebug
```

Changes to `site/` run `cd site && npm test && npm run lint && npm run build`.

## Commits and sign-off (DCO)

Use a conventional subject (`feat(ime): …`, `fix: …`, `docs: …`) and explain why in the body.

Every commit needs a `Signed-off-by` line, added with `git commit -s`. By signing off you certify the [Developer Certificate of Origin](https://developercertificate.org/): that you wrote the change, or otherwise have the right to submit it under the project's Apache-2.0 license.

## Code of conduct

By taking part you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).
