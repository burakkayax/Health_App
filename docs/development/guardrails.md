# Development Guardrails

Use these rules to keep each PR small, reviewable, and safe for future health science, Health Connect, privacy, analytics, insights, forecasting, and AI work.

## PR Scope Rules

* Each PR should have one primary purpose.
* Do not mix new features with architecture rewrites.
* Do not change UI in data-layer PRs.
* Do not change database schema without migrations, exported schemas, and migration tests.
* Do not add Health Connect behavior before the Health Connect PR.
* Do not add settings, analytics, insights, forecasting, AI, or recommendation behavior as incidental work.

## Feature PR Rules

* Add or update the domain model first.
* Add a Room entity and DAO only when the data must be persisted.
* Add repository interfaces in `:domain`.
* Add repository implementations in `:data:repository`.
* Add use cases before ViewModel behavior.
* Add ViewModel state and events before UI wiring.
* Add UI after the data and domain paths are clear.
* Add focused tests and previews where behavior or UI changes.

## Refactor PR Rules

* Preserve behavior unless the PR explicitly says otherwise.
* Keep refactors close to the module or layer being improved.
* Do not move data access into feature modules.
* Do not introduce new runtime behavior as part of cleanup.
* Update architecture docs when dependency rules change.

## Data Migration Rules

* Increase the database version for schema changes.
* Add explicit Room migrations and avoid destructive migration.
* Commit exported Room schema JSON files.
* Preserve old records during migrations.
* Add migration tests for schema changes.
* Recheck manual add flows after database or repository changes.

## Design System Rules

* Prefer shared components from `:core:ui`.
* Avoid one-off card, input, and button implementations in feature modules.
* Do not return to default Material UI when a custom health component exists.
* Keep motion calm and restrained.
* Keep feature screens visually aligned with the existing health app chrome.

## Health Science And Safety Rules

* Do not make diagnosis claims.
* Do not provide medication dose or treatment advice.
* Use cautious language for labs, medications, and supplements.
* Explain uncertainty when health signals are incomplete.
* AI must not directly interpret raw health data without safety guardrails.

## Testing Expectations

* Run `./gradlew assembleDebug` before opening a PR.
* Run `./gradlew test` for JVM module tests.
* Run `./gradlew testDebugUnitTest` for Android unit tests.
* Run `./gradlew lintDebug` for lint.
* Add use case tests for domain logic.
* Add migration tests for database schema changes.
* Test ViewModel behavior when practical.
* Include previews for UI changes when practical.

## Manual Verification Checklist

* App launches.
* Summary works.
* Weight works.
* Sleep works.
* BMI still calculates from profile and weight data.
* Bottom navigation remains Summary, Trends, Insights, Search unless the PR explicitly changes navigation.
* Add, Profile, and floating search tab behavior do not return unintentionally.

## Codex And Antigravity Prompt Expectations

* State the PR goal and scope boundaries before asking for code changes.
* Name files or modules that may be changed.
* Call out files or layers that must not change.
* Ask for local build, unit test, and lint results when practical.
* Require a summary of behavior, UI, navigation, and schema impact.
* Keep generated changes aligned with `docs/architecture/module-boundaries.md`.
