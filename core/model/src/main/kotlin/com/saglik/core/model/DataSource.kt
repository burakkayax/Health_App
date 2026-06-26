package com.saglik.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DataSource {
    MANUAL,
    HEALTH_CONNECT,
    IMPORTED,
    ESTIMATED,
}
