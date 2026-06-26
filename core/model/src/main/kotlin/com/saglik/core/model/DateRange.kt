package com.saglik.core.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DateRange(
    val start: LocalDate,
    val end: LocalDate,
) {
    init {
        require(start <= end) { "DateRange start must be on or before end." }
    }
}
