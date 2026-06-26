package com.saglik.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SleepQuality {
    POOR,
    OKAY,
    GOOD,
    EXCELLENT,
}
