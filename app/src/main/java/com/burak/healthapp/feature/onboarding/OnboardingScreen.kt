package com.burak.healthapp.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.core.ui.components.AvatarBadge
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class OnboardingFormState(
    val name: String = "",
    val dailyCalories: String = DefaultHealthGoals.DAILY_CALORIES.toString(),
    val protein: String = DefaultHealthGoals.PROTEIN_GRAMS.toString(),
    val carbs: String = DefaultHealthGoals.CARB_GRAMS.toString(),
    val fat: String = DefaultHealthGoals.FAT_GRAMS.toString(),
    val water: String = DefaultHealthGoals.WATER_TARGET_ML.toString(),
    val sleepBedtime: String = DefaultHealthGoals.SLEEP_BEDTIME.toString(),
    val sleepWakeTime: String = DefaultHealthGoals.SLEEP_WAKE_TIME.toString(),
    val exerciseTargetDays: String = DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK.toString(),
    val exerciseTargetDuration: String = DefaultHealthGoals.EXERCISE_DURATION_MINUTES.toString(),
    val smokeDailyLimit: String = DefaultHealthGoals.SMOKE_DAILY_LIMIT.toString(),
    val targetWeight: String = DefaultHealthGoals.TARGET_WEIGHT_KG.toInt().toString(),
    val currentWeight: String = DefaultHealthGoals.BASELINE_WEIGHT_KG.toInt().toString(),
    val currentHeight: String = "",
    val currentShoulder: String = DefaultHealthGoals.BASELINE_SHOULDER_CM.toInt().toString(),
    val currentWaist: String = DefaultHealthGoals.BASELINE_WAIST_CM.toInt().toString(),
    val currentHip: String = DefaultHealthGoals.BASELINE_HIP_CM.toInt().toString(),
    val supplementsText: String = "D3 Vitamini\nOmega 3\nMultivitamin",
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    var isSaving by mutableStateOf(false)
        private set

    fun complete(form: OnboardingFormState) {
        viewModelScope.launch {
            isSaving = true
            val profile = UserProfile.fromName(
                name = form.name,
                heightCm = form.currentHeight.toFloatOrNull(),
            )
            val currentWeight = form.currentWeight.toFloatOrDefault(DefaultHealthGoals.BASELINE_WEIGHT_KG)
            val currentShoulder = form.currentShoulder.toFloatOrDefault(DefaultHealthGoals.BASELINE_SHOULDER_CM)
            val currentWaist = form.currentWaist.toFloatOrDefault(DefaultHealthGoals.BASELINE_WAIST_CM)
            val currentHip = form.currentHip.toFloatOrDefault(DefaultHealthGoals.BASELINE_HIP_CM)
            val goals = GoalSettings(
                dailyCaloriesTarget = form.dailyCalories.toIntOrDefault(DefaultHealthGoals.DAILY_CALORIES),
                proteinTargetGrams = form.protein.toIntOrDefault(DefaultHealthGoals.PROTEIN_GRAMS),
                carbTargetGrams = form.carbs.toIntOrDefault(DefaultHealthGoals.CARB_GRAMS),
                fatTargetGrams = form.fat.toIntOrDefault(DefaultHealthGoals.FAT_GRAMS),
                waterTargetMl = form.water.toIntOrDefault(DefaultHealthGoals.WATER_TARGET_ML),
                sleepTargetBedtime = form.sleepBedtime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_BEDTIME,
                sleepTargetWakeTime = form.sleepWakeTime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_WAKE_TIME,
                exerciseTargetDaysPerWeek = form.exerciseTargetDays.toIntOrDefault(DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK),
                exerciseTargetDurationMinutes = form.exerciseTargetDuration.toIntOrDefault(DefaultHealthGoals.EXERCISE_DURATION_MINUTES),
                smokeDailyLimit = form.smokeDailyLimit.toIntOrDefault(DefaultHealthGoals.SMOKE_DAILY_LIMIT),
                baselineWeightKg = currentWeight,
                targetWeightKg = form.targetWeight.toFloatOrDefault(DefaultHealthGoals.TARGET_WEIGHT_KG),
                baselineShoulderCm = currentShoulder,
                baselineWaistCm = currentWaist,
                baselineHipCm = currentHip,
            )
            val measurement = BodyMeasurementEntry(
                date = LocalDate.now(),
                weightKg = currentWeight,
                shoulderCm = currentShoulder,
                waistCm = currentWaist,
                hipCm = currentHip,
            )
            val supplements = form.supplementsText
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList()
            settingsRepository.completeOnboarding(
                profile = profile,
                goals = goals,
                initialMeasurement = measurement,
                supplements = supplements,
            )
            isSaving = false
        }
    }
}

