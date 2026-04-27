package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    operator fun invoke(monthKey: String? = null): Flow<List<Expense>> =
        if (monthKey == null) repository.observeExpenses()
        else repository.observeExpensesForMonth(monthKey)
}
