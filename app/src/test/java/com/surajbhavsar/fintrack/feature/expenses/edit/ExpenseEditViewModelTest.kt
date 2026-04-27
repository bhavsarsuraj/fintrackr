package com.surajbhavsar.fintrack.feature.expenses.edit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.usecase.AddExpenseUseCase
import com.surajbhavsar.fintrack.domain.usecase.UpdateExpenseUseCase
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseEditViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private val addExpense: AddExpenseUseCase = mockk()
    private val updateExpense: UpdateExpenseUseCase = mockk()

    @Test
    fun `add mode starts with empty defaults`() = runTest {
        val vm = ExpenseEditViewModel(SavedStateHandle(), expenseRepository, addExpense, updateExpense)
        vm.state.test {
            val initial = awaitItem()
            assertNull(initial.id)
            assertEquals("", initial.title)
            assertEquals("", initial.amount)
            assertEquals(Category.OTHER, initial.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `edit mode loads existing expense and prefills fields`() = runTest {
        val savedState = SavedStateHandle().apply { set("expenseId", "id-1") }
        val existing = Expense(
            id = "id-1", title = "Lunch", amountMinor = 4500,
            category = Category.FOOD, note = "with team", occurredAt = 1234L,
        )
        coEvery { expenseRepository.getById("id-1") } returns existing

        val vm = ExpenseEditViewModel(savedState, expenseRepository, addExpense, updateExpense)

        vm.state.test {
            // initial loading state, then loaded
            val first = awaitItem()
            assertTrue(first.isLoading)
            advanceUntilIdle()
            val loaded = awaitItem()
            assertEquals("id-1", loaded.id)
            assertEquals("Lunch", loaded.title)
            assertEquals(Category.FOOD, loaded.category)
            assertEquals("with team", loaded.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `field setters update state`() = runTest {
        val vm = ExpenseEditViewModel(SavedStateHandle(), expenseRepository, addExpense, updateExpense)

        vm.onTitleChange("Bagels")
        vm.onAmountChange("12.50")
        vm.onNoteChange("breakfast")
        vm.onCategoryChange(Category.GROCERIES)
        vm.onDateChange(9999L)

        val s = vm.state.value
        assertEquals("Bagels", s.title)
        assertEquals("12.50", s.amount)
        assertEquals("breakfast", s.note)
        assertEquals(Category.GROCERIES, s.category)
        assertEquals(9999L, s.occurredAt)
    }

    @Test
    fun `amount input strips non-numeric characters`() = runTest {
        val vm = ExpenseEditViewModel(SavedStateHandle(), expenseRepository, addExpense, updateExpense)

        vm.onAmountChange("ab12.34cd")
        assertEquals("12.34", vm.state.value.amount)
    }

    @Test
    fun `save in add mode invokes AddExpenseUseCase and flips saved`() = runTest {
        coEvery { addExpense(any()) } returns Result.success(Unit)
        val vm = ExpenseEditViewModel(SavedStateHandle(), expenseRepository, addExpense, updateExpense)
        vm.onTitleChange("Tea")
        vm.onAmountChange("3.00")

        vm.save()
        advanceUntilIdle()

        coVerify { addExpense(any()) }
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun `save propagates failure as errorMessage`() = runTest {
        coEvery { addExpense(any()) } returns Result.failure(IllegalArgumentException("Title is required"))
        val vm = ExpenseEditViewModel(SavedStateHandle(), expenseRepository, addExpense, updateExpense)

        vm.save()
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun `save in edit mode calls UpdateExpenseUseCase`() = runTest {
        val savedState = SavedStateHandle().apply { set("expenseId", "id-1") }
        val existing = Expense(
            id = "id-1", title = "T", amountMinor = 100,
            category = Category.OTHER, note = null, occurredAt = 0L,
        )
        coEvery { expenseRepository.getById("id-1") } returns existing
        coEvery { updateExpense(any()) } returns Result.success(Unit)

        val vm = ExpenseEditViewModel(savedState, expenseRepository, addExpense, updateExpense)
        advanceUntilIdle()

        vm.onAmountChange("9.99")
        vm.save()
        advanceUntilIdle()

        coVerify { updateExpense(any()) }
        assertTrue(vm.state.value.saved)
    }
}
