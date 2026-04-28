package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.domain.model.SmokingEntry

fun SmokingEntryEntity.toDomain(): SmokingEntry = SmokingEntry(
    id = id,
    date = date,
    count = count,
)
