package com.surajbhavsar.fintrack.domain.usecase

import app.cash.turbine.test
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMonthlyInsightsUseCaseTest {

    private val expenseRepo: ExpenseRepository = mockk()
    private val budgetRepo: BudgetRepository = mockk()
    private val useCase = GetMonthlyInsightsUseCase(expenseRepo, budgetRepo)

    @Test
    fun `combines totals and budgets into MonthlyInsight`() = runTest {
        val totals = mapOf(
            Category.FOOD to 500L,
            Category.TRANSPORT to 300L,
        )
        val budgets = listOf(
            Budget(id = "b1", category = Category.FOOD, monthKey = "2026-04", limitMinor = 1000L),
        )
        every { expenseRepo.observeTotalsByCategoryForMonth("2026-04") } returns flowOf(totals)
        every { budgetRepo.observeBudgetsForMonth("2026-04") } returns flowOf(budgets)

        useCase("2026-04").test {
            val insight = awaitItem()
            assertEquals("2026-04", insight.monthKey)
            assertEquals(800L, insight.totalMinor)
            assertEquals(2, insight.byCategory.size)
            assertEquals(1, insight.budgetUsage.size)
            val foodUsage = insight.budgetUsage.first()
            assertEquals(Category.FOOD, foodUsage.category)
            assertEquals(500L, foodUsage.spentMinor)
            assertEquals(1000L, foodUsage.limitMinor)
            assertEquals(0.5f, foodUsage.percentUsed, 0.0001f)
            awaitComplete()
        }
    }

    @Test
    fun `budget without spend shows zero spent`() = runTest {
        val budgets = listOf(
            Budget(id = "b1", category = Category.HEALTH, monthKey = "2026-04", limitMinor = 500L),
        )
        every { expenseRepo.observeTotalsByCategoryForMonth("2026-04") } returns flowOf(emptyMap())
        every { budgetRepo.observeBudgetsForMonth("2026-04") } returns flowOf(budgets)

        useCase("2026-04").test {
            val insight = awaitItem()
            assertEquals(0L, insight.totalMinor)
            assertEquals(0L, insight.budgetUsage.first().spentMinor)
            awaitComplete()
        }
    }
}
