package com.surajbhavsar.fintrack.data.repository

import com.surajbhavsar.fintrack.data.local.dao.ExpenseDao
import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import com.surajbhavsar.fintrack.data.remote.ExpenseRemoteDataSource
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseRepositoryImplTest {

    private val dao: ExpenseDao = mockk(relaxed = true)
    private val remote: ExpenseRemoteDataSource = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val repo = ExpenseRepositoryImpl(dao, remote, authRepository, Dispatchers.Unconfined)

    private fun localEntity(
        id: String,
        updatedAt: Long,
        deleted: Boolean = false,
        pendingSync: Boolean = true,
    ) = ExpenseEntity(
        id = id, title = "T-$id", amountMinor = 100L, category = "OTHER",
        note = null, occurredAt = 0L, monthKey = "2026-04",
        recurringRuleId = null, createdAt = 0L, updatedAt = updatedAt,
        pendingSync = pendingSync, deleted = deleted,
    )

    @Test
    fun `addExpense writes locally with pendingSync flag`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf(null) // no sync
        val captured = slot<ExpenseEntity>()
        coEvery { dao.upsert(capture(captured)) } returns Unit

        val expense = Expense(
            id = "id-1", title = "Coffee", amountMinor = 100,
            category = Category.FOOD, note = null, occurredAt = 0L,
        )
        repo.addExpense(expense)

        assertEquals("id-1", captured.captured.id)
        assertTrue(captured.captured.pendingSync)
    }

    @Test
    fun `syncWithRemote pushes pending and marks them synced`() = runTest {
        val pending = listOf(localEntity("a", 1L), localEntity("b", 2L))
        coEvery { authRepository.currentUserId } returns flowOf("user-1")
        coEvery { dao.pendingSync() } returns pending

        repo.syncWithRemote()

        coVerify { remote.pushExpense("user-1", pending[0]) }
        coVerify { remote.pushExpense("user-1", pending[1]) }
        coVerify { dao.markSynced("a") }
        coVerify { dao.markSynced("b") }
    }

    @Test
    fun `syncWithRemote sends delete for soft-deleted entities`() = runTest {
        val deleted = localEntity("d", 5L, deleted = true)
        coEvery { authRepository.currentUserId } returns flowOf("user-1")
        coEvery { dao.pendingSync() } returns listOf(deleted)

        repo.syncWithRemote()

        coVerify { remote.deleteExpense("user-1", "d") }
        coVerify(exactly = 0) { remote.pushExpense(any(), any()) }
        coVerify { dao.markSynced("d") }
    }

    @Test
    fun `syncWithRemote no-ops when not signed in`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf(null)

        repo.syncWithRemote()

        coVerify(exactly = 0) { dao.pendingSync() }
        coVerify(exactly = 0) { remote.pushExpense(any(), any()) }
    }

    @Test
    fun `pullFromRemote upserts new and remote-newer rows, skips local-newer`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf("user-1")

        val remoteRows = listOf(
            localEntity("new-1", updatedAt = 200L, pendingSync = false),
            localEntity("conflict", updatedAt = 300L, pendingSync = false), // remote newer
            localEntity("conflict-old", updatedAt = 50L, pendingSync = false), // local newer
        )
        coEvery { remote.fetchSince("user-1", 0L) } returns remoteRows
        coEvery { dao.getById("new-1") } returns null
        coEvery { dao.getById("conflict") } returns localEntity("conflict", updatedAt = 100L, pendingSync = false)
        coEvery { dao.getById("conflict-old") } returns localEntity("conflict-old", updatedAt = 100L, pendingSync = false)

        val merged = slot<List<ExpenseEntity>>()
        coEvery { dao.upsertAll(capture(merged)) } returns Unit

        val newCursor = repo.pullFromRemote(0L)

        // Cursor advances to max remote updatedAt
        assertEquals(300L, newCursor)
        // Only "new-1" and "conflict" should be merged in
        val mergedIds = merged.captured.map { it.id }.toSet()
        assertTrue("new-1" in mergedIds)
        assertTrue("conflict" in mergedIds)
        assertTrue("conflict-old" !in mergedIds)
        // Merged rows must have pendingSync = false (synced from remote)
        assertTrue(merged.captured.all { !it.pendingSync })
    }

    @Test
    fun `pullFromRemote returns same cursor when remote has nothing`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf("user-1")
        coEvery { remote.fetchSince("user-1", 100L) } returns emptyList()

        val cursor = repo.pullFromRemote(100L)

        assertEquals(100L, cursor)
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `pullFromRemote no-ops when not signed in`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf(null)

        val cursor = repo.pullFromRemote(99L)
        assertEquals(99L, cursor)
        coVerify(exactly = 0) { remote.fetchSince(any(), any()) }
    }

    @Test
    fun `deleteExpense soft-deletes locally`() = runTest {
        coEvery { authRepository.currentUserId } returns flowOf(null)
        repo.deleteExpense("xyz")
        coVerify { dao.softDelete("xyz", any()) }
    }
}
