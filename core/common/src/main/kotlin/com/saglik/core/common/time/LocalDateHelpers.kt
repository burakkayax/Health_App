package com.saglik.core.common.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

fun LocalDate.startOfIsoWeek(): LocalDate = minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
