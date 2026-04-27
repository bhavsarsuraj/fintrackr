package com.surajbhavsar.fintrack.feature.budgets

import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val monthKey: String = "",
    val budgets: List<Budget> = emptyList(),
    val currencyCode: String = "INR",
    val editor: BudgetEditor? = null,
    val errorMessage: String? = null,
)

data class BudgetEditor(
    val existingId: String? = null,
    val category: Category = Category.FOOD,
    val amount: String = "",
)
