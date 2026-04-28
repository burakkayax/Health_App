package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.ExerciseEntry

fun countExerciseDays(entries: List<ExerciseEntry>): Int = entries.map(ExerciseEntry::date).distinct().size
