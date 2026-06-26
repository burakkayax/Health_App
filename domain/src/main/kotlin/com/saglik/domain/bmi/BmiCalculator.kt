package com.saglik.domain.bmi

class BmiCalculator {
    fun calculate(weightKg: Float, heightCm: Float): Float? {
        if (!weightKg.isFinite() || !heightCm.isFinite()) return null
        if (weightKg <= 0f || heightCm <= 0f) return null

        val heightMeters = heightCm / 100f
        val bmi = weightKg / (heightMeters * heightMeters)

        return bmi.takeIf { it.isFinite() }
    }
}
