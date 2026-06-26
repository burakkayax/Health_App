package com.saglik.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.component.GlassHealthCard

@Composable
fun HealthTabPlaceholderScreen(
    title: String,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item {
            GlassHealthCard {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthColors.Ink,
                )
                Text(
                    text = "Placeholder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HealthColors.SecondaryText,
                )
            }
        }
    }
}
