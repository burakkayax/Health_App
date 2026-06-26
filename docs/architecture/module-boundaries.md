# Module Boundaries

To ensure the architecture remains scalable, the following dependency rules must be strictly enforced.

## 🟢 Allowed Dependencies

### Feature Modules (`:feature:*`)
- May depend on `:domain`
- May depend on `:core:model`
- May depend on `:core:ui` and `:core:designsystem`
- May depend on Android/Jetpack libraries (Compose, Navigation, Lifecycle, Hilt)

### Domain Module (`:domain`)
- May depend on `:core:model`
- May depend on `:core:common`
- May depend on Kotlin Standard Library, Coroutines, and Kotlinx DateTime

### Data Repository Module (`:data:repository`)
- May depend on `:domain`
- May depend on `:core:model`
- May depend on `:core:database` and `:data:local`

### Core Database Module (`:core:database`)
- Self-contained (Entities, DAOs, Room setup)

## 🔴 Forbidden Dependencies

- **Feature → Data/Core Impl:** Features must NOT depend on `:data:repository`, `:core:database`, or `:core:datastore`. (All data access goes through `:domain` Use Cases).
- **Feature → Room:** UI should not access DAOs or Room Entities.
- **Domain → Android UI / Compose:** The domain layer must remain pure Kotlin (no `android.*` UI imports).
- **Domain → Persistence:** Domain must not depend on Room, DataStore, or other persistence mechanisms.
- **Data → Feature:** Data layers must remain entirely unaware of the UI layers.
- **Core Model → Feature:** Core models are foundational and must not depend on higher-level features.

## DI Module Organization
DI bindings are kept in `:app:di` to prevent module coupling, structured into focused modules:
- `DatabaseModule`
- `DaoModule`
- `DataStoreModule`
- `LocalDataSourceModule`
- `RepositoryModule`
- `UseCaseModule`
- `HealthConnectModule`
