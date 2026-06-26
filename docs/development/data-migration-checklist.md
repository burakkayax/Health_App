# Data Migration Checklist

Use this checklist for any PR that changes Room entities, DAOs, migrations, repository persistence behavior, import behavior, or external identity handling.

## Database Changes

* Was the database version increased?
* Was a migration added?
* Was schema export updated?
* Were generated schema JSON files committed?
* Was destructive migration avoided?
* Were old records preserved?
* Were migration tests added?
* Did manual add flows still work?

## Sync And External Identity

* Do manual records with null external identity still work?
* Does external identity uniqueness avoid duplicates?
* Are Health Connect and import records source-aware?
* Are `createdAt` and `updatedAt` handled consistently?
* Are local manual records protected from import overwrite behavior?

## Ordering

* Are history lists newest-first?
* Are chart data points chronological?
* Did Weight all-time trend behavior remain unchanged?
* Did Sleep history and trend behavior remain unchanged?
* Did summary cards keep their existing ordering assumptions?

## Required Verification

* Run the relevant migration tests.
* Run `./gradlew test` when domain or JVM module tests are affected.
* Run `./gradlew testDebugUnitTest` when Android unit tests are affected.
* Run `./gradlew assembleDebug` after data-layer changes.
* Manually verify Weight and Sleep add flows when persistence behavior changes.
