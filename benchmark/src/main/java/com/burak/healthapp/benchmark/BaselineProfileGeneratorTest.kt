package com.burak.healthapp.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGeneratorTest {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        val packageName = "com.burak.healthapp"

        baselineProfileRule.collect(
            packageName = packageName,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            waitForAppReady()
            navigateToTrends()
            navigateToProfile()
            navigateToToday()
        }
    }
}
