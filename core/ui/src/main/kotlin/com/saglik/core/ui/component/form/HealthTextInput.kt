package com.saglik.core.ui.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.designsystem.theme.HealthTypography

@Composable
fun HealthTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = HealthTypography.labelMedium,
            color = HealthColors.SecondaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(HealthShapeTokens.pill)
                .border(
                    width = 1.dp,
                    color = if (isError) {
                        HealthColors.ActivityOrange.copy(alpha = 0.72f)
                    } else {
                        HealthColors.GlassBorder
                    },
                    shape = HealthShapeTokens.pill,
                )
                .background(HealthColors.GlassSurface.copy(alpha = 0.54f))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = HealthTypography.titleLarge.copy(
                    color = HealthColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                ),
                cursorBrush = SolidColor(HealthColors.SystemBlue),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = HealthTypography.titleLarge,
                                color = HealthColors.TertiaryText.copy(alpha = 0.7f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}
