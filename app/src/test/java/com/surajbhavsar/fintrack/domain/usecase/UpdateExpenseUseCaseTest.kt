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

class UpdateExpenseUseCaseTest {

    private val repository: ExpenseRepository = mockk(relaxed = true)
    private val useCase = UpdateExpenseUseCase(repository)

    private fun expense(title: String = "Lunch", amount: Long = 100L) = Expense(
        id = "id-1",
        title = title,
        amountMinor = amount,
        category = Category.FOOD,
        note = null,
        occurredAt = 0,
        createdAt = 1L,
        updatedAt = 1L,
    )

    @Test
    fun `fails when title is blank`() = runTest {
        val result = useCase(expense(title = "  "))
        assertTrue(result.isFailure)
    }

    @Test
    fun `fails when amount is non-positive`() = runTest {
        val result = useCase(expense(amount = 0L))
        assertTrue(result.isFailure)
    }

    @Test
    fun `bumps updatedAt and persists`() = runTest {
        val captured = slot<Expense>()
        coEvery { repository.updateExpense(capture(captured)) } returns Unit

        val original = expense()
        val before = original.updatedAt
        val result = useCase(original)

        assertTrue(result.isSuccess)
        assertEquals(original.id, captured.captured.id)
        assertTrue("updatedAt should be bumped", captured.captured.updatedAt > before)
        coVerify(exactly = 1) { repository.updateExpense(any()) }
    }
}
