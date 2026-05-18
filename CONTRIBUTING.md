# Contributing to Feeder

Thanks for your interest in contributing! Feeder is an open-source RSS/Atom/JSONFeed reader for Android.
It is available on [F-Droid](https://f-droid.org/en/packages/com.nononsenseapps.feeder/) and
[Google Play](https://play.google.com/store/apps/details?id=com.nononsenseapps.feeder.play).

---

## Getting started

Clone the repository:

```
git clone https://github.com/spacecowboy/Feeder.git
cd Feeder
```

Build and install to a connected Android device:

```
./gradlew installDebug
```

No API keys or special local configuration are required to build and run the app.

---

## Architecture overview

Feeder uses **MVVM** with a unidirectional data flow. A more detailed breakdown is available in
[`AGENTS.md`](AGENTS.md), which also serves as the reference for AI coding agents.

The short version:

- **`ui/compose/`** — Jetpack Compose screens and components
- **`ui/*.kt`** — ViewModels that own coroutine scopes and expose `StateFlow` to the UI
- **`archmodel/Repository.kt`** — single source of truth for all dynamic data
- **`archmodel/*Store.kt`** — domain-scoped data access (feeds, items, settings, …)
- **`db/room/`** — Room database, entities, DAOs
- **`model/`** — feed parsing, HTML, OPML, notifications

Dependency injection is handled exclusively by **Kodein DI**. Please do not introduce Hilt, Dagger,
or any other DI framework.

---

## Running tests

| Task | Command |
|---|---|
| JVM unit tests only | `./gradlew test` |
| **Full test suite** (JVM + instrumented) | `./gradlew check connectedCheck` |

Instrumented tests (`connectedCheck`) use a Gradle-managed virtual device (`pixel2api30`) that is started automatically. No physical device or manually started emulator is required — just a correctly configured Android SDK.

---

## Code style

Feeder uses **ktlint**. Before opening a pull request, format your code:

```
./gradlew ktlintFormat
```

To check without modifying:

```
./gradlew ktlintCheck
```

CI will fail on unformatted code.

---

## Database schema changes

> **This is a hard rule — no exceptions.**

If your change modifies the Room database schema (adding/removing/altering tables or columns), you
**must** also provide:

1. A migration object `MIGRATION_N_N+1` in `app/src/main/java/com/nononsenseapps/feeder/db/room/AppDatabase.kt`,
   registered in the `Room.databaseBuilder` call.
2. A migration test in `app/src/androidTest/java/com/nononsenseapps/feeder/db/room/`.

Feeder has users upgrading across many versions. A missing migration causes data loss, which is a
critical bug.

---

## Commit messages

Feeder follows [Conventional Commits](https://www.conventionalcommits.org/) with **past-tense**
descriptions (they read better in the auto-generated changelog):

```
<type>: <past-tense description>
```

Examples:

```
fix: adjusted sync frequency
feat: added article text size setting
chore: updated dependency versions
refactor: extracted feed parsing into separate class
```

Common types: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`.

---

## Translations

Translations are very welcome! The easiest way to contribute is via
[Weblate](https://hosted.weblate.org/engage/feeder/). Pull requests with translation files are also
accepted.

---

## Pull request guidelines

- Keep PRs focused — one concern per PR is much easier to review.
- Include tests for new behaviour where practical.
- Run `./gradlew ktlintFormat` before pushing.
- Follow the commit message convention above.
- If your change touches the database schema, migrations are mandatory (see above).

---

## Questions?

Open an issue on GitHub or start a discussion. The project is maintained by one person, so please
be patient — all genuine contributions are appreciated.
