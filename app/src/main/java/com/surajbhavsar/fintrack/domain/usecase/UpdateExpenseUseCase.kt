package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> = runCatching {
        require(expense.title.isNotBlank()) { "Title is required" }
        require(expense.amountMinor > 0) { "Amount must be greater than zero" }
        repository.updateExpense(expense.copy(updatedAt = System.currentTimeMillis()))
    }
}
