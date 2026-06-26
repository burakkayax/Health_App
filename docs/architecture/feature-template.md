# Feature Implementation Template

Follow this step-by-step checklist when adding a new health metric (e.g., Water, Steps, Caffeine) to maintain architectural consistency.

## 1. Domain Layer (`:core:model`, `:domain`)
1. Create the Domain model in `:core:model`.
2. Define the Repository Interface in `:domain`.
3. Create Use Cases (e.g., `AddMetricUseCase`, `ObserveMetricSummaryUseCase`) in `:domain`.

## 2. Data Layer (`:core:database`, `:data:repository`)
4. Create the Room Entity in `:core:database`.
5. Create the DAO in `:core:database`.
6. Add the DAO to `AppDatabase` and create necessary migrations.
7. Create Mapper functions to map between Entity and Domain model.
8. Implement the Repository Interface in `:data:repository`.

## 3. Dependency Injection (`:app`)
9. Add the DAO binding to `DaoModule`.
10. Add the Repository binding to `RepositoryModule`.
11. Add the Use Case bindings to `UseCaseModule`.

## 4. UI Layer (`:feature:*`)
12. Create the feature module (e.g., `:feature:water`).
13. Define `UiState`, `UiEvent`, and Navigation Route.
14. Create the ViewModel interacting solely with Use Cases.
15. Handle Empty, Loading, and Error states.
16. Implement the UI Composables (Detail Route, Trend Card, Add Entry Form, History List).
17. Add UI Previews using safe local fake state.

## 5. Integration
18. Add the new feature route to `HealthNavHost` and `HealthRoutes`.
19. Update `SummaryViewModel` and `SummaryScreen` to display the new metric card.

## 6. Testing & Validation
20. Write Unit Tests for Use Cases and ViewModels.
21. Manually verify navigation, state persistence, and UI edge cases.
