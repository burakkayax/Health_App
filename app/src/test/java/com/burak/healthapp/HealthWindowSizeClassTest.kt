package com.burak.healthapp

import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.dashboardColumnCount
import com.burak.healthapp.core.ui.adaptive.healthWindowSizeClassForWidth
import com.burak.healthapp.core.ui.adaptive.shouldUseNavigationRail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthWindowSizeClassTest {
    @Test
    fun widthMapping_usesCompactMediumExpandedThresholds() {
        assertEquals(HealthWindowSizeClass.COMPACT, healthWindowSizeClassForWidth(599))
        assertEquals(HealthWindowSizeClass.MEDIUM, healthWindowSizeClassForWidth(600))
        assertEquals(HealthWindowSizeClass.MEDIUM, healthWindowSizeClassForWidth(839))
        assertEquals(HealthWindowSizeClass.EXPANDED, healthWindowSizeClassForWidth(840))
    }

    @Test
    fun navigationRail_isUsedForMediumAndExpandedOnly() {
        assertFalse(HealthWindowSizeClass.COMPACT.shouldUseNavigationRail)
        assertTrue(HealthWindowSizeClass.MEDIUM.shouldUseNavigationRail)
        assertTrue(HealthWindowSizeClass.EXPANDED.shouldUseNavigationRail)
    }

    @Test
    fun dashboardColumnCount_isSingleColumnForCompactAndTwoColumnsOtherwise() {
        assertEquals(1, HealthWindowSizeClass.COMPACT.dashboardColumnCount)
        assertEquals(2, HealthWindowSizeClass.MEDIUM.dashboardColumnCount)
        assertEquals(2, HealthWindowSizeClass.EXPANDED.dashboardColumnCount)
    }
}
