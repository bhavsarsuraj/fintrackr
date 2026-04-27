package com.surajbhavsar.fintrack.feature.expenses

import app.cash.turbine.test
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.usecase.DeleteExpenseUseCase
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ExpensesViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val expenseRepository: ExpenseRepository = mockk()
    private val deleteExpense: DeleteExpenseUseCase = mockk()
    private val preferences: PreferencesRepository = mockk()

    private fun expense(id: String, amount: Long, category: Category = Category.FOOD): Expense =
        Expense(
            id = id,
            title = "T-$id",
            amountMinor = amount,
            category = category,
            note = null,
            occurredAt = 0,
        )

    @Test
    fun `emits success state with sum total when expenses load`() = runTest {
        val expenses = listOf(expense("a", 100), expense("b", 250))
        every { expenseRepository.observeExpensesForMonth(any()) } returns flowOf(expenses)
        every { preferences.currencyCode } returns flowOf("INR")

        val viewModel = ExpensesViewModel(expenseRepository, preferences, deleteExpense)

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(true, initial.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.expenses.size)
            assertEquals(350L, loaded.totalMinor)
            assertEquals("INR", loaded.currencyCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applies category filter to expenses and total`() = runTest {
        val expenses = listOf(
            expense("a", 100, Category.FOOD),
            expense("b", 250, Category.TRANSPORT),
            expense("c", 75, Category.FOOD),
        )
        every { expenseRepository.observeExpensesForMonth(any()) } returns flowOf(expenses)
        every { preferences.currencyCode } returns flowOf("INR")

        val viewModel = ExpensesViewModel(expenseRepository, preferences, deleteExpense)

        viewModel.state.test {
            // skip loading + first success
            awaitItem()
            awaitItem()

            viewModel.setCategoryFilter(Category.FOOD)
            val filtered = awaitItem()
            assertEquals(2, filtered.expenses.size)
            assertEquals(175L, filtered.totalMinor)
            assertEquals(Category.FOOD, filtered.categoryFilter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `delegates delete to use case`() = runTest {
        every { expenseRepository.observeExpensesForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        coEvery { deleteExpense(any()) } returns Result.success(Unit)

        val viewModel = ExpensesViewModel(expenseRepository, preferences, deleteExpense)
        viewModel.onDelete("abc")
        advanceUntilIdle()

        coVerify { deleteExpense("abc") }
    }
}
