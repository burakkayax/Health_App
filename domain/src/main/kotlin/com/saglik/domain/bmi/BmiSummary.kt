package com.saglik.domain.bmi

data class BmiSummary(
    val bmi: BmiResult?,
    val hasData: Boolean,
    val missingReason: BmiMissingReason?,
)
