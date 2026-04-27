package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.core.common.toMonthKey
import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    title = title,
    amountMinor = amountMinor,
    category = Category.fromName(category),
    note = note,
    occurredAt = occurredAt,
    recurringRuleId = recurringRuleId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Expense.toEntity(pendingSync: Boolean = true, deleted: Boolean = false): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    amountMinor = amountMinor,
    category = category.name,
    note = note,
    occurredAt = occurredAt,
    monthKey = occurredAt.toMonthKey(),
    recurringRuleId = recurringRuleId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pendingSync = pendingSync,
    deleted = deleted,
)
