# Performance, Battery & Stability Baseline Audit

## Scope

- App startup
- Today dashboard
- Trends screen
- Detail screens
- Compose recomposition
- Flow / Room / DataStore
- Charts and monthly grids
- Background work
- Step sensor / foreground service
- Water reminder WorkManager
- Import/export
- Stability and crash risks

## Summary

This audit is a baseline for follow-up optimization PRs. It intentionally avoids large behavior changes and uses lightweight debug-only logging to make the current bottlenecks easier to observe in Logcat.

### Top 5 Risks

1. Today dashboard is fed by one large `TodaySnapshot` combine chain. Any metric update can rebuild the whole dashboard UI state.
2. Detail screens now build chart state in ViewModels, but several builders still perform grouping, date-window creation, formatting, sorting, and monthly grid construction every emission.
3. Step tracking uses a foreground service and sensor listener correctly guarded by user preference in the app shell, but the service itself does not re-check the persisted preference on service restart.
4. Water reminder worker can still perform settings and Today snapshot reads before discovering that notification permission prevents visible output.
5. Import/export paths read and parse whole JSON payloads and collapse many failures into generic UI messages, which limits diagnosis and can make large files feel unstable.

### Fast Wins

- Add `remember` / `derivedStateOf` around Today dashboard sort/filter work.
- Split Today card state collection or render boundaries so one small metric update does not force all cards through the same path.
- Add early notification-permission checks inside `WaterReminderWorker`.
- Cache date and number formatter instances used by chart label generation.
- Add field-level import validation before Room writes.

### Suggested Follow-Up PRs

- PR23.6 Performance & Recomposition Optimization
- PR23.7 Battery / Background Work / Sensor Optimization
- PR23.8 Stability, Import/Export & Error Handling Hardening

## Debug-Only Measurement Points

Added in this PR:

- `app/src/main/java/com/burak/healthapp/core/performance/PerformanceLogger.kt`
- `app/src/main/java/com/burak/healthapp/core/performance/PerformanceTrace.kt`

The logger writes only when `BuildConfig.DEBUG` is true.

Current markers:

- `MainActivity:onCreate:start`
- `MainActivity:setContent:start`
- `MainActivity:setContent:first_render`
- `TodayRoute:start` / `TodayRoute:first_render`
- `TrendsRoute:start` / `TrendsRoute:first_render`
- `WeightDetailRoute:start` / `WeightDetailRoute:first_render`
- `SleepDetailRoute:start` / `SleepDetailRoute:first_render`
- `StepDetailRoute:start` / `StepDetailRoute:first_render`
- `HydrationDetailRoute:start` / `HydrationDetailRoute:first_render`
- `CaffeineDetailRoute:start` / `CaffeineDetailRoute:first_render`
- `SmokingDetailRoute:start` / `SmokingDetailRoute:first_render`
- `ExerciseDetailRoute:start` / `ExerciseDetailRoute:first_render`

Current measured state-build blocks:

- `Today:state_build`
- `Trends:state_build`
- `WeightDetail:state_build`
- `SleepDetail:state_build`
- `StepDetail:state_build`
- `HydrationDetail:state_build`
- `CaffeineDetail:state_build`
- `SmokingDetail:state_build`
- `ExerciseDetail:state_build`

These are diagnostic only. They are not a replacement for macrobenchmark numbers.

## Findings

### Finding 1

- Area: Today dashboard / Flow fan-in
- Symptom: Small changes to one metric can rebuild the full `TodaySnapshot` and `TodayUiState`.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/repository/DashboardRepositoryImpl.kt`, `observeToday`; `app/src/main/java/com/burak/healthapp/feature/today/TodayViewModel.kt`.
- Risk level: High
- Suggested follow-up PR: PR23.6

The repository combines settings, meals, hydration, sleep, exercise, smoking, steps, caffeine, supplements, doses, and measurement flows into one snapshot. This is simple and correct, but the dashboard is now broad enough that card-local updates should be considered.

### Finding 2

- Area: Today dashboard / Compose recomposition
- Symptom: Card order and visibility were sorted and filtered directly inside `TodayContent`.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/feature/today/TodayScreen.kt`, `orderedCards`, `visibleCards`, `dashboardItems`.
- Risk level: Medium
- Suggested follow-up PR: PR23.6

