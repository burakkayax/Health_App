package com.burak.healthapp.data.nutrition

import java.text.Normalizer

/**
 * Shared Turkish-aware search text normalizer.
 * Used by both preset and custom food search to ensure consistent
 * accent-insensitive, case-insensitive matching.
 *
 * Examples: "Süt" → "sut", "Yoğurt" → "yogurt", "Çiğ" → "cig"
 */
object TurkishSearchNormalizer {
    fun normalize(text: String): String {
        val lower = text.lowercase()
            .replace('ı', 'i')
            .replace('ğ', 'g')
            .replace('ü', 'u')
            .replace('ş', 's')
            .replace('ö', 'o')
            .replace('ç', 'c')
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
