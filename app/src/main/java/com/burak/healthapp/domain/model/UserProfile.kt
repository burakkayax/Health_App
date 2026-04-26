package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class UserProfile(
    val name: String = "Misafir",
    val avatarInitials: String = "M",
    val heightCm: Float? = null,
) {
    companion object {
        fun fromName(name: String, heightCm: Float? = null): UserProfile {
            val trimmed = name.trim().ifBlank { "Misafir" }
            val initials = trimmed
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "M" }
            return UserProfile(
                name = trimmed,
                avatarInitials = initials,
                heightCm = heightCm,
            )
        }
    }
}