`AdaptiveDashboardGrid` uses stable keys, which is good. PR23.6 moved dashboard item derivation into a helper and memoized it with `remember`.

### Finding 3

- Area: Detail screen state generation
- Symptom: Chart state builders still do repeated `groupBy`, `sumOf`, date-range generation, sorting, and month-grid construction per emission.
- Evidence / code location: `CaffeineDetailScreen.kt`, `HydrationDetailScreen.kt`, `SmokingDetailScreen.kt`, `ExerciseDetailScreen.kt`, `StepDetailScreen.kt`, `SleepDetailScreen.kt`.
- Risk level: Medium
- Suggested follow-up PR: PR23.6

Most of this work is already outside composition, which is the right direction. The remaining risk is repeated CPU work on each flow emission. Debug `PerformanceLogger.measure(...)` markers were added around these builders.

### Finding 4

- Area: Trends screen
- Symptom: Trends combines settings with a broad trends snapshot and builds multiple chart/insight models together.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/feature/trends/TrendsViewModel.kt`; `data/src/main/java/com/burak/healthapp/data/repository/TrendsRepositoryImpl.kt`.
- Risk level: Medium
- Suggested follow-up PR: PR23.6 or PR23.9

`flowOn(Dispatchers.Default)` is already used for UI mapping. Future work should look at per-chart state and avoiding full Trends recomputation when only one metric changes.

### Finding 5

- Area: Room date queries
- Symptom: Most date-range queries now use date indexes and deterministic order, but any future query shape changes need migration-backed index review.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/local/dao/*Dao.kt`; entity indexes in `data/src/main/java/com/burak/healthapp/data/local/entity/*Entity.kt`.
- Risk level: Low
- Suggested follow-up PR: PR25 if new indexes are needed

Hydration, meals, sleep, exercise, smoking, steps, body measurements, supplement doses, and caffeine have relevant date indexes. No migration is proposed in this audit.

### Finding 6

- Area: DataStore dashboard customization
- Symptom: Dashboard card config JSON is decoded every settings emission.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/repository/SettingsRepositoryImpl.kt`, `settings` flow and `decodeDashboardCardConfig`.
- Risk level: Low
- Suggested follow-up PR: PR23.6

The config is small, but this can become noisy because settings are collected by several screens. A simple distinct/cached decode strategy can be considered later.

### Finding 7

- Area: Step sensor / foreground service
- Symptom: The app shell starts/stops the service based on `stepTrackingEnabled`, sensor availability, and permission, but `StepCounterService.onStartCommand` only checks permission and sensor.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/feature/app/PermissionEffects.kt`; `app/src/main/java/com/burak/healthapp/core/step/StepCounterService.kt`.
- Risk level: Medium
- Suggested follow-up PR: PR23.7

If Android restarts the sticky service, the service should also check persisted `stepTrackingEnabled` before registering the listener.

### Finding 8

- Area: Step sensor shutdown
- Symptom: Pending sensor value flush uses `runBlocking` in `onDestroy`.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/core/step/StepCounterService.kt`, `flushPendingSensorValue`.
- Risk level: Medium
- Suggested follow-up PR: PR23.7

This preserves data, but it can block service teardown. A bounded async flush or explicit last-write path would be safer.

### Finding 9

- Area: Water reminder worker
- Symptom: Worker reads settings and Today snapshot before notification visibility is fully known.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/core/reminder/WaterReminderWorker.kt`; notification permission check in `HealthNotifications.showWaterReminder`.
- Risk level: Medium
- Suggested follow-up PR: PR23.7

The worker exits correctly for disabled reminders, outside time windows, snoozed days, and completed hydration target. It should also short-circuit before the Today snapshot read when notification permission is missing.

### Finding 10

