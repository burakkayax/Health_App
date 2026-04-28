package com.burak.healthapp.feature.profile.goals

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings

data class ProfileGoalsUiState(
    val userName: String,
    val avatarInitials: String,
    val goalSettings: GoalSettings,
    val latestMeasurement: BodyMeasurementEntry?,
    val heightCm: Float? = null,
)
