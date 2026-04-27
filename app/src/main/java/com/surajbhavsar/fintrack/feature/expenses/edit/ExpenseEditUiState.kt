package com.surajbhavsar.fintrack.feature.expenses.edit

import com.surajbhavsar.fintrack.domain.model.Category

data class ExpenseEditUiState(
    val id: String? = null,
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val category: Category = Category.OTHER,
    val occurredAt: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)