- Area: Import/export IO
- Symptom: Import reads the entire selected file into memory, and export builds the full JSON string before writing.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/export/HealthDataImportFileReader.kt`; `HealthDataExportFileWriter.kt`; `app/src/main/java/com/burak/healthapp/feature/profile/ProfileViewModel.kt`.
- Risk level: Medium
- Suggested follow-up PR: PR23.8

The file operations run on `Dispatchers.IO`, which is good. Large files still need size limits, streaming, or clearer progress/error states.

### Finding 11

- Area: Import transaction boundaries
- Symptom: Room import is transactional, but settings import happens after the Room transaction.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/export/HealthDataManagementRepositoryImpl.kt`, `importHealthData`.
- Risk level: High
- Suggested follow-up PR: PR23.8

If DB import succeeds and a later DataStore write fails, imported records and imported settings can diverge. The follow-up should define a consistent import commit model.

### Finding 12

- Area: Import validation
- Symptom: JSON validation checks blank/invalid JSON and schema version, then relies on decode. Field-level semantic validation is limited.
- Evidence / code location: `data/src/main/java/com/burak/healthapp/data/export/JsonHealthDataImporter.kt`.
- Risk level: Medium
- Suggested follow-up PR: PR23.8

Invalid date/time formats are rejected as invalid JSON/decode failures, but users do not get field-specific feedback.

### Finding 13

- Area: Error handling UX
- Symptom: Import/export failures are collapsed to generic UI messages.
- Evidence / code location: `app/src/main/java/com/burak/healthapp/feature/profile/ProfileViewModel.kt`, `exportData`, `loadImportPreview`, `confirmImport`.
- Risk level: Medium
- Suggested follow-up PR: PR23.8

This is safe for privacy, but it makes support and debugging harder. A typed, localized error model can preserve privacy while making failures clearer.

### Finding 14

- Area: Benchmark coverage
- Symptom: Existing benchmark coverage focuses on startup, Today scroll, and navigation between primary tabs.
- Evidence / code location: `benchmark/src/main/java/com/burak/healthapp/benchmark/StartupMacrobenchmarkTest.kt`; `BaselineProfileGeneratorTest.kt`.
- Risk level: Low
- Suggested follow-up PR: PR23.6

After PR23.6, add scenarios for dashboard customization, caffeine detail, hydration detail, and import/export preview if feasible.

## Compose Recomposition Notes

- Today has stable keys through `AdaptiveDashboardGrid`, and compact/expanded paths both use keyed lazy containers.
- Today dashboard item derivation is memoized from `dashboardCards`; future work should target the broader repository fan-in instead.
- Detail screens generally receive already-built UI state, so charts are not doing the heaviest date aggregation in composition.
- Some detail history lists use stable IDs or dates; this should be kept when PR23.6 touches these screens.
- UI state classes are mostly immutable data classes, but many contain regular Kotlin lists. This is acceptable for now; a future pass can consider persistent immutable collections only if recomposition evidence justifies it.

## Flow / Room / DataStore Notes

- `DashboardRepositoryImpl.observeToday` is the largest flow fan-in and the highest priority performance target.
- Detail range queries use `observeBetween(startDate, endDate)` with `ORDER BY`, and current entity indexes mostly match date filters.
- `SettingsRepositoryImpl.settings` parses dashboard config from a JSON string on every DataStore emission.
- Dashboard reorder writes are final-action based, not live-drag writes, which is good for DataStore write pressure.
- No database migration is recommended from this audit. Any new index should be handled in a separate DB PR with migration tests.

## Chart and Monthly Grid Notes

- Weekly and monthly states for hydration, caffeine, smoking, exercise, step, and sleep are mostly ViewModel-built.
- `MetricMonthRingGrid` renders supplied state and now supports over-limit semantics; it should stay presentation-only.
- Formatter creation and label formatting are small but repeated. PR23.6 can cache formatter instances or preformat labels in UI state.
- Weight chart state is built in the ViewModel path and now has a debug timing marker.
- Trends chart state is still broad and should be measured before changing.

