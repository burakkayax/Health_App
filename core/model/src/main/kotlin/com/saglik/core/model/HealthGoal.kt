package com.saglik.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class HealthGoal {
    LOSE_WEIGHT,
    GAIN_WEIGHT,
    MAINTAIN_WEIGHT,
    BUILD_MUSCLE,
    GENERAL_HEALTH,
}
