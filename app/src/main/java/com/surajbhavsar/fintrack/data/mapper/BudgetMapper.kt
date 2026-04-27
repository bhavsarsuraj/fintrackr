package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.data.local.entity.BudgetEntity
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    category = Category.fromName(category),
    monthKey = monthKey,
    limitMinor = limitMinor,
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    category = category.name,
    monthKey = monthKey,
    limitMinor = limitMinor,
)
