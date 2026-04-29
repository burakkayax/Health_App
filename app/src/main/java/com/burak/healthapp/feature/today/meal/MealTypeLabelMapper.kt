package com.burak.healthapp.feature.today.meal

import androidx.annotation.StringRes
import com.burak.healthapp.R
import com.burak.healthapp.domain.model.MealType

@get:StringRes
val MealType.labelResId: Int
    get() = when (this) {
        MealType.BREAKFAST -> R.string.meal_type_breakfast
        MealType.LUNCH -> R.string.meal_type_lunch
        MealType.DINNER -> R.string.meal_type_dinner
        MealType.SNACK -> R.string.meal_type_snack
    }