## Battery / Background Work Notes

- Step tracking is user controlled and app-shell gated by permission and sensor availability.
- `StepSensorWritePolicy` throttles writes, reducing Room write pressure.
- `StepCounterService` should re-check persisted enablement before sensor registration on sticky restarts.
- `flushPendingSensorValue` should avoid unbounded blocking during service teardown.
- Water reminder scheduling uses WorkManager and does not use exact alarms.
- Water reminder worker should short-circuit earlier when notification permission is denied.
- Water reminder action receiver uses `goAsync()` and IO coroutine work, which is appropriate; failure reporting is intentionally minimal.

## Stability and Crash Risk Notes

- Import/export file access is user-controlled through SAF and IO-backed, but large payloads are loaded as full strings.
- Import validation should become field-aware: date/time parsing, negative or extreme values, enum mismatches, duplicate edge cases, and schema migration details.
- Current generic error messages avoid exposing sensitive details, but they hide actionable import failure causes.
- Room import is transactional for database records. Settings import is not part of the same transaction boundary.
- Delete-all health data is transactional for Room health records and preserves profile/theme/onboarding/goals as designed.

## Existing Benchmark Status

- `:benchmark:assembleBenchmark` is expected to pass without an emulator.
- `:benchmark:connectedBenchmarkAndroidTest` requires an emulator or physical device and is not mandatory for this PR.
- Local emulator results are useful for regression smoke checks, but they are not representative of real battery, startup, or frame timing behavior.
- Physical device runs should be preferred for PR23.6 and later optimization validation.
- Benchmark labels currently target app navigation and Today scroll. After performance work, add detail-screen and dashboard-customization paths.

## PR23.6 Follow-up Notes

Addressed in PR23.6:

- Today dashboard card derivation was moved behind a small helper and `remember`, so card ordering, visibility filtering, empty state, and the customize item are no longer rebuilt on every unrelated recomposition.
- Today card render boundaries were narrowed: dashboard cards now receive their own `NutritionCardState`, `HydrationCardState`, `ExerciseCardState`, `SmokingCardState`, `StepCardState`, `SleepCardState`, or `WeightCardState` instead of the full `TodayUiState`.
- Detail state builders now share date-window helpers for trailing week, month-to-date, and month-grid generation.
- Hydration, caffeine, smoking, exercise, step, sleep, and weight detail builders now reuse formatter instances and avoid repeated `LocalDate.now()` calls inside month-grid loops.
- Step detail now uses the already-built date map for total/average state instead of repeatedly scanning with `date in days`.
- Locale-aware whole-number formatting now caches `NumberFormat` instances per locale/thread.
- Lazy key usage was reviewed; the exercise type picker received stable enum-name keys, and existing dashboard/history keys were retained.

Deferred to PR23.7:

- `StepCounterService` persisted `stepTrackingEnabled` re-check on sticky restarts.
- Non-blocking/bounded pending sensor flush during service teardown.
- Earlier `WaterReminderWorker` exit when notifications cannot be shown.
- Additional tests around service restart and notification-permission no-op paths.

Deferred to PR23.8:

- Typed import/export error model.
- Field-level import validation for date/time, enum, negative value, and schema mismatch failures.
- Large JSON size limits or streaming strategy.
- A clearer Room + DataStore import commit model.

Deferred to PR23.9:

- Trends state decomposition and per-chart rendering redesign.
- Broader trends chart benchmark coverage after the UI structure is redesigned.

Measurement markers retained:

- Route-level `DebugRoutePerformanceTrace` markers for Today, Trends, and detail routes.
- State-build `PerformanceLogger.measure(...)` markers for Today, Trends, Weight, Sleep, Step, Hydration, Caffeine, Smoking, and Exercise.

Benchmark notes:

- No connected macrobenchmark scenario was added in PR23.6 because current detail-screen navigation paths are still too UI-text dependent for a low-flake smoke path.
- `:benchmark:assembleBenchmark` remains the required non-emulator validation.
- Detail-screen and dashboard-customization benchmark paths should be added after selector/test tags are made more automation-friendly.