private val OnboardingFormStateSaver = listSaver<OnboardingFormState, String>(
    save = { form ->
        listOf(
            form.name,
            form.dailyCalories,
            form.protein,
            form.carbs,
            form.fat,
            form.water,
            form.sleepBedtime,
            form.sleepWakeTime,
            form.exerciseTargetDays,
            form.exerciseTargetDuration,
            form.smokeDailyLimit,
            form.targetWeight,
            form.currentWeight,
            form.currentHeight,
            form.currentShoulder,
            form.currentWaist,
            form.currentHip,
            form.supplementsText,
        )
    },
    restore = { values ->
        OnboardingFormState(
            name = values[0],
            dailyCalories = values[1],
            protein = values[2],
            carbs = values[3],
            fat = values[4],
            water = values[5],
            sleepBedtime = values[6],
            sleepWakeTime = values[7],
            exerciseTargetDays = values[8],
            exerciseTargetDuration = values[9],
            smokeDailyLimit = values[10],
            targetWeight = values[11],
            currentWeight = values[12],
            currentHeight = values[13],
            currentShoulder = values[14],
            currentWaist = values[15],
            currentHip = values[16],
            supplementsText = values[17],
        )
    },
)

@Composable
fun OnboardingRoute() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var form by rememberSaveable(stateSaver = OnboardingFormStateSaver) {
        mutableStateOf(OnboardingFormState())
    }

    OnboardingScreen(
        currentStep = currentStep,
        form = form,
        isSaving = viewModel.isSaving,
        onStepChange = { currentStep = it },
        onFormChange = { form = it },
        onFinish = { viewModel.complete(form) },
    )
}

