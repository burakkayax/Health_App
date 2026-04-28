package com.burak.healthapp.domain.calculation

fun clampProgress(current: Float, target: Float): Float {
    if (target <= 0f) return 0f
    return (current / target).coerceIn(0f, 1f)
}

fun directionAwareProgress(baseline: Float, current: Float, target: Float): Float {
    if (baseline == target) return if (current == target) 1f else 0f
    return if (target > baseline) {
        ((current - baseline) / (target - baseline)).coerceIn(0f, 1f)
    } else {
        ((baseline - current) / (baseline - target)).coerceIn(0f, 1f)
    }
}

fun calculateBodyMassIndex(weightKg: Float?, heightCm: Float?): Float? {
    if (weightKg == null || heightCm == null || weightKg <= 0f || heightCm <= 0f) return null
    val heightMeters = heightCm / 100f
    return weightKg / (heightMeters * heightMeters)
}

fun classifyBodyMassIndex(bmi: Float): String = when {
    bmi < 18.5f -> "Zayıf"
    bmi < 25f -> "Normal"
    bmi < 30f -> "Kilolu"
    else -> "Yüksek Kilolu"
}

fun normalizeBodyMassIndexToGauge(
    bmi: Float,
    minValue: Float = 15f,
    maxValue: Float = 40f,
): Float {
    if (maxValue <= minValue) return 0f
    return ((bmi - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
}
