package com.saglik.feature.summary

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object SummaryRoute {
    const val route = "summary"
}

object StepsRoute {
    const val route = "steps"
}

object ExerciseRoute {
    const val route = "exercise"
}

fun NavGraphBuilder.summaryScreen(
    onBottomTabSelected: (String) -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToSteps: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    composable(SummaryRoute.route) {
        val viewModel: SummaryViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        SummaryScreen(
            state = state,
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(),
            onWeightClick = onNavigateToWeight,
            onBmiClick = onNavigateToWeight,
            onStepsClick = onNavigateToSteps,
            onExerciseClick = onNavigateToExercise,
        )
    }
}
