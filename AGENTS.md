# Feeder — Agent & Contributor Reference

Feeder is an open-source RSS/Atom/JSONFeed reader for Android, written in Kotlin with Jetpack Compose.
It runs entirely on-device; no account or backend is required for core functionality.

Source: <https://github.com/spacecowboy/Feeder>

---

## Environment

- **JDK 17** or newer is required (dictated by the Android Gradle plugin).
- No API keys or secrets are needed to build. The OpenAI feature keys are entered by the user at runtime via the Settings UI.
- No `local.properties` setup is required beyond the standard Android SDK path.

---

## Architecture

Feeder follows **MVVM** with a unidirectional data flow:

```
UI (Jetpack Compose screens)
  └── ViewModels  (state holders, coroutine scope owners)
        └── Repository  (single source of truth for dynamic data)
              └── Stores  (domain-scoped data access)
                    ├── Room database  (local persistence)
                    └── Network / REST clients
```

### Layer responsibilities

| Layer | Location | Role |
|---|---|---|
| **UI** | `ui/compose/` | Composable screens and components. Reads `StateFlow` from ViewModels, sends user events back. |
| **ViewModels** | `ui/*.kt` (e.g. `MainActivityViewModel`) | Own coroutine scopes, transform Repository flows into UI state, handle one-off commands. |
| **Repository** | `archmodel/Repository.kt` | The single place where all dynamic data decisions are made. Coordinates between stores. |
| **Stores** | `archmodel/*Store.kt` | Each store owns one data domain (feeds, feed items, settings, sync remotes, etc.). |
| **Database** | `db/room/` | Room entities, DAOs, and the `AppDatabase`. Current schema version: **39**. |
| **Model / parsers** | `model/` | Feed parsing, full-text extraction, HTML handling, OPML, notifications. |
| **Background** | `background/` | WorkManager/JobScheduler jobs for sync, blocklist updates, cleanup. |
| **Sync** | `sync/` | Optional cross-device sync REST client (see below). |
| **OpenAI** | `openai/` | Optional AI summarisation/translation (see below). |
| **DI** | `di/` | Kodein DI modules. |

### Dependency injection

The project uses **Kodein DI** exclusively. It was chosen because it is explicit and non-magical — bindings are plain Kotlin code and easy to trace. Do **not** introduce Hilt, Dagger, or any other DI framework.

To add a new dependency:
1. Add the binding in the appropriate module under `di/`.
2. Retrieve it with `val foo: Foo by instance()` inside a `DIAware` class, or pass the `DI` container explicitly.

---

## Package overview

| Package | Purpose |
|---|---|
| `archmodel/` | Repository + all Store classes. Core of the app's data layer. |
| `background/` | Background jobs (RSS sync, blocklist, cleanup, sync-chain tasks). |
| `base/` | Base classes shared across layers. |
| `blob/` | Storage for large binary content (e.g. cached article blobs). |
| `contentprovider/` | Android `ContentProvider` for widget and shortcut support. |
| `crypto/` | Encryption utilities (used by the sync feature). |
| `data/` | Simple data-only classes / value types. |
| `db/room/` | Room database: entities, DAOs, migrations, `AppDatabase`. |
| `di/` | Kodein DI module definitions. |
| `model/` | Feed parsing (`FeedParser`), HTML handling, OPML, notifications logic, full-text parser. |
| `notifications/` | Android notification construction and management. |
| `openai/` | AI summarisation/translation integration (community feature). |
| `sync/` | Feeder-Sync REST client and related models (experimental). |
| `truetype/` | Custom font loading utilities. |
| `ui/` | Activities, ViewModels, and all Compose UI code (under `ui/compose/`). |
| `util/` | General-purpose extension functions and helpers. |
| `widget/` | Android home-screen widget. |

---

## Build flavors

