package com.saglik.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Sex {
    MALE,
    FEMALE,
    OTHER,
    UNSPECIFIED,
}
