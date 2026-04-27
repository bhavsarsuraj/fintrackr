package com.surajbhavsar.fintrack.feature.insights

import app.cash.turbine.test
import com.surajbhavsar.fintrack.core.ui.UiState
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.MonthlyInsight
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.usecase.GetMonthlyInsightsUseCase
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val getInsights: GetMonthlyInsightsUseCase = mockk()
    private val preferences: PreferencesRepository = mockk()

    @Test
    fun `emits Success when insight stream produces a value`() = runTest {
        val insight = MonthlyInsight(
            monthKey = "2026-04",
            totalMinor = 100,
            byCategory = mapOf(Category.FOOD to 100L),
            budgetUsage = emptyList(),
        )
        every { getInsights(any()) } returns flowOf(insight)
        every { preferences.currencyCode } returns flowOf("INR")

        val vm = InsightsViewModel(getInsights, preferences)
        advanceUntilIdle()

        vm.state.test {
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(insight, (success as UiState.Success).data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMonth updates monthKey state`() = runTest {
        val empty = MonthlyInsight("2026-04", 0L, emptyMap(), emptyList())
        every { getInsights(any()) } returns flowOf(empty)
        every { preferences.currencyCode } returns flowOf("INR")

        val vm = InsightsViewModel(getInsights, preferences)
        vm.setMonth("2026-03")
        advanceUntilIdle()

        assertEquals("2026-03", vm.monthKey.value)
    }
}
