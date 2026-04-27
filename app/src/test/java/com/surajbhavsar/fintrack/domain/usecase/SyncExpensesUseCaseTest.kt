package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncExpensesUseCaseTest {

    private val expenseRepo: ExpenseRepository = mockk(relaxed = true)
    private val preferencesRepo: PreferencesRepository = mockk(relaxed = true)
    private val useCase = SyncExpensesUseCase(expenseRepo, preferencesRepo)

    @Test
    fun `pushes then pulls and updates cursor when newer data found`() = runTest {
        coEvery { expenseRepo.syncWithRemote() } returns Unit
        coEvery { preferencesRepo.lastSyncedAt } returns flowOf(100L)
        coEvery { expenseRepo.pullFromRemote(100L) } returns 500L

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerifyOrder {
            expenseRepo.syncWithRemote()
            expenseRepo.pullFromRemote(100L)
            preferencesRepo.setLastSyncedAt(500L)
        }
    }

    @Test
    fun `does not update cursor when pull returns same cursor`() = runTest {
        coEvery { expenseRepo.syncWithRemote() } returns Unit
        coEvery { preferencesRepo.lastSyncedAt } returns flowOf(200L)
        coEvery { expenseRepo.pullFromRemote(200L) } returns 200L

        useCase()

        coVerify(exactly = 0) { preferencesRepo.setLastSyncedAt(any()) }
    }

    @Test
    fun `propagates failure when push throws`() = runTest {
        coEvery { expenseRepo.syncWithRemote() } throws RuntimeException("network down")
        coEvery { preferencesRepo.lastSyncedAt } returns flowOf(0L)

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
