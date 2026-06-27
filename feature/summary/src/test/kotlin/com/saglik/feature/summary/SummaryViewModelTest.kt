@file:OptIn(
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
    kotlin.time.ExperimentalTime::class,
)

package com.saglik.feature.summary

import com.saglik.core.model.DataSource
import com.saglik.core.model.DateRange
import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseType
import com.saglik.core.model.SleepEntry
import com.saglik.core.model.StepsEntry
import com.saglik.core.model.UserProfile
import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.repository.ExerciseRepository
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.repository.StepsRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveExerciseSummaryUseCase
import com.saglik.domain.usecase.ObserveLatestWeightEntryUseCase
import com.saglik.domain.usecase.ObserveSleepSummaryUseCase
import com.saglik.domain.usecase.ObserveStepsSummaryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class SummaryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadingIncludesStepsAndExerciseStates() {
        val state = SummaryUiState.loading()

        assertEquals("Loading", state.steps.primaryText)
        assertEquals("Reading steps", state.steps.secondaryText)
        assertEquals("Last 7 days unavailable", state.steps.weeklyText)
        assertEquals("Loading", state.exercise.primaryText)
        assertEquals("Reading sessions", state.exercise.secondaryText)
        assertEquals("No sessions logged", state.exercise.latestText)
    }

    @Test
    fun mapsEmptyStepsToNoStepsYet() = runTest {
        val state = createViewModel().loadedState()

        assertEquals("No steps yet", state.steps.primaryText)
        assertEquals("Sync Health Connect to import steps", state.steps.secondaryText)
        assertEquals("Last 7 days unavailable", state.steps.weeklyText)
    }

    @Test
    fun mapsNonEmptyStepsToFormattedTodayAndWeeklyTotals() = runTest {
        val state = createViewModel(
            steps = listOf(
                stepsEntry(
                    id = "today",
                    start = "2026-06-27T08:00:00Z",
                    end = "2026-06-27T09:00:00Z",
                    count = 6_240L,
                ),
                stepsEntry(
                    id = "week",
                    start = "2026-06-22T08:00:00Z",
                    end = "2026-06-22T09:00:00Z",
                    count = 35_860L,
                ),
            ),
        ).loadedState()

        assertEquals("6,240 steps", state.steps.primaryText)
        assertEquals("Today", state.steps.secondaryText)
        assertEquals("42,100 in 7 days", state.steps.weeklyText)
    }

    @Test
    fun mapsEmptyExerciseToNoExerciseYet() = runTest {
        val state = createViewModel().loadedState()

        assertEquals("No exercise yet", state.exercise.primaryText)
        assertEquals("Sync Health Connect or add a session later", state.exercise.secondaryText)
        assertEquals("No sessions logged", state.exercise.latestText)
    }

    @Test
    fun mapsNonEmptyExerciseToSessionCountAndDuration() = runTest {
        val state = createViewModel(
            exerciseSessions = listOf(
                exerciseSession(
                    id = "exercise-1",
                    end = "2026-06-26T10:00:00Z",
                    durationMinutes = 90,
                    title = "Morning walk",
                ),
                exerciseSession(
                    id = "exercise-2",
                    end = "2026-06-27T11:00:00Z",
                    durationMinutes = 95,
                    title = "Evening ride",
                ),
            ),
        ).loadedState()

        assertEquals("2 sessions", state.exercise.primaryText)
        assertEquals("185 min total", state.exercise.secondaryText)
        assertEquals("Evening ride", state.exercise.latestText)
    }

    private suspend fun SummaryViewModel.loadedState(): SummaryUiState =
        uiState.drop(1).first()

    private fun createViewModel(
        steps: List<StepsEntry> = emptyList(),
        exerciseSessions: List<ExerciseSession> = emptyList(),
    ): SummaryViewModel {
        val weightRepository = FakeWeightRepository()
        return SummaryViewModel(
            observeLatestWeightEntryUseCase = ObserveLatestWeightEntryUseCase(weightRepository),
            observeBmiSummaryUseCase = ObserveBmiSummaryUseCase(
                userProfileRepository = FakeUserProfileRepository(),
                weightRepository = weightRepository,
            ),
            observeSleepSummaryUseCase = ObserveSleepSummaryUseCase(FakeSleepRepository()),
            observeStepsSummaryUseCase = ObserveStepsSummaryUseCase(
                repository = FakeStepsRepository(steps),
                todayProvider = { LocalDate(2026, 6, 27) },
                timeZone = TimeZone.UTC,
            ),
            observeExerciseSummaryUseCase = ObserveExerciseSummaryUseCase(
                FakeExerciseRepository(exerciseSessions),
            ),
        )
    }

    private class FakeWeightRepository : WeightRepository {
        override fun observeLatestWeightEntry(): Flow<WeightEntry?> = flowOf(null)

        override fun observeWeightEntries(): Flow<List<WeightEntry>> = flowOf(emptyList())

        override suspend fun addWeightEntry(entry: WeightEntry) = Unit
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        override fun observeProfile(): Flow<UserProfile?> = flowOf(null)

        override suspend fun saveProfile(profile: UserProfile) = Unit
    }

    private class FakeSleepRepository : SleepRepository {
        override fun observeAllSleepEntries(): Flow<List<SleepEntry>> = flowOf(emptyList())

        override fun observeLatestSleepEntry(): Flow<SleepEntry?> = flowOf(null)

        override fun observeSleepEntries(range: DateRange): Flow<List<SleepEntry>> =
            flowOf(emptyList())

        override suspend fun addSleepEntry(entry: SleepEntry) = Unit
    }

    private class FakeStepsRepository(
        private val entries: List<StepsEntry>,
    ) : StepsRepository {
        override fun observeStepsEntries(): Flow<List<StepsEntry>> = flowOf(entries)

        override fun observeStepsEntriesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<StepsEntry>> =
            flowOf(
                entries.filter {
                    it.startTimeMillis < endExclusive && it.endTimeMillis > startInclusive
                },
            )
    }

    private class FakeExerciseRepository(
        private val sessions: List<ExerciseSession>,
    ) : ExerciseRepository {
        override fun observeExerciseSessions(): Flow<List<ExerciseSession>> = flowOf(sessions)

        override fun observeExerciseSessionsBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<ExerciseSession>> =
            flowOf(
                sessions.filter {
                    it.endTimeMillis >= startInclusive && it.endTimeMillis < endExclusive
                },
            )

        override suspend fun addExerciseSession(input: AddExerciseSessionInput) = Unit
    }

    private fun stepsEntry(
        id: String,
        start: String,
        end: String,
        count: Long,
    ): StepsEntry =
        StepsEntry(
            id = id,
            startTimeMillis = Instant.parse(start).toEpochMilliseconds(),
            endTimeMillis = Instant.parse(end).toEpochMilliseconds(),
            count = count,
            source = DataSource.HEALTH_CONNECT,
            note = null,
            sourceRecordId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = "Example Source",
            createdAt = Instant.parse(end).toEpochMilliseconds(),
            updatedAt = Instant.parse(end).toEpochMilliseconds(),
            lastSyncedAt = null,
            deletedAt = null,
        )

    private fun exerciseSession(
        id: String,
        end: String,
        durationMinutes: Int,
        title: String?,
    ): ExerciseSession {
        val endTimeMillis = Instant.parse(end).toEpochMilliseconds()
        return ExerciseSession(
            id = id,
            startTimeMillis = endTimeMillis - durationMinutes * 60_000L,
            endTimeMillis = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = ExerciseType.CYCLING,
            title = title,
            notes = null,
            source = DataSource.HEALTH_CONNECT,
            sourceRecordId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = "Example Source",
            createdAt = endTimeMillis,
            updatedAt = endTimeMillis,
            lastSyncedAt = null,
            deletedAt = null,
        )
    }
}

class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
