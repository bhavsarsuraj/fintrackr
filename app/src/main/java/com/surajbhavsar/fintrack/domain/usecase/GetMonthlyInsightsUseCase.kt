package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.model.BudgetUsage
import com.surajbhavsar.fintrack.domain.model.MonthlyInsight
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetMonthlyInsightsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
) {
    operator fun invoke(monthKey: String): Flow<MonthlyInsight> =
        combine(
            expenseRepository.observeTotalsByCategoryForMonth(monthKey),
            budgetRepository.observeBudgetsForMonth(monthKey),
        ) { totalsByCategory, budgets ->
            val total = totalsByCategory.values.sum()
            val usage = budgets.map { b ->
                BudgetUsage(
                    category = b.category,
                    limitMinor = b.limitMinor,
                    spentMinor = totalsByCategory[b.category] ?: 0L,
                )
            }
            MonthlyInsight(monthKey, total, totalsByCategory, usage)
        }
}
