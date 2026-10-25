# Contributing to Bucky Keys

Thanks for helping. Bucky is early, so the most useful contributions right now are ideas, layout requests and bug reports. Open them from [the issue chooser](https://github.com/DimiVlachos/bucky/issues/new/choose).

## Before writing code

Open an issue first for anything bigger than a small fix, so we can agree on the approach. [CLAUDE.md](CLAUDE.md) describes the modules, their dependency rules and the code rules. [PLAN.md](PLAN.md) is the maintainer's working plan, in Greek; you don't need to read it to contribute.

## Ground rules

- The keyboard never gets the `INTERNET` or `QUERY_ALL_PACKAGES` permission.
- The `:ime` module stays free of Compose, billing, Play Services, settings, DataStore, DI, coroutines and lifecycle.
- No allocations in `KeyboardView.onDraw` or `onTouchEvent`.
- Logging is Timber only, in debug builds only.
- User-facing strings live in resources, in English (`values/`) and Greek (`values-el/`).

## Checks

Run these before opening a pull request. CI runs the same checks on every pull request, plus a DCO sign-off check.

```sh
./gradlew spotlessApply
./gradlew detekt :detekt-rules:test testFossDebugUnitTest testDebugUnitTest :app:architectureTest lintFossDebug assembleFossDebug assemblePlayDebug
```

Changes to `site/` run `cd site && npm test && npm run lint && npm run build`.

After building, `scripts/check-manifest-permissions.sh` confirms neither app flavour requests INTERNET or QUERY_ALL_PACKAGES.

## Commits and sign-off (DCO)

Use a conventional subject (`feat(ime): …`, `fix: …`, `docs: …`) and explain why in the body.

Every commit needs a `Signed-off-by` line, added with `git commit -s`. By signing off you certify the [Developer Certificate of Origin](https://developercertificate.org/): that you wrote the change, or otherwise have the right to submit it under the project's Apache-2.0 license.

## Code of conduct

By taking part you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).
