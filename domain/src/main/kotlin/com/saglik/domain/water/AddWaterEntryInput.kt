package com.saglik.domain.water

data class AddWaterEntryInput(
    val amountMl: Int,
    val recordedAtMillis: Long,
    val note: String?,
)
