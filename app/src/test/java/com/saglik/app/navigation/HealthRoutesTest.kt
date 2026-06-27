package com.saglik.app.navigation

import com.saglik.feature.summary.ExerciseRoute
import com.saglik.feature.summary.StepsRoute
import com.saglik.feature.summary.SummaryRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HealthRoutesTest {
    @Test
    fun stepsAndExerciseDetailsSelectSummaryTab() {
        assertEquals(SummaryRoute.route, HealthRoutes.selectedBottomRouteFor(StepsRoute.route))
        assertEquals(SummaryRoute.route, HealthRoutes.selectedBottomRouteFor(ExerciseRoute.route))
    }

    @Test
    fun stepsAndExerciseAreNotTopLevelTabs() {
        val topLevelRoutes = HealthRoutes.topLevelDestinations.map { it.route }

        assertFalse(topLevelRoutes.contains(StepsRoute.route))
        assertFalse(topLevelRoutes.contains(ExerciseRoute.route))
    }
}
