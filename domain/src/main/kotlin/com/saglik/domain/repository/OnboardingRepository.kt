package com.saglik.domain.repository

@Deprecated(
    message = "Use AppPreferencesRepository instead.",
    replaceWith = ReplaceWith("AppPreferencesRepository"),
)
typealias OnboardingRepository = AppPreferencesRepository
