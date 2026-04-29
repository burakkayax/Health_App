package com.burak.healthapp.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

internal fun MacrobenchmarkScope.navigateIfExists(label: String) {
    val node = device.wait(Until.findObject(By.text(label)), 1_500)
    node?.click()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.scrollTodayListIfExists() {
    val list = device.wait(Until.findObject(By.res("com.burak.healthapp:id/today_list")), 1_500)
    list?.setGestureMargin(device.displayWidth / 5)
    list?.scroll(Direction.DOWN, 0.8f)
    list?.scroll(Direction.UP, 0.8f)
}
