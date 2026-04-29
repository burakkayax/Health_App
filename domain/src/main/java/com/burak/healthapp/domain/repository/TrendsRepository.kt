package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TrendsRepository {
    fun observeTrends(
        period: TrendsPeriod,
        endDate: LocalDate = LocalDate.now(),
    ): Flow<TrendsSnapshot>
}
