package com.saglik.domain.bmi

class BmiCategoryMapper {
    fun map(value: Float): BmiCategory =
        when {
            !value.isFinite() -> BmiCategory.UNKNOWN
            value < 18.5f -> BmiCategory.LOW
            value < 25f -> BmiCategory.HEALTHY
            value < 30f -> BmiCategory.HIGH
            else -> BmiCategory.VERY_HIGH
        }
}
