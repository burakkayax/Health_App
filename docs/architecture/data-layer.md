# Data Layer

This project treats Room as an implementation detail behind repositories. UI and
feature modules should depend on domain models and repository contracts, not on
Room entities, DAOs, or database modules.

## Room schema export

Room schema export is enabled for `AppDatabase`. Generated schema JSON files are
kept in `core/database/schemas/` and are committed with database changes. The
same folder is included as androidTest assets so migration tests can validate
real exported schemas.

## Migration policy

Database migrations must preserve user data and pass Room schema validation. Do
not use destructive migrations, `fallbackToDestructiveMigration`, or skipped
schema validation. The current migration chain is `1 -> 2 -> 3 -> 4`.

Version 4 adds sync identity metadata to Weight and Sleep records. Existing
weight rows backfill `createdAt` and `updatedAt` from `recordedAt`. Existing
sleep rows backfill `createdAt` and `updatedAt` from `endTime`.

## Sync identity fields

Weight and Sleep entities include these data-layer fields:

- `sourceRecordId`: external record id from Health Connect or imported data.
- `sourcePackageName`: package or source identifier when available.
- `sourceAppName`: user-friendly source name when available.
- `createdAt`: when this app first created or imported the local row.
- `updatedAt`: when this app last changed the local row.
- `lastSyncedAt`: when the row was last synced or imported.
- `deletedAt`: future tombstone field for delete/export/sync flows.

These fields are intentionally not exposed to UI yet.

## DataSource policy

Supported source values are `MANUAL`, `HEALTH_CONNECT`, `IMPORTED`, and
`ESTIMATED`. Manual records may have null external identity fields. External
records should provide `sourceRecordId` whenever the upstream source makes one
available.

## Duplicate prevention

Weight and Sleep use a Room-validated unique composite index on:

- `source`
- `sourcePackageName`
- `sourceRecordId`

Room 2.7 does not expose a partial-index `WHERE` clause in `@Index`, so the
implementation uses a normal unique composite index. SQLite allows multiple
`NULL` values in unique indexes, which means manual rows with null
`sourceRecordId` can coexist. External duplicate prevention is strongest when
external rows include both `sourcePackageName` and `sourceRecordId`.

DAOs expose external identity lookup methods for future import/upsert work, but
this PR does not implement Health Connect import.

## Delete and export preparation

`deletedAt` is passive metadata for future export/delete/privacy and sync flows.
Current queries do not filter it because soft-delete behavior is not implemented
yet and existing UI behavior must remain unchanged.

## Ordering rules

History lists are newest-first:

- Weight history orders by `recordedAt DESC`.
- Sleep history orders by `endTime DESC`.

Chart and trend consumers should keep chart data chronological. Do not move
chart ordering or aggregation policy into DAOs unless the domain contract changes.

## Module boundaries

Feature modules should not depend on database implementation modules. Repositories
map Room entities to domain models and keep data-layer-only metadata internal
until source display, Health Connect import, export/delete, or privacy tools need
it.

## Future expectations

Future Health Connect and import work should use `source`, `sourcePackageName`,
and `sourceRecordId` for idempotent upserts. Source display can use
`sourceAppName` later without changing existing history text now.

Lab, medication, supplement, and other health record families are intentionally
left for later PRs.

## Not implemented here

This data-layer hardening change does not add Health Connect integration,
permission flows, settings/profile screens, new tracking modules, trend engines,
insight engines, forecasting, AI, recommendations, notifications, or UI/navigation
changes.
