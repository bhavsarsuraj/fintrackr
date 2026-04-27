package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import java.util.UUID
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(input: Input): Result<Unit> {
        if (input.title.isBlank()) return Result.failure(IllegalArgumentException("Title is required"))
        if (input.amountMinor <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        val expense = Expense(
            id = input.id ?: UUID.randomUUID().toString(),
            title = input.title.trim(),
            amountMinor = input.amountMinor,
            category = input.category,
            note = input.note?.takeIf { it.isNotBlank() }?.trim(),
            occurredAt = input.occurredAt,
        )
        return runCatching { repository.addExpense(expense) }
    }

    data class Input(
        val id: String? = null,
        val title: String,
        val amountMinor: Long,
        val category: com.surajbhavsar.fintrack.domain.model.Category,
        val note: String?,
        val occurredAt: Long,
    )
}
