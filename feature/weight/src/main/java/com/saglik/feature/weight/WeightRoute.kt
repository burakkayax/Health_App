package com.saglik.feature.weight

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object WeightRoute {
    const val route = "weight"
}

fun NavGraphBuilder.weightScreen() {
    composable(WeightRoute.route) {
        val viewModel: WeightDetailViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        WeightDetailScreen(
            state = state,
            onWeightInputChanged = viewModel::onWeightInputChanged,
            onAddWeightClick = viewModel::addWeight,
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(),
        )
    }
}
