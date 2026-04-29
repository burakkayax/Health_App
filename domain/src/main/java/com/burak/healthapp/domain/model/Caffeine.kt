package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CaffeineEntry(
    val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val drinkType: CaffeineDrinkType,
    val size: CaffeineDrinkSize,
    val estimatedMg: Int,
    val customName: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class CaffeineDrinkType {
    TURKISH_COFFEE,
    FILTER_COFFEE,
    ESPRESSO,
    AMERICANO,
    LATTE,
    CAPPUCCINO,
    BLACK_TEA,
    GREEN_TEA,
    ENERGY_DRINK,
    COLA,
    OTHER,
}

enum class CaffeineDrinkSize {
    SMALL,
    MEDIUM,
    LARGE,
}

object CaffeineEstimates {
    fun estimateMg(type: CaffeineDrinkType, size: CaffeineDrinkSize): Int = when (type) {
        CaffeineDrinkType.TURKISH_COFFEE -> estimateBySize(size, small = 50, medium = 65, large = 90)
        CaffeineDrinkType.FILTER_COFFEE -> estimateBySize(size, small = 90, medium = 140, large = 200)
        CaffeineDrinkType.ESPRESSO -> estimateBySize(size, small = 60, medium = 120, large = 180)
        CaffeineDrinkType.AMERICANO -> estimateBySize(size, small = 75, medium = 120, large = 180)
        CaffeineDrinkType.LATTE,
        CaffeineDrinkType.CAPPUCCINO,
        -> estimateBySize(size, small = 60, medium = 100, large = 150)
        CaffeineDrinkType.BLACK_TEA -> estimateBySize(size, small = 25, medium = 40, large = 60)
        CaffeineDrinkType.GREEN_TEA -> estimateBySize(size, small = 20, medium = 30, large = 45)
        CaffeineDrinkType.ENERGY_DRINK -> estimateBySize(size, small = 80, medium = 120, large = 160)
        CaffeineDrinkType.COLA -> estimateBySize(size, small = 25, medium = 35, large = 50)
        CaffeineDrinkType.OTHER -> 50
    }

    private fun estimateBySize(
        size: CaffeineDrinkSize,
        small: Int,
        medium: Int,
        large: Int,
    ): Int = when (size) {
        CaffeineDrinkSize.SMALL -> small
        CaffeineDrinkSize.MEDIUM -> medium
        CaffeineDrinkSize.LARGE -> large
    }
}
