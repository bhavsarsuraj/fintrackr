package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class GenerateRecurringExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val ruleRepository: RecurringRuleRepository,
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()): Int {
        val due = ruleRepository.rulesDueAt(now)
        due.forEach { rule ->
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                title = rule.title,
                amountMinor = rule.amountMinor,
                category = rule.category,
                note = "Auto-generated",
                occurredAt = rule.nextRunAt,
                recurringRuleId = rule.id,
            )
            expenseRepository.addExpense(expense)
            ruleRepository.upsert(rule.copy(nextRunAt = nextRun(rule)))
        }
        return due.size
    }

    private fun nextRun(rule: RecurringRule): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = rule.nextRunAt }
        when (rule.frequency) {
            Frequency.DAILY -> cal.add(Calendar.DAY_OF_MONTH, 1)
            Frequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            Frequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }
}
