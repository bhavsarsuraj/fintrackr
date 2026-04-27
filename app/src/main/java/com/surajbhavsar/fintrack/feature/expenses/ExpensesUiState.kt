package com.surajbhavsar.fintrack.feature.expenses

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense

data class ExpensesUiState(
    val isLoading: Boolean = true,
    val monthKey: String = "",
    val categoryFilter: Category? = null,
    val expenses: List<Expense> = emptyList(),
    val totalMinor: Long = 0,
    val currencyCode: String = "INR",
    val errorMessage: String? = null,
)
