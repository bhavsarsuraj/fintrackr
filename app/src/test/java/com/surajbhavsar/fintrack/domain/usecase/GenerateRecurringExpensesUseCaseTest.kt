package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class GenerateRecurringExpensesUseCaseTest {

    private val expenseRepo: ExpenseRepository = mockk(relaxed = true)
    private val ruleRepo: RecurringRuleRepository = mockk(relaxed = true)
    private val useCase = GenerateRecurringExpensesUseCase(expenseRepo, ruleRepo)

    private fun monthlyRule(nextRunAt: Long) = RecurringRule(
        id = "r1",
        title = "Rent",
        amountMinor = 25_000_00,
        category = Category.BILLS,
        frequency = Frequency.MONTHLY,
        nextRunAt = nextRunAt,
        active = true,
    )

    @Test
    fun `does nothing when no rules are due`() = runTest {
        coEvery { ruleRepo.rulesDueAt(any()) } returns emptyList()

        val generated = useCase(now = 1_700_000_000_000)

        assertEquals(0, generated)
        coVerify(exactly = 0) { expenseRepo.addExpense(any()) }
        coVerify(exactly = 0) { ruleRepo.upsert(any()) }
    }

    @Test
    fun `creates expense and advances rule by one month`() = runTest {
        val nextRun = 1_700_000_000_000L
        val rule = monthlyRule(nextRun)
        coEvery { ruleRepo.rulesDueAt(any()) } returns listOf(rule)

        val createdExpense = slot<Expense>()
        val updatedRule = slot<RecurringRule>()
        coEvery { expenseRepo.addExpense(capture(createdExpense)) } returns Unit
        coEvery { ruleRepo.upsert(capture(updatedRule)) } returns Unit

        val generated = useCase(now = nextRun + 1)

        assertEquals(1, generated)
        assertEquals("Rent", createdExpense.captured.title)
        assertEquals(rule.amountMinor, createdExpense.captured.amountMinor)
        assertEquals(rule.id, createdExpense.captured.recurringRuleId)
        assertEquals(nextRun, createdExpense.captured.occurredAt)

        val expectedNext = Calendar.getInstance().apply {
            timeInMillis = nextRun
            add(Calendar.MONTH, 1)
        }.timeInMillis
        assertEquals(expectedNext, updatedRule.captured.nextRunAt)
        assertTrue(updatedRule.captured.active)
    }
}
