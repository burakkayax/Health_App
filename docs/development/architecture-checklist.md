# Architecture Checklist

Use this checklist during PR review and when preparing Codex or Antigravity prompts.

## Layering

* UI flows through ViewModel -> Use Case -> Repository Interface -> Repository Implementation -> DAO/DataStore/External Source.
* Feature modules must not access DAOs directly.
* Feature modules must not depend on `:core:database`.
* Feature modules must not depend on data implementation modules.
* Domain must not depend on Compose.
* Domain must not depend on Room.
* Domain must not depend on Android UI.
* Data modules must not depend on feature modules.
* DI bindings stay in focused `:app:di` modules unless the architecture docs are intentionally updated.

## Navigation

* Bottom navigation remains Summary, Trends, Insights, Search unless an explicit navigation PR changes it.
* Add and Profile tabs must not return unintentionally.
* Floating search button behavior must not return unintentionally.
* Splash and onboarding chrome rules must remain stable.
* Navigation routes should stay centralized in the app navigation package.

## Testing

* Use case logic should have unit tests.
* Migration changes should have migration tests.
* ViewModel behavior should be tested when practical.
* UI changes should include previews when practical.
* Build, unit tests, and lint should run before merge.

## Review Questions

* Does the PR keep module dependencies aligned with `docs/architecture/module-boundaries.md`?
* Did any feature module gain a direct data or database dependency?
* Did any domain code gain Android UI, Compose, Room, or persistence imports?
* Did any data module start referencing feature code?
* Are behavior changes intentional and covered by tests or manual verification?
