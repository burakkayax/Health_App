package com.saglik.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.designsystem.theme.HealthTheme
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.form.HealthPrimaryButton
import com.saglik.core.ui.component.state.HealthInlineStatusMessage
import com.saglik.core.ui.screen.HealthGradientBackground

class HealthConnectPermissionRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthTheme {
                HealthConnectPermissionRationaleScreen(onDoneClick = ::finish)
            }
        }
    }
}

@Composable
private fun HealthConnectPermissionRationaleScreen(
    onDoneClick: () -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    HealthGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = HealthSpacing.screenHorizontal,
                    top = statusBarTop + 24.dp,
                    end = HealthSpacing.screenHorizontal,
                    bottom = navBarBottom + 24.dp,
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            GlassHealthCard {
                Text(
                    text = "Health Connect permissions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HealthColors.Ink,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Health App asks for read access to weight and sleep data so future sync can be enabled after you choose to connect it.",
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText,
                )
                HealthInlineStatusMessage(
                    message = "No Health Connect data is being imported yet.",
                    modifier = Modifier.padding(top = 16.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "You can grant or revoke these permissions at any time in Health Connect settings.",
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText,
                )
                HealthPrimaryButton(
                    text = "Done",
                    onClick = onDoneClick,
                    modifier = Modifier.padding(top = 22.dp),
                )
            }
        }
    }
}
