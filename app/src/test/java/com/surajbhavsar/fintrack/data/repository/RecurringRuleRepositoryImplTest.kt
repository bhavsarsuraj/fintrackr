package com.surajbhavsar.fintrack.data.repository

import app.cash.turbine.test
import com.surajbhavsar.fintrack.data.local.dao.RecurringRuleDao
import com.surajbhavsar.fintrack.data.local.entity.RecurringRuleEntity
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringRuleRepositoryImplTest {

    private val dao: RecurringRuleDao = mockk()
    private val repo = RecurringRuleRepositoryImpl(dao, Dispatchers.Unconfined)

    @Test
    fun `observeActiveRules maps entities`() = runTest {
        val entity = RecurringRuleEntity(
            id = "r1", title = "Rent", amountMinor = 1000,
            category = "BILLS", frequency = "MONTHLY",
            nextRunAt = 0L, active = true,
        )
        every { dao.observeActive() } returns flowOf(listOf(entity))

        repo.observeActiveRules().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(Frequency.MONTHLY, list[0].frequency)
            awaitComplete()
        }
    }

    @Test
    fun `rulesDueAt delegates to dao with timestamp`() = runTest {
        coEvery { dao.dueAt(123L) } returns emptyList()
        repo.rulesDueAt(123L)
        coVerify { dao.dueAt(123L) }
    }

    @Test
    fun `upsert and delete delegate to dao`() = runTest {
        coEvery { dao.upsert(any()) } returns Unit
        coEvery { dao.delete("r1") } returns Unit

        repo.upsert(
            RecurringRule(
                id = "r1", title = "X", amountMinor = 100,
                category = Category.OTHER, frequency = Frequency.DAILY,
                nextRunAt = 0L, active = true,
            )
        )
        repo.delete("r1")

        coVerify { dao.upsert(any()) }
        coVerify { dao.delete("r1") }
    }
}