Remaining performance risks:

- `DashboardRepositoryImpl.observeToday` remains the largest fan-in and can still rebuild a broad snapshot for single-card data changes.
- Trends mapping is still intentionally broad.
- Some history/detail sections are column-based instead of lazy because their expected item count is small today; this should be revisited only with real data showing pressure.

## PR23.7 Follow-up Notes

Step tracking changes:

- `StepCounterService` now reads persisted `stepTrackingEnabled` before starting foreground tracking and before registering the step sensor listener.
- Sticky service restarts are guarded by the same start-decision path: disabled preference, missing permission, or missing sensor all stop the service without registering the listener.
- `ACTION_STOP` still persists `stepTrackingEnabled = false`, unregisters any active listener through the normal stop path, and returns `START_NOT_STICKY`.
- Sensor listener registration is centralized in `registerStepListener(...)`; duplicate registration is skipped when `listenerRegistered` is already true.
- Listener cleanup is centralized in `unregisterStepListener()`, clears `listenerRegistered`, clears `sensorManager`, and is safe to call repeatedly from stop and destroy paths.
- Sensor write failures are caught inside the service coroutine so repository errors do not crash the service.

Pending flush changes:

- Pending step sensor flush now uses a bounded 500 ms `withTimeoutOrNull` path instead of unbounded teardown blocking.
- `flushPendingStepSensorValue(...)` marks a sensor value as written only after the repository write succeeds.
- Failed or timed-out flushes are debug-logged only in debug builds and do not crash service teardown.

Water reminder changes:

- `WaterReminderWorker` now checks `shouldReadHydrationSnapshotForReminder(...)` before reading Today snapshot data.
- The worker exits before snapshot reads when the reminder is disabled, the current time is outside the reminder window, or notification permission is unavailable.
- Existing snooze and completed-target checks are preserved after snapshot read.
- Notification posting remains double-guarded: the worker checks before doing work, and `HealthNotifications.showWaterReminder(...)` still checks before notifying.
- Worker exceptions are caught and converted to `Result.success()` with debug-only logging to avoid background retry loops for user-invisible reminders.

Scheduler check:

- `WaterReminderScheduler.apply(...)` already cancels `ReminderConstants.WATER_REMINDER_WORK_NAME` when reminders are disabled.
- Enabled reminders still use `ExistingPeriodicWorkPolicy.UPDATE`, so settings changes update the same unique periodic work instead of creating duplicates.
- No scheduler redesign or exact-alarm behavior was introduced.

Deferred to PR23.8:

- Typed import/export error model.
- Field-level import validation.
- Large JSON import/export hardening.
- Room + DataStore import commit consistency.

Remaining battery/background risks:

- Physical-device verification is still needed for service restart and sensor lifecycle behavior.
- The bounded step flush can still drop the last pending value if the repository write exceeds the timeout.
- WorkManager may still wake within its normal periodic constraints even when notifications are denied; the worker now exits before expensive snapshot reads.

## PR23.8 Follow-up Notes

Import/export hardening added:

- Import validation now returns typed `ImportValidationError` values for empty files, invalid JSON, unsupported schema, missing required fields, invalid date/time/date-time, invalid enum, invalid number, negative values, oversized files, decode failure, database failure, settings failure, and unknown failures.
- `JsonHealthDataImporter` now performs a semantic validation pass before preview/confirm import. It checks ISO date, time, and date-time fields, domain enum strings, and negative numeric health values before any Room write.
- Sleep `startTime` / `endTime` values such as `22:30` now fail preview with an explicit date-time format error instead of falling through to a generic import failure.
- SAF import reads are capped at 5 MB. Known file sizes are rejected before reading; unknown sizes are read with a max-byte guard.
- `ProfileViewModel` sets import/export loading states before long work and maps typed import failures to localized user-safe messages.
- Room import remains transactional. Room and DataStore cannot be made atomic together, so settings write failures are reported as `SettingsFailure` after records are imported.

Remaining PR23.8 risks:

