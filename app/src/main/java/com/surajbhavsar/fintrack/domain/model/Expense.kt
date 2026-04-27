package com.surajbhavsar.fintrack.domain.model

data class Expense(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val category: Category,
    val note: String? = null,
    val occurredAt: Long,
    val recurringRuleId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)
