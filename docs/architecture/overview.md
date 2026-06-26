# Architecture Overview

## Purpose
This project is an Android health tracking app utilizing Kotlin, Jetpack Compose, Room, DataStore, and Hilt. It focuses on maintaining a robust, modular, and scalable architecture to easily accommodate new health tracking features (e.g., Activity, Water, Caffeine, Mood).

## Current Module Structure
The app is organized into the following Gradle modules:
- `:app`: Application entry point, main navigation, and DI wiring.
- `:core:*`: Foundational shared layers.
  - `:core:common`: Utility classes and formatters.
  - `:core:model`: Pure domain model definitions.
  - `:core:designsystem`: Design tokens, theming, and UI components.
  - `:core:ui`: Shared Composables used across multiple features.
  - `:core:database`: Room database, DAOs, entities, and converters.
  - `:core:datastore`: DataStore definitions for preferences.
  - `:core:healthconnect`: Wrapper/placeholder for Health Connect.
- `:domain`: Business logic, use cases, and repository interfaces.
- `:data:*`: Data access layers.
  - `:data:local`: Local data sources (e.g., DataStore abstractions).
  - `:data:repository`: Repository implementations connecting domain interfaces to DAOs/local sources.
- `:feature:*`: Independent UI features.
  - `:feature:onboarding`: Initial app setup.
  - `:feature:summary`: Main dashboard.
  - `:feature:weight`, `:feature:bmi`, `:feature:sleep`, `:feature:profile`: Specific tracking modules.

## Architecture Flow
Data strictly flows downwards, while dependencies point inwards towards the Domain layer.

UI (Compose) ↔ ViewModel ↔ Use Case ↔ Repository Interface (`:domain`)
                                    ↳ Repository Implementation (`:data:repository`)
                                        ↳ DAO / DataStore / External API (`:core:database` / `:data:local`)

## Current Features (V1)
- Onboarding
- Summary dashboard
- Weight tracking (with trend charts)
- BMI calculation
- Sleep tracking (duration, overnight ranges)

## Future Features
Intentionally absent for now, planned for subsequent PRs:
- Health Connect real integration
- Settings / Privacy tools
- Water / Caffeine / Mood / Symptoms / Activity tracking
- Cloud sync / Authentication
