package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.ExerciseEntry

fun countExerciseDays(entries: List<ExerciseEntry>): Int {
    return entries.map(ExerciseEntry::date).distinct().size
}