| Flavor | App ID | Notes |
|---|---|---|
| `fdroid` | `com.nononsenseapps.feeder` | **Primary** — distributed via F-Droid. |
| `play` | `com.nononsenseapps.feeder.play` | Google Play. Identical code; only a donation-related string differs (Play Store policy). |

---

## Commands

| Task | Command |
|---|---|
| Build & install to device | `./gradlew installFdroidDebug` |
| Run all JVM tests | `./gradlew test` |
| **Full test suite** (JVM + instrumented) | `./gradlew check connectedCheck` |
| Run only database migration tests | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.nononsenseapps.feeder.db.room` |
| Lint Kotlin style | `./gradlew ktlintCheck` |
| Auto-format Kotlin style | `./gradlew ktlintFormat` |

> Always run `./gradlew ktlintFormat` before committing. The CI will reject unformatted code.

Instrumented tests (`connectedCheck`) use a Gradle-managed virtual device (`pixel2api30`) and will spin up an emulator automatically. No physical device or manually started emulator is required, but the Android SDK must be correctly configured.

---

## Hard rules

### Database migrations are mandatory

**Every change to the Room schema MUST include:**

1. A migration in `db/room/AppDatabase.kt` (add a `MIGRATION_N_N+1` object and register it).
2. A migration test in `app/src/androidTest/java/com/nononsenseapps/feeder/db/room/`.

There is no exception to this rule. Feeder has a long history of users upgrading across many versions, and data loss from a missing migration is a critical bug.

---

## Commit message style

Feeder uses **Conventional Commits** with **past-tense** descriptions. Past tense reads better in the auto-generated changelog.

> **Note:** This intentionally deviates from the standard Conventional Commits spec, which uses imperative mood. If you write Conventional Commits elsewhere, you will default to imperative out of muscle memory — please don't.

```
<type>: <past-tense description>

fix: adjusted sync frequency
feat: added article text size setting
chore: updated dependency versions
refactor: extracted feed parsing into separate class
```

Common types: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`.

---

## Navigation

The app uses the **Jetpack Compose Navigation** library (`androidx.navigation.compose`). A `NavHost` is set up in `MainActivity`, and each destination is defined in `ui/compose/navigation/NavigationDestinations.kt` using a `.register()` extension pattern that attaches the composable and its arguments to the nav graph.

---

## Notable features

### Feeder Sync (experimental)
An optional cross-device sync feature that synchronises subscriptions and read-status via a self-hostable REST backend ([feeder-sync](https://github.com/spacecowboy/feeder-sync)). It is considered experimental and not 100% reliable. The relevant code lives in `sync/`. Use caution when modifying it.

### AI summarisation & translation (`openai/`)
A community-contributed feature. Users supply their own API keys via the Settings UI. It is entirely opt-in and should remain so — do not make it a required dependency.

---

## Logging

Always use the `logDebug` utility from `util/Logging.kt` instead of calling `android.util.Log.d` directly.
`logDebug` is a no-op in production builds, so debug noise never reaches end users.

```kotlin
// Good
import com.nononsenseapps.feeder.util.logDebug
logDebug(LOG_TAG, "message")

// Bad — leaks debug output to production
android.util.Log.d("tag", "message")
```

For warnings and errors that should survive in production, `Log.w` / `Log.e` are fine to use directly.

### Log tag convention

- Declare the tag as a constant named `LOG_TAG` in the class's companion object (or at file scope for top-level code).
- The value must be `SCREAMING_SNAKE_CASE` and **prefixed with `FEEDER_`**.
- Keep it short enough to stay within Android's 23-character tag limit.

```kotlin
companion object {
    private const val LOG_TAG = "FEEDER_MY_COMPONENT"
}
```

---

## Things to avoid

- Do **not** introduce new DI frameworks (Hilt, Dagger, etc.). Use Kodein.
- Do **not** skip database migrations. See the hard rule above.
- Do **not** add network calls or remote dependencies to core functionality — Feeder is designed to work fully offline.
- Do **not** make the Play flavor meaningfully different from the free flavor beyond what already exists.
