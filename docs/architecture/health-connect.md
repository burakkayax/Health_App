# Health Connect

PR-8 adds the foundation for Health Connect without importing, syncing, or storing any Health Connect records. Weight and sleep sync is intentionally left for PR-9.

PR-9 adds foreground, user-initiated Weight/Sleep sync. It reads Health Connect `WeightRecord` and `SleepSessionRecord` data from the last 30 days and imports those records into the existing local Room tables.

PR-10 adds foreground, user-initiated Steps sync. The same Settings/Profile action now reads Health Connect `StepsRecord` data from the last 30 days and imports those records into the local `steps_entries` Room table.

PR-11 adds foreground, user-initiated Exercise sync. The same Settings/Profile action now reads Health Connect `ExerciseSessionRecord` data from the last 30 days and imports basic session metadata into the local `exercise_sessions` Room table.

## PR-8 Foundation Scope

- Adds the Health Connect SDK to `:core:healthconnect`.
- Checks provider availability.
- Defines the minimum read permission set for future Weight/Sleep sync.
- Checks granted and missing permissions.
- Lets the user launch the Health Connect permission screen from Settings/Profile.
- Shows calm Settings/Profile states for ready, missing permission, update required, unsupported, loading, and error.

PR-8 does not read `WeightRecord`, read `SleepSessionRecord`, write records, start background sync, use WorkManager, write to Room, or change the database schema.

## PR-9/PR-11 Foreground Sync Scope

- Sync is started only after the user taps `Sync Health Connect data` in Settings/Profile.
- Sync checks availability and required read permissions before reading records.
- Sync reads only the last 30 days: from now minus 30 days to now.
- Sync imports only `WeightRecord`, `SleepSessionRecord`, `StepsRecord`, and `ExerciseSessionRecord`.
- Imported rows use `DataSource.HEALTH_CONNECT`.
- Duplicate prevention uses `source + sourcePackageName + sourceRecordId`.
- Re-running sync updates the existing Health Connect row instead of creating a duplicate.
- Invalid sleep sessions are skipped safely.
- Invalid steps records are skipped safely.
- Invalid exercise sessions are skipped safely.

PR-11 does not add Health Connect writes, write permissions, route/location import, background read permission, historical read permission, WorkManager, automatic sync, deletion sync, changes-token sync, conflict UI, exercise recommendations, workout plans, training programs, AI interpretation, or advanced workout metrics.

## Android Version Behavior

On Android 14 and newer, Health Connect is platform-backed. Permission usage rationale is exposed through the `android.intent.action.VIEW_PERMISSION_USAGE` alias with `android.intent.category.HEALTH_PERMISSIONS`, protected by `android.permission.START_VIEW_PERMISSION_USAGE`.

On Android 13 and below, the app supports the Health Connect rationale intent `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` and queries the provider package `com.google.android.apps.healthdata`.

## Availability Model

The app-level model lives in `:core:model`:

- `Available`: Health Connect APIs can be used.
- `ProviderUpdateRequired`: the provider is missing or needs an update.
- `Unsupported`: Health Connect is unavailable on this device or profile.

`:core:healthconnect` maps SDK status values to this model. UI code does not call `HealthConnectClient.getSdkStatus()` directly.

## Permission Model

Required permissions are centralized in `HealthConnectPermissions`:

- `HealthPermission.getReadPermission(WeightRecord::class)`
- `HealthPermission.getReadPermission(SleepSessionRecord::class)`
- `HealthPermission.getReadPermission(StepsRecord::class)`
- `HealthPermission.getReadPermission(ExerciseSessionRecord::class)`

`HealthConnectPermissionStatus` computes required, granted, missing, and `allRequiredGranted`. Permission status is refreshable because users can grant or revoke access outside the app.

Only Weight, Sleep, Steps, and Exercise read permissions are requested. No write, exercise route, hydration, nutrition, medical data, heart-rate, blood-pressure, historical read, or background read permissions are requested.

## Architecture Boundary

- `:core:healthconnect` owns Health Connect SDK calls, required permissions, availability mapping, permission status checks, record reads, permission request contract, and safe intent helpers.
- `:core:model` owns app-level Health Connect snapshots so SDK record classes do not leak into feature UI.
- `:domain` owns the `HealthConnectRepository`, `HealthConnectSyncRepository`, and foreground sync use case.
- `:data:repository` adapts the Health Connect data source and existing DAOs for idempotent import.
- `:feature:profile` observes domain use cases and launches the permission request only after a user taps the Settings/Profile button.

Feature UI does not import Health Connect SDK record classes and does not write records directly. It triggers the sync use case and renders typed outcomes.

## Settings/Profile Integration

The Health Connect section shows real state:

- Checking/loading
- Available with permissions granted
- Available with missing permissions
- Provider install/update required
- Unsupported
- Error
- Syncing
- Success with inserted/updated counts
- No data

The section may show actions to sync weight, sleep, steps, and exercise sessions, grant permissions, open Health Connect settings, install/update Health Connect, or refresh status. Permission launch and sync are both user initiated. Returning from the permission screen refreshes state.

## Privacy Copy Rules

Health Connect setup copy must stay narrow and factual:

- Say that sync imports only weight, sleep, steps, and exercise sessions from the last 30 days.
- Say users can grant or revoke permissions at any time.
- Say sync starts only after the user taps the sync action.
- Do not make medical, diagnostic, recommendation, insight, forecasting, or AI claims.

## Current Limitations

- No Health Connect writes.
- No background sync, automatic sync, notifications, or WorkManager.
- No historical read permission and no all-time import.
- No exercise route/location import.
- No heart-rate, distance, speed, power, calories, laps, segments, GPS route, or advanced workout metrics.
- No exercise recommendations, workout plans, training programs, or AI interpretation.
- No deletion sync.
- No changes token yet.
- No conflict-resolution or merge UI.
