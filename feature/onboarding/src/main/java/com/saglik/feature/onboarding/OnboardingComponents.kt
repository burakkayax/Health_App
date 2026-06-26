package com.saglik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.ui.screen.GlassScaffold

@Composable
internal fun OnboardingScaffold(
    currentStep: OnboardingStep,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassScaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = HealthSpacing.screenHorizontal),
        ) {
            OnboardingTopBar(
                currentStep = currentStep,
                showBackButton = showBackButton,
                onBackClick = onBackClick,
                modifier = Modifier.padding(top = 12.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 22.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                content = content,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = footer,
            )
        }
    }
}

@Composable
private fun OnboardingTopBar(
    currentStep: OnboardingStep,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackButton) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.62f))
                    .border(1.dp, HealthColors.GlassBorder, CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HealthColors.Ink,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(42.dp))
        }
        OnboardingProgressIndicator(
            progress = (currentStep.ordinal + 1).toFloat() / OnboardingStep.entries.size.toFloat(),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
        )
        Spacer(modifier = Modifier.size(42.dp))
    }
}

@Composable
internal fun OnboardingProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(7.dp)
            .clip(HealthShapeTokens.pill)
            .background(Color.White.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(HealthShapeTokens.pill)
                .background(HealthColors.SystemBlue),
        )
    }
}

@Composable
internal fun OnboardingStepHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = if (large) MaterialTheme.typography.displayLarge else MaterialTheme.typography.headlineMedium,
            color = HealthColors.Ink,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = HealthColors.SecondaryText,
        )
    }
}

@Composable
internal fun OnboardingSelectionCard(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = HealthShapeTokens.card
    val borderColor = if (selected) {
        HealthColors.SystemBlue.copy(alpha = 0.84f)
    } else {
        HealthColors.GlassBorder
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (selected) 18.dp else 10.dp,
                shape = shape,
                ambientColor = HealthColors.Shadow,
                spotColor = HealthColors.Shadow,
            )
            .border(1.dp, borderColor, shape)
            .clip(shape)
            .background(
                if (selected) {
                    HealthColors.LightBlue.copy(alpha = 0.58f)
                } else {
                    HealthColors.GlassSurface
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(1.5.dp, borderColor, CircleShape)
                .background(if (selected) HealthColors.SystemBlue else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = HealthColors.Ink,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthColors.SecondaryText,
                )
            }
        }
    }
}

@Composable
internal fun OnboardingNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    val shape = HealthShapeTokens.card
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.displayLarge.copy(
            color = HealthColors.Ink,
            fontWeight = FontWeight.Bold,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(HealthColors.SystemBlue),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 116.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = shape,
                        ambientColor = HealthColors.Shadow,
                        spotColor = HealthColors.Shadow,
                    )
                    .border(1.dp, HealthColors.GlassBorder, shape)
                    .clip(shape)
                    .background(Color.White.copy(alpha = 0.78f))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.displayLarge,
                            color = HealthColors.TertiaryText,
                        )
                    }
                    innerTextField()
                }
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleLarge,
                    color = HealthColors.SecondaryText,
                )
            }
        },
    )
}

@Composable
internal fun OnboardingPrimaryButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = HealthShapeTokens.pill,
        contentPadding = PaddingValues(horizontal = 22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthColors.SystemBlue,
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.56f),
            disabledContentColor = HealthColors.SecondaryText,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (text == "Start Tracking") {
                    Icons.Rounded.Check
                } else {
                    Icons.AutoMirrored.Rounded.ArrowForward
                },
                contentDescription = null,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
internal fun OnboardingError(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return

    Text(
        text = message,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = HealthColors.ActivityOrange,
    )
}
