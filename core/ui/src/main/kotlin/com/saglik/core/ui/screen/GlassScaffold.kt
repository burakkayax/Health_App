package com.saglik.core.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable BoxScope.() -> Unit = {},
    floatingActionButton: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    HealthGradientBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            bottomBar()
            floatingActionButton()
        }
    }
}
