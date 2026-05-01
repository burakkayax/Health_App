package com.burak.healthapp.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

private const val TARGET_PACKAGE = "com.burak.healthapp"
private const val READY_TIMEOUT_MS = 10_000L
private const val ACTION_TIMEOUT_MS = 5_000L

internal fun MacrobenchmarkScope.waitForAppReady() {
    val hasMainShell = device.wait(
        Until.hasObject(By.res(resourceId("nav_today"))),
        READY_TIMEOUT_MS,
    ) || device.wait(
        Until.hasObject(By.res(resourceId("today_list"))),
        ACTION_TIMEOUT_MS,
    )
    check(hasMainShell) {
        "Health app did not reach the main shell. Complete onboarding before running connected benchmarks."
    }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.navigateToToday() {
    if (device.hasObject(By.res(resourceId("profile_screen"))) && !device.hasObject(By.res(resourceId("nav_today")))) {
        device.pressBack()
        waitForAppReady()
    }
    navigateByStableSelector(
        testTag = "nav_today",
        fallbackDescriptions = listOf("Bugun", "Bugün", "Today"),
    )
}

internal fun MacrobenchmarkScope.navigateToTrends() {
    navigateByStableSelector(
        testTag = "nav_trends",
        fallbackDescriptions = listOf("Egilimler", "Eğilimler", "Trends"),
    )
    device.wait(Until.hasObject(By.res(resourceId("trends_screen"))), ACTION_TIMEOUT_MS)
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.navigateToProfile() {
    navigateByStableSelector(
        testTags = listOf("nav_profile", "profile_avatar_button"),
        fallbackDescriptions = listOf("Profil", "Profile"),
    )
    device.wait(Until.hasObject(By.res(resourceId("profile_screen"))), ACTION_TIMEOUT_MS)
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.scrollTodayList() {
    waitForAppReady()
    navigateToToday()
    val list = device.wait(Until.findObject(By.res(resourceId("today_list"))), ACTION_TIMEOUT_MS)
    checkNotNull(list) { "Today list with testTag today_list was not found." }
    list.setGestureMargin(device.displayWidth / 5)
    list.scroll(Direction.DOWN, 0.8f)
    list.scroll(Direction.UP, 0.8f)
    device.waitForIdle()
}

private fun MacrobenchmarkScope.navigateByStableSelector(
    testTag: String,
    fallbackDescriptions: List<String>,
) {
    navigateByStableSelector(
        testTags = listOf(testTag),
        fallbackDescriptions = fallbackDescriptions,
    )
}

private fun MacrobenchmarkScope.navigateByStableSelector(
    testTags: List<String>,
    fallbackDescriptions: List<String>,
) {
    waitForAppReady()
    val node = findByTagOrFallback(testTags, fallbackDescriptions)
    checkNotNull(node) { "Navigation item ${testTags.joinToString()} was not found." }
    node.click()
    device.waitForIdle()
}

private fun MacrobenchmarkScope.findByTagOrFallback(
    testTags: List<String>,
    fallbackDescriptions: List<String>,
): UiObject2? {
    testTags.forEach { testTag ->
        device.wait(Until.findObject(By.res(resourceId(testTag))), ACTION_TIMEOUT_MS)?.let { return it }
    }
    fallbackDescriptions.forEach { label ->
        device.wait(Until.findObject(By.desc(label)), 500)?.let { return it }
        device.wait(Until.findObject(By.text(label)), 500)?.let { return it }
    }
    return null
}

private fun resourceId(testTag: String): String = "$TARGET_PACKAGE:id/$testTag"
