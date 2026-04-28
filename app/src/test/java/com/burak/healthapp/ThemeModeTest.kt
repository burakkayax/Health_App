package com.burak.healthapp

import com.burak.healthapp.core.ui.theme.resolveDarkTheme
import com.burak.healthapp.domain.model.ThemeMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun resolveDarkTheme_respectsExplicitModes() {
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, isSystemDark = true))
        assertTrue(resolveDarkTheme(ThemeMode.DARK, isSystemDark = false))
    }

    @Test
    fun resolveDarkTheme_followsSystemMode() {
        assertTrue(resolveDarkTheme(ThemeMode.SYSTEM, isSystemDark = true))
        assertFalse(resolveDarkTheme(ThemeMode.SYSTEM, isSystemDark = false))
    }
}
