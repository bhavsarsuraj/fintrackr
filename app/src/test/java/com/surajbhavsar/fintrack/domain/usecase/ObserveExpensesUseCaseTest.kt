package com.surajbhavsar.fintrack.domain.usecase

import app.cash.turbine.test
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveExpensesUseCaseTest {

    private val repository: ExpenseRepository = mockk()
    private val useCase = ObserveExpensesUseCase(repository)

    private val sample = listOf(
        Expense(
            id = "a", title = "T", amountMinor = 100,
            category = Category.FOOD, note = null, occurredAt = 0,
        ),
    )

    @Test
    fun `delegates to observeExpenses when monthKey is null`() = runTest {
        every { repository.observeExpenses() } returns flowOf(sample)
        useCase(null).test {
            assertEquals(sample, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `delegates to observeExpensesForMonth when monthKey provided`() = runTest {
        every { repository.observeExpensesForMonth("2026-04") } returns flowOf(sample)
        useCase("2026-04").test {
            assertEquals(sample, awaitItem())
            awaitComplete()
        }
    }
}