- Export still builds the full JSON in memory; streaming export is deferred until real file-size pressure appears.
- The import validator reports the first safe error, not a complete field-level error list.
- Room + DataStore consistency is documented but not fully atomic.

## PR23.9 Follow-up Notes

Trends redesign added:

- Trends now uses a weekly/monthly period selector where weekly is the last 7 days and monthly is the last 30 days.
- The tab was restructured around a period summary, highlights, goal adherence, metric trend cards, short data-based insights, and data-quality warnings.
- Trends snapshot data now includes current and previous period averages, caffeine, smoking, exercise, weight, adherence counts, and logged-day counts.
- Short insights are rule-based and cautious: water adherence, step movement, caffeine after cutoff, smoking limit, exercise activity, and data sufficiency are described without causal claims.
- Metric cards can navigate to existing detail destinations for water, steps, sleep, exercise, caffeine, smoking, weight, and nutrition.

Deferred beyond PR23.9:

- Full Insight Engine, Correlation Engine, experiments, and weekly reports remain out of scope.
- Sleep stability v2 is still deferred.
- More advanced Trends visualizations and benchmark scenarios should follow after this information architecture has settled.

## PR23.10 Follow-up Notes

Benchmark stabilization changes:

- Connected benchmark navigation now prefers stable Compose testTag resource ids instead of localized tab text.
- Main navigation exposes `nav_today`, `nav_trends`, and `nav_profile`; compact profile navigation can also use `profile_avatar_button`.
- `HealthApp` enables `testTagsAsResourceId`, so UiAutomator can find Compose test tags through `By.res(...)`.
- Startup and baseline profile paths now wait for the main shell before scrolling or tab navigation.
- `today_list`, `trends_screen`, and `profile_screen` are used as readiness checks where applicable.

Connected benchmark constraints:

- `:benchmark:assembleBenchmark` remains the non-device validation path.
- `:benchmark:connectedBenchmarkAndroidTest` still requires an unlocked emulator or physical device.
- Emulator results should be treated as smoke/regression signals; physical devices remain the preferred performance baseline.

## PR23.11 Follow-up Notes

Loading and empty-state changes:

- Root loading no longer renders the plain `Yükleniyor` text between Android splash and app content.
- Static, low-cost skeleton components were added for cards, metrics, charts, Today, Trends, and detail screens.
- Today and Trends use skeletons only for initial loading state; normal data updates should not reset the screen to skeleton.
- Detail screens can use the shared `DetailSkeletonContent`; water, caffeine, smoking, exercise, step, sleep, and weight initial states are now marked as loading.
- Trends and metric detail empty states use ghost placeholders to communicate where future data will appear.
- Import/export actions now use more specific loading labels for preparing, reading, importing, and deleting data.

Remaining UX follow-ups:

- Skeleton timing can be tuned later if real devices show visible flicker.
- Per-detail empty copy can be specialized further without changing the common skeleton system.
- Onboarding loading polish remains out of scope for this PR.

## Recommended PR Split

### PR23.6 Performance & Recomposition Optimization

- Split or memoize Today dashboard derived state.
- Investigate smaller state streams per dashboard card.
- Cache repeated formatter/date label work.
- Add `remember` / `derivedStateOf` only where measurements show churn.
- Expand macrobenchmark scenarios after changes.

### PR23.7 Battery / Background Work / Sensor Optimization

- Re-check `stepTrackingEnabled` inside `StepCounterService`.
- Replace blocking service shutdown flush with a bounded non-blocking strategy.
- Short-circuit `WaterReminderWorker` before Room reads when notifications cannot be shown.
- Add tests for sticky service restart and notification-permission no-op paths.
- Review WorkManager reschedule behavior after settings changes.

### PR23.8 Stability, Import/Export & Error Handling Hardening

- Add size limits or streaming strategy for JSON import/export.
- Add field-level import validation with typed localized errors.
- Define an import commit model for Room + DataStore consistency.
- Add more granular import/export failure telemetry in debug builds.
- Add tests for malformed date/time fields and oversized files.
