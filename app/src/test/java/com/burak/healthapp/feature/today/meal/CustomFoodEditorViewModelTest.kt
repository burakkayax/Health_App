package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.repository.CustomFoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CustomFoodEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun editExistingCustomFood_preservesFiberSugarSodium() = runTest {
        val repository = FakeCustomFoodRepository(existingFood = customFoodWithOptionalNutrients())
        val viewModel = CustomFoodEditorViewModel(repository)

        viewModel.loadFood(1L)
        advanceUntilIdle()
        viewModel.save(onSuccess = {})
        advanceUntilIdle()

        assertEquals(5.5f, repository.savedFood?.fiberGrams)
        assertEquals(2f, repository.savedFood?.sugarGrams)
        assertEquals(120f, repository.savedFood?.sodiumMg)
    }

    @Test
    fun editExistingCustomFood_nameChangeDoesNotClearOptionalNutrients() = runTest {
        val repository = FakeCustomFoodRepository(existingFood = customFoodWithOptionalNutrients())
        val viewModel = CustomFoodEditorViewModel(repository)

        viewModel.loadFood(1L)
        advanceUntilIdle()
        viewModel.onNameChange("Yulaf ezmesi")
        viewModel.save(onSuccess = {})
        advanceUntilIdle()

        assertEquals("Yulaf ezmesi", repository.savedFood?.name)
        assertEquals(5.5f, repository.savedFood?.fiberGrams)
        assertEquals(2f, repository.savedFood?.sugarGrams)
        assertEquals(120f, repository.savedFood?.sodiumMg)
    }

    @Test
    fun editExistingCustomFood_macroChangeDoesNotClearOptionalNutrients() = runTest {
        val repository = FakeCustomFoodRepository(existingFood = customFoodWithOptionalNutrients())
        val viewModel = CustomFoodEditorViewModel(repository)

        viewModel.loadFood(1L)
        advanceUntilIdle()
        viewModel.onCaloriesChange("360")
        viewModel.onProteinChange("12")
        viewModel.save(onSuccess = {})
        advanceUntilIdle()

        assertEquals(360, repository.savedFood?.calories)
        assertEquals(12, repository.savedFood?.proteinGrams)
        assertEquals(5.5f, repository.savedFood?.fiberGrams)
        assertEquals(2f, repository.savedFood?.sugarGrams)
        assertEquals(120f, repository.savedFood?.sodiumMg)
    }

    @Test
    fun newCustomFood_withoutOptionalFields_savesNullOptionalNutrients() = runTest {
        val repository = FakeCustomFoodRepository(existingFood = null)
        val viewModel = CustomFoodEditorViewModel(repository)

        viewModel.resetForAdd()
        viewModel.onNameChange("Yeni besin")
        viewModel.onServingNameChange("porsiyon")
        viewModel.onServingGramsChange("100")
        viewModel.onCaloriesChange("100")
        viewModel.save(onSuccess = {})
        advanceUntilIdle()

        assertNull(repository.savedFood?.fiberGrams)
        assertNull(repository.savedFood?.sugarGrams)
        assertNull(repository.savedFood?.sodiumMg)
    }

    private fun customFoodWithOptionalNutrients(): CustomFood = CustomFood(
        id = 1L,
        name = "Yulaf",
        brand = "Ev",
        servingName = "kase",
        servingGrams = 100f,
        calories = 350,
        proteinGrams = 10,
        carbsGrams = 50,
        fatGrams = 5,
        fiberGrams = 5.5f,
        sugarGrams = 2f,
        sodiumMg = 120f,
        isFavorite = true,
        createdAt = LocalDateTime.parse("2026-04-27T10:00:00"),
        updatedAt = LocalDateTime.parse("2026-04-27T11:00:00"),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
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

private class FakeCustomFoodRepository(
    private val existingFood: CustomFood?,
) : CustomFoodRepository {
    private val foods = MutableStateFlow(existingFood?.let(::listOf).orEmpty())
    var savedFood: CustomFood? = null

    override fun observeAll(): Flow<List<CustomFood>> = foods

    override suspend fun getAll(): List<CustomFood> = foods.value

    override suspend fun searchCustomFoods(query: String): List<CustomFood> = foods.value

    override suspend fun getById(id: Long): CustomFood? = existingFood?.takeIf { it.id == id }

    override suspend fun save(food: CustomFood): Long {
        savedFood = food
        foods.value = listOf(food.copy(id = food.id.takeIf { it != 0L } ?: 1L))
        return foods.value.single().id
    }

    override suspend fun delete(id: Long) {
        foods.value = foods.value.filterNot { it.id == id }
    }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        foods.value = foods.value.map { food ->
            if (food.id == id) food.copy(isFavorite = isFavorite) else food
        }
    }

    override suspend fun deleteAll() {
        foods.value = emptyList()
    }
}
