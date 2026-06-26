package com.saglik.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val HealthShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

object HealthShapeTokens {
    val card = RoundedCornerShape(32.dp)
    val bottomBar = RoundedCornerShape(38.dp)
    val pill = RoundedCornerShape(999.dp)
}
