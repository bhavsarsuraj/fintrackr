package com.surajbhavsar.fintrack.feature.budgets

import app.cash.turbine.test
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val budgetRepo: BudgetRepository = mockk(relaxed = true)
    private val preferences: PreferencesRepository = mockk(relaxed = true)

    private val sample = Budget(id = "b1", category = Category.FOOD, monthKey = "2026-04", limitMinor = 1000L)

    @Test
    fun `emits success state with budgets`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns flowOf(listOf(sample))
        every { preferences.currencyCode } returns flowOf("INR")

        val vm = BudgetsViewModel(budgetRepo, preferences)
        vm.state.test {
            awaitItem() // initial
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.budgets.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startAdd opens editor with defaults`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        val vm = BudgetsViewModel(budgetRepo, preferences)

        vm.startAdd()
        assertNotNull(vm.state.value.editor)
        assertEquals(Category.FOOD, vm.state.value.editor?.category)
    }

    @Test
    fun `startEdit prefills editor from existing budget`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        val vm = BudgetsViewModel(budgetRepo, preferences)

        vm.startEdit(sample)
        val editor = vm.state.value.editor
        assertNotNull(editor)
        assertEquals("b1", editor!!.existingId)
        assertEquals(Category.FOOD, editor.category)
        assertEquals("10.0", editor.amount)
    }

    @Test
    fun `saveEditor with invalid amount sets error`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        val vm = BudgetsViewModel(budgetRepo, preferences)

        vm.startAdd()
        vm.onEditorAmount("0")
        vm.saveEditor()
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
        coVerify(exactly = 0) { budgetRepo.upsertBudget(any()) }
    }

    @Test
    fun `saveEditor persists and closes editor on success`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        val captured = slot<Budget>()
        coEvery { budgetRepo.upsertBudget(capture(captured)) } returns Unit

        val vm = BudgetsViewModel(budgetRepo, preferences)
        vm.startAdd()
        vm.onEditorCategory(Category.HEALTH)
        vm.onEditorAmount("250.00")
        vm.saveEditor()
        advanceUntilIdle()

        assertEquals(Category.HEALTH, captured.captured.category)
        assertEquals(25_000L, captured.captured.limitMinor)
        assertNull(vm.state.value.editor)
    }

    @Test
    fun `delete delegates to repo`() = runTest {
        every { budgetRepo.observeBudgetsForMonth(any()) } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        coEvery { budgetRepo.deleteBudget("b1") } returns Unit

        val vm = BudgetsViewModel(budgetRepo, preferences)
        vm.delete("b1")
        advanceUntilIdle()

        coVerify { budgetRepo.deleteBudget("b1") }
    }

    @Test
    fun `setMonth changes monthKey and triggers re-query`() = runTest {
        every { budgetRepo.observeBudgetsForMonth("2026-04") } returns flowOf(listOf(sample))
        every { budgetRepo.observeBudgetsForMonth("2026-03") } returns flowOf(emptyList())
        every { preferences.currencyCode } returns flowOf("INR")

        val vm = BudgetsViewModel(budgetRepo, preferences)
        vm.setMonth("2026-03")
        advanceUntilIdle()

        assertEquals("2026-03", vm.state.value.monthKey)
    }
}
