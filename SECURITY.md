# Security policy

A keyboard sees everything its user types, so security reports get priority.

## Reporting a vulnerability

Please do not open a public issue. Instead, report it privately through GitHub's [private vulnerability reporting](https://github.com/DimiVlachos/bucky/security/advisories/new), or email hello@buckykeys.app.

Reports are answered as quickly as possible, usually within a few days. Once a fix is released, the advisory is published with credit to you unless you prefer otherwise.

## Scope

In scope: the Android app in this repository and the [buckykeys.app](https://buckykeys.app) waitlist.

Worth knowing: the app has no `INTERNET` permission, so anything that would let typed text leave the device is a critical finding.

## Supported versions

Only the latest alpha on [GitHub Releases](https://github.com/DimiVlachos/bucky/releases) (currently 0.1.0-alpha1) is supported. Fixes go to the `main` branch and ship in the next alpha.
