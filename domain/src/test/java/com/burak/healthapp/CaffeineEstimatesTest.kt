package com.burak.healthapp

import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEstimates
import org.junit.Assert.assertEquals
import org.junit.Test

class CaffeineEstimatesTest {
    @Test
    fun estimateMg_returnsExpectedValuesPerDrinkAndSize() {
        assertEquals(50, CaffeineEstimates.estimateMg(CaffeineDrinkType.TURKISH_COFFEE, CaffeineDrinkSize.SMALL))
        assertEquals(65, CaffeineEstimates.estimateMg(CaffeineDrinkType.TURKISH_COFFEE, CaffeineDrinkSize.MEDIUM))
        assertEquals(90, CaffeineEstimates.estimateMg(CaffeineDrinkType.TURKISH_COFFEE, CaffeineDrinkSize.LARGE))

        assertEquals(140, CaffeineEstimates.estimateMg(CaffeineDrinkType.FILTER_COFFEE, CaffeineDrinkSize.MEDIUM))
        assertEquals(120, CaffeineEstimates.estimateMg(CaffeineDrinkType.ESPRESSO, CaffeineDrinkSize.MEDIUM))
        assertEquals(120, CaffeineEstimates.estimateMg(CaffeineDrinkType.AMERICANO, CaffeineDrinkSize.MEDIUM))
        assertEquals(100, CaffeineEstimates.estimateMg(CaffeineDrinkType.LATTE, CaffeineDrinkSize.MEDIUM))
        assertEquals(100, CaffeineEstimates.estimateMg(CaffeineDrinkType.CAPPUCCINO, CaffeineDrinkSize.MEDIUM))
        assertEquals(40, CaffeineEstimates.estimateMg(CaffeineDrinkType.BLACK_TEA, CaffeineDrinkSize.MEDIUM))
        assertEquals(30, CaffeineEstimates.estimateMg(CaffeineDrinkType.GREEN_TEA, CaffeineDrinkSize.MEDIUM))
        assertEquals(120, CaffeineEstimates.estimateMg(CaffeineDrinkType.ENERGY_DRINK, CaffeineDrinkSize.MEDIUM))
        assertEquals(35, CaffeineEstimates.estimateMg(CaffeineDrinkType.COLA, CaffeineDrinkSize.MEDIUM))
    }

    @Test
    fun estimateMg_otherUsesStableDefault() {
        assertEquals(50, CaffeineEstimates.estimateMg(CaffeineDrinkType.OTHER, CaffeineDrinkSize.SMALL))
        assertEquals(50, CaffeineEstimates.estimateMg(CaffeineDrinkType.OTHER, CaffeineDrinkSize.MEDIUM))
        assertEquals(50, CaffeineEstimates.estimateMg(CaffeineDrinkType.OTHER, CaffeineDrinkSize.LARGE))
    }
}
