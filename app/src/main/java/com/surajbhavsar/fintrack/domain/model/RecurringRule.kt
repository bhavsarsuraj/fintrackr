package com.surajbhavsar.fintrack.domain.model

data class RecurringRule(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val category: Category,
    val frequency: Frequency,
    val nextRunAt: Long,
    val active: Boolean = true,
)

enum class Frequency { DAILY, WEEKLY, MONTHLY }
