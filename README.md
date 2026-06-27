# Saglik App

An Android health tracking app built with Kotlin, Jetpack Compose, Hilt, Navigation Compose, Room, DataStore, and a multi-module architecture.

## V1 Demo Status

Current V1 features:

- Onboarding and profile setup
- Summary dashboard
- Weight tracking
- BMI calculation
- Sleep tracking
- Health Connect import for weight, sleep, steps, and exercise
- Read-only Steps and Exercise Summary cards with drill-down detail pages
- Persistent app chrome
- Floating bottom navigation
- Collapsing frosted header
- Manual entries
- Charts
- History lists

V1 polish focus:

- Premium pastel gradient and glass card interface
- Shared app chrome on main screens
- Real weight, BMI, sleep, steps, and exercise data on the Summary dashboard
- All-time Weight Trend with newest-first history
- Steps and Exercise detail pages show local synced data without charts, recommendations, route, calories, distance, or heart-rate metrics
- Manual sleep entry with overnight duration support
- Consistent empty, validation, and saving states

Intentionally missing from V1:

- Water tracking
- Caffeine tracking
- Mood or stress tracking
- Screen time
- Real insights
- Correlations
- AI coach
- Steps or exercise recommendations, trend analysis, route, calories, heart-rate, distance, speed, or training guidance
- Cloud sync
- Authentication

## Development

Build:

```bash
./gradlew assembleDebug
```

JVM tests:

```bash
./gradlew test
```

Android debug unit tests:

```bash
./gradlew testDebugUnitTest
```

Lint:

```bash
./gradlew lintDebug
```

Optional full local check:

```bash
./gradlew assembleDebug test testDebugUnitTest lintDebug
```

CI runs the debug build, JVM tests, debug unit tests, and debug lint through GitHub Actions on push and pull requests to `main`.
