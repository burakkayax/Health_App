package com.burak.healthapp.feature.onboarding

enum class OnboardingStep {
    WELCOME,
    TRACKING_AREAS,
    BASIC_INFO,
    ACTIVITY_GOAL,
    SMART_GOALS,
    PREFERENCES,
    DONE,
}

enum class OnboardingActivityLevel {
    LOW,
    LIGHT,
    MODERATE,
    HIGH,
}

enum class OnboardingMainGoal {
    MAINTAIN,
    SLOW_GAIN,
    SLOW_LOSS,
}

enum class OnboardingSex {
    MALE,
    FEMALE,
    UNSPECIFIED,
}
