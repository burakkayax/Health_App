package com.burak.healthapp.domain.calculation

import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val hourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatHourMinute(time: LocalTime): String = time.format(hourMinuteFormatter)
