package com.burak.healthapp.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupMacrobenchmarkTest {
    @get:Rule
    val macrobenchmarkRule = MacrobenchmarkRule()

    private val packageName = "com.burak.healthapp"

    @Test
    fun startup() {
        macrobenchmarkRule.measureRepeated(
            packageName = packageName,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Partial(),
        ) {
            pressHome()
            startActivityAndWait()
            waitForAppReady()
        }
    }

    @Test
    fun todayScrollAndNavigation() {
        macrobenchmarkRule.measureRepeated(
            packageName = packageName,
            metrics = listOf(FrameTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Partial(),
        ) {
            startActivityAndWait()
            waitForAppReady()
            scrollTodayList()
            navigateToTrends()
            navigateToProfile()
            navigateToToday()
        }
    }
}
