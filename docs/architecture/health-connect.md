# Health Connect Foundation

PR-8 adds the foundation for Health Connect without importing, syncing, or storing any Health Connect records. Weight and sleep sync is intentionally left for PR-9.

## Scope

- Adds the Health Connect SDK to `:core:healthconnect`.
- Checks provider availability.
- Defines the minimum read permission set for future Weight/Sleep sync.
- Checks granted and missing permissions.
- Lets the user launch the Health Connect permission screen from Settings/Profile.
- Shows calm Settings/Profile states for ready, missing permission, update required, unsupported, loading, and error.

PR-8 does not read `WeightRecord`, read `SleepSessionRecord`, write records, start background sync, use WorkManager, write to Room, or change the database schema.

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

`HealthConnectPermissionStatus` computes required, granted, missing, and `allRequiredGranted`. Permission status is refreshable because users can grant or revoke access outside the app.

Only Weight and Sleep read permissions are requested because PR-9 will handle Weight/Sleep import. No write, steps, exercise, hydration, nutrition, medical data, heart-rate, blood-pressure, or background read permissions are requested.

## Architecture Boundary

- `:core:healthconnect` owns Health Connect SDK calls, required permissions, availability mapping, permission status checks, permission request contract, and safe intent helpers.
- `:domain` owns the `HealthConnectRepository` interface and small use cases.
- `:data:repository` adapts the Health Connect data source to the domain repository.
- `:feature:profile` observes domain use cases and launches the permission request only after a user taps the Settings/Profile button.

Feature UI does not read Health Connect records and does not insert Health Connect data into Room.

## Settings/Profile Integration

The Health Connect section shows real foundation state:

- Checking/loading
- Available with permissions granted
- Available with missing permissions
- Provider install/update required
- Unsupported
- Error

The section may show actions to grant permissions, open Health Connect settings, install/update Health Connect, or refresh status. Permission launch is user initiated and returning from the permission screen refreshes state.

## Privacy Copy Rules

Health Connect setup copy must stay narrow and factual:

- Say that no data is being imported yet.
- Say users can grant or revoke permissions at any time.
- Say Weight and Sleep sync will be added later.
- Do not make medical, diagnostic, recommendation, insight, forecasting, or AI claims.
