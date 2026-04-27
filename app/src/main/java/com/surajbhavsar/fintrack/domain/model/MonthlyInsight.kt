package com.surajbhavsar.fintrack.domain.model

data class MonthlyInsight(
    val monthKey: String,
    val totalMinor: Long,
    val byCategory: Map<Category, Long>,
    val budgetUsage: List<BudgetUsage>,
)

data class BudgetUsage(
    val category: Category,
    val limitMinor: Long,
    val spentMinor: Long,
) {
    val percentUsed: Float = if (limitMinor == 0L) 0f else (spentMinor.toFloat() / limitMinor)
}
