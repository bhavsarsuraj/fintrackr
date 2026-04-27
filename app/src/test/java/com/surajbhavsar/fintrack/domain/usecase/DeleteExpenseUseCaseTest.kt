package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteExpenseUseCaseTest {

    private val repository: ExpenseRepository = mockk(relaxed = true)
    private val useCase = DeleteExpenseUseCase(repository)

    @Test
    fun `delegates to repository and returns success`() = runTest {
        coEvery { repository.deleteExpense("abc") } returns Unit
        val result = useCase("abc")
        assertTrue(result.isSuccess)
        coVerify { repository.deleteExpense("abc") }
    }

    @Test
    fun `wraps thrown exception as failure`() = runTest {
        coEvery { repository.deleteExpense("abc") } throws RuntimeException("boom")
        val result = useCase("abc")
        assertTrue(result.isFailure)
    }
}
