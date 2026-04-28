package com.burak.healthapp.feature.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.burak.healthapp.feature.app.toLocalDateUtc
import com.burak.healthapp.feature.app.toPickerMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal fun LocalDate.toPickerMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

internal fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
