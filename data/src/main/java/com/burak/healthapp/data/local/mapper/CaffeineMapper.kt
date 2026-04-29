package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry

fun CaffeineEntryEntity.toDomain(): CaffeineEntry = CaffeineEntry(
    id = id,
    date = date,
    time = time,
    drinkType = CaffeineDrinkType.entries.firstOrNull { it.name == drinkType } ?: CaffeineDrinkType.OTHER,
    size = CaffeineDrinkSize.entries.firstOrNull { it.name == size } ?: CaffeineDrinkSize.MEDIUM,
    estimatedMg = estimatedMg,
    customName = customName,
    createdAt = createdAt,
)

fun CaffeineEntry.toEntity(): CaffeineEntryEntity = CaffeineEntryEntity(
    id = id,
    date = date,
    time = time,
    drinkType = drinkType.name,
    size = size.name,
    estimatedMg = estimatedMg,
    customName = customName?.trim()?.ifBlank { null },
    createdAt = createdAt,
)
