@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.data.repository

import com.saglik.core.database.entity.UserProfileEntity
import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import com.saglik.core.model.UserProfile
import kotlinx.datetime.Instant

internal fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        id = id,
        sex = enumValueOfOrDefault(sex, Sex.UNSPECIFIED),
        age = age,
        birthDate = birthDate,
        heightCm = heightCm,
        goal = enumValueOfOrDefault(goal, HealthGoal.GENERAL_HEALTH),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )

internal fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        sex = sex.name,
        age = age,
        birthDate = birthDate,
        heightCm = heightCm,
        goal = goal.name,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
    )

private inline fun <reified T : Enum<T>> enumValueOfOrDefault(value: String, fallback: T): T =
    runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