@Composable
private fun OnboardingScreen(
    currentStep: Int,
    form: OnboardingFormState,
    isSaving: Boolean,
    onStepChange: (Int) -> Unit,
    onFormChange: (OnboardingFormState) -> Unit,
    onFinish: () -> Unit,
) {
    val stepTitles = listOf("Hedefler", "Ölçüler", "Profil")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("onboarding_root"),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = HealthSpacing.sm,
                vertical = HealthSpacing.sm,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                ) {
                    Text(
                        text = "Sağlık",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "İlk kurulum birkaç kısa adımdan oluşur. Sonrasında dashboard doğrudan bugüne odaklanır.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                    )
                }
            }
            item {
                StepSelector(
                    stepTitles = stepTitles,
                    currentStep = currentStep,
                )
            }
            item {
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_content"),
                ) {
                    when (currentStep) {
                        0 -> GoalsStep(form = form, onFormChange = onFormChange)
                        1 -> MeasurementsStep(form = form, onFormChange = onFormChange)
                        else -> ProfileStep(form = form, onFormChange = onFormChange)
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                ) {
                    if (currentStep > 0) {
                        RoundedPillButton(
                            label = "Geri",
                            modifier = Modifier.weight(1f),
                            onClick = { onStepChange(currentStep - 1) },
                        )
                    }
                    RoundedPillButton(
                        label = if (currentStep == stepTitles.lastIndex) "Kurulumu Bitir" else "Devam",
                        modifier = Modifier.weight(1f),
                        containerColor = HealthPrimary,
                        contentColor = Color.White,
                        onClick = {
                            if (currentStep == stepTitles.lastIndex) onFinish() else onStepChange(currentStep + 1)
                        },
                    )
                }
            }
            if (isSaving) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = HealthSpacing.xs),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = HealthPrimary,
                        )
                        Spacer(modifier = Modifier.size(HealthSpacing.xs))
                        Text(
                            text = "İlk veriler hazırlanıyor",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelector(
    stepTitles: List<String>,
    currentStep: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        stepTitles.forEachIndexed { index, title ->
            val selected = index == currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(vertical = HealthSpacing.sm, horizontal = HealthSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun GoalsStep(
    form: OnboardingFormState,
    onFormChange: (OnboardingFormState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
        Text(
            text = "Beslenme ve günlük hedefler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Kalori, makro ve su hedeflerini gram/ml bazında net tutuyoruz.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OnboardingSectionTitle("Beslenme")
        FieldRow(
            leftLabel = "Günlük Kalori",
            leftValue = form.dailyCalories,
            rightLabel = "Protein (g)",
            rightValue = form.protein,
            onLeftChange = { onFormChange(form.copy(dailyCalories = it)) },
            onRightChange = { onFormChange(form.copy(protein = it)) },
        )
        FieldRow(
            leftLabel = "Karb (g)",
            leftValue = form.carbs,
            rightLabel = "Yağ (g)",
            rightValue = form.fat,
            onLeftChange = { onFormChange(form.copy(carbs = it)) },
            onRightChange = { onFormChange(form.copy(fat = it)) },
        )
        FieldRow(
            leftLabel = "Su Hedefi (ml)",
            leftValue = form.water,
            rightLabel = "Hedef Kilo",
            rightValue = form.targetWeight,
            onLeftChange = { onFormChange(form.copy(water = it)) },
            onRightChange = { onFormChange(form.copy(targetWeight = it)) },
        )
        OnboardingSectionTitle("Uyku")
        FieldRow(
            leftLabel = "Planlanan Yatış",
            leftValue = form.sleepBedtime,
            rightLabel = "Planlanan Uyanış",
            rightValue = form.sleepWakeTime,
            onLeftChange = { onFormChange(form.copy(sleepBedtime = it)) },
            onRightChange = { onFormChange(form.copy(sleepWakeTime = it)) },
        )
        OnboardingSectionTitle("Egzersiz ve sigara")
        FieldRow(
            leftLabel = "Egzersiz Gün",
            leftValue = form.exerciseTargetDays,
            rightLabel = "Egzersiz Süre (dk)",
            rightValue = form.exerciseTargetDuration,
            onLeftChange = { onFormChange(form.copy(exerciseTargetDays = it)) },
            onRightChange = { onFormChange(form.copy(exerciseTargetDuration = it)) },
        )
        FieldRow(
            leftLabel = "Sigara Limiti",
            leftValue = form.smokeDailyLimit,
            rightLabel = "",
            rightValue = "",
            onLeftChange = { onFormChange(form.copy(smokeDailyLimit = it)) },
            onRightChange = {},
        )
    }
}

@Composable
private fun MeasurementsStep(
    form: OnboardingFormState,
    onFormChange: (OnboardingFormState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
        Text(
            text = "Mevcut ölçüler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Omuz, bel ve kalça ölçülerini tarih bazlı kayıtları başlatmak için alıyoruz.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FieldRow(
            leftLabel = "Mevcut Kilo",
            leftValue = form.currentWeight,
            rightLabel = "Mevcut Omuz",
            rightValue = form.currentShoulder,
            onLeftChange = { onFormChange(form.copy(currentWeight = it)) },
            onRightChange = { onFormChange(form.copy(currentShoulder = it)) },
        )
        FieldRow(
            leftLabel = "Boy (cm)",
            leftValue = form.currentHeight,
            rightLabel = "",
            rightValue = "",
            onLeftChange = { onFormChange(form.copy(currentHeight = it)) },
            onRightChange = {},
        )
        FieldRow(
            leftLabel = "Mevcut Bel",
            leftValue = form.currentWaist,
            rightLabel = "Mevcut Kalça",
            rightValue = form.currentHip,
            onLeftChange = { onFormChange(form.copy(currentWaist = it)) },
            onRightChange = { onFormChange(form.copy(currentHip = it)) },
        )
    }
}

@Composable
private fun ProfileStep(
    form: OnboardingFormState,
    onFormChange: (OnboardingFormState) -> Unit,
) {
    val profile = remember(form.name) { UserProfile.fromName(form.name) }

    Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
        Text(
            text = "Profil ve takviyeler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Takviyeleri her satıra bir adet gelecek şekilde yazabilirsin.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            AvatarBadge(initials = profile.avatarInitials)
            Column {
                Text(
                    text = if (form.name.isBlank()) "İsmini gir" else profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Avatar baş harflerden üretilecek",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HealthPillTextField(
            label = "Ad Soyad",
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
        )
        HealthPillTextField(
            label = "Takviyeler",
            value = form.supplementsText,
            onValueChange = { onFormChange(form.copy(supplementsText = it)) },
            singleLine = false,
            minLines = 5,
        )
    }
}

@Composable
private fun FieldRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useSingleColumn = maxWidth < 360.dp || rightLabel.isBlank()
        if (useSingleColumn) {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                OnboardingPillTextField(
                    label = leftLabel,
                    value = leftValue,
                    onValueChange = onLeftChange,
                )
                if (rightLabel.isNotBlank()) {
                    OnboardingPillTextField(
                        label = rightLabel,
                        value = rightValue,
                        onValueChange = onRightChange,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                OnboardingPillTextField(
                    label = leftLabel,
                    value = leftValue,
                    modifier = Modifier.weight(1f),
                    onValueChange = onLeftChange,
                )
                OnboardingPillTextField(
                    label = rightLabel,
                    value = rightValue,
                    modifier = Modifier.weight(1f),
                    onValueChange = onRightChange,
                )
            }
        }
    }
}

@Composable
private fun OnboardingSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = HealthSpacing.xs),
    )
}

@Composable
private fun OnboardingPillTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

private fun String.toIntOrDefault(fallback: Int): Int = toIntOrNull() ?: fallback
private fun String.toFloatOrDefault(fallback: Float): Float = toFloatOrNull() ?: fallback
private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
