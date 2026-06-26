package com.saglik.domain.usecase

import com.saglik.domain.bmi.BmiCalculator
import com.saglik.domain.bmi.BmiCategoryMapper
import com.saglik.domain.bmi.BmiMissingReason
import com.saglik.domain.bmi.BmiResult
import com.saglik.domain.bmi.BmiSummary
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveBmiSummaryUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val weightRepository: WeightRepository,
    private val calculator: BmiCalculator = BmiCalculator(),
    private val categoryMapper: BmiCategoryMapper = BmiCategoryMapper(),
) {
    operator fun invoke(): Flow<BmiSummary> =
        combine(
            userProfileRepository.observeProfile(),
            weightRepository.observeLatestWeightEntry(),
        ) { profile, latestWeight ->
            when {
                profile == null -> missing(BmiMissingReason.MISSING_PROFILE)
                profile.heightCm <= 0f -> missing(BmiMissingReason.MISSING_HEIGHT)
                latestWeight == null -> missing(BmiMissingReason.MISSING_WEIGHT)
                else -> {
                    val bmi = calculator.calculate(
                        weightKg = latestWeight.weightKg,
                        heightCm = profile.heightCm,
                    )

                    if (bmi == null) {
                        missing(BmiMissingReason.INVALID_INPUT)
                    } else {
                        BmiSummary(
                            bmi = BmiResult(
                                value = bmi,
                                category = categoryMapper.map(bmi),
                            ),
                            hasData = true,
                            missingReason = null,
                        )
                    }
                }
            }
        }

    private fun missing(reason: BmiMissingReason): BmiSummary =
        BmiSummary(
            bmi = null,
            hasData = false,
            missingReason = reason,
        )
}
