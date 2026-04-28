package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.AvatarBadge
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
internal fun ProfileHeaderCard(state: ProfileUiState) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            AvatarBadge(initials = state.avatarInitials)
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                Text(
                    text = state.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.profile_summary_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
