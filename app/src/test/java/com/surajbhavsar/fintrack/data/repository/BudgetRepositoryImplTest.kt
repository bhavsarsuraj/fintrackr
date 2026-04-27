package com.surajbhavsar.fintrack.data.repository

import app.cash.turbine.test
import com.surajbhavsar.fintrack.data.local.dao.BudgetDao
import com.surajbhavsar.fintrack.data.local.entity.BudgetEntity
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetRepositoryImplTest {

    private val dao: BudgetDao = mockk()
    private val repo = BudgetRepositoryImpl(dao, Dispatchers.Unconfined)

    @Test
    fun `observeBudgetsForMonth maps entities to domain`() = runTest {
        val entities = listOf(
            BudgetEntity(id = "b1", category = "FOOD", monthKey = "2026-04", limitMinor = 1000),
        )
        every { dao.observeByMonth("2026-04") } returns flowOf(entities)

        repo.observeBudgetsForMonth("2026-04").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(Category.FOOD, list[0].category)
            awaitComplete()
        }
    }

    @Test
    fun `upsertBudget delegates to dao`() = runTest {
        val captured = slot<BudgetEntity>()
        coEvery { dao.upsert(capture(captured)) } returns Unit

        repo.upsertBudget(
            Budget(id = "b1", category = Category.HEALTH, monthKey = "2026-04", limitMinor = 500),
        )

        assertEquals("b1", captured.captured.id)
        assertEquals("HEALTH", captured.captured.category)
    }

    @Test
    fun `deleteBudget delegates to dao`() = runTest {
        coEvery { dao.delete("b1") } returns Unit
        repo.deleteBudget("b1")
        coVerify { dao.delete("b1") }
    }
}
