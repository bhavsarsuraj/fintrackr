package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddExpenseUseCaseTest {

    private val repository: ExpenseRepository = mockk(relaxed = true)
    private val useCase = AddExpenseUseCase(repository)

    @Test
    fun `fails when title is blank`() = runTest {
        val result = useCase(
            AddExpenseUseCase.Input(
                title = "  ",
                amountMinor = 100,
                category = Category.FOOD,
                note = null,
                occurredAt = 0L,
            )
        )
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.addExpense(any()) }
    }

    @Test
    fun `fails when amount is non-positive`() = runTest {
        val result = useCase(
            AddExpenseUseCase.Input(
                title = "Lunch",
                amountMinor = 0,
                category = Category.FOOD,
                note = null,
                occurredAt = 0L,
            )
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `persists expense with trimmed title and null note when note is blank`() = runTest {
        val captured = slot<Expense>()
        coEvery { repository.addExpense(capture(captured)) } returns Unit

        val result = useCase(
            AddExpenseUseCase.Input(
                title = "  Coffee  ",
                amountMinor = 4500,
                category = Category.FOOD,
                note = "   ",
                occurredAt = 1_700_000_000_000,
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("Coffee", captured.captured.title)
        assertEquals(null, captured.captured.note)
        assertEquals(Category.FOOD, captured.captured.category)
        assertEquals(4500L, captured.captured.amountMinor)
    }
}
