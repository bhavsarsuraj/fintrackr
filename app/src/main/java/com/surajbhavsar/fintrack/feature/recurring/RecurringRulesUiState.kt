package com.surajbhavsar.fintrack.feature.recurring

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule

data class RecurringRulesUiState(
    val isLoading: Boolean = true,
    val rules: List<RecurringRule> = emptyList(),
    val currencyCode: String = "INR",
    val editor: RuleEditor? = null,
    val errorMessage: String? = null,
)

data class RuleEditor(
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.BILLS,
    val frequency: Frequency = Frequency.MONTHLY,
    val nextRunAt: Long = System.currentTimeMillis(),
)
