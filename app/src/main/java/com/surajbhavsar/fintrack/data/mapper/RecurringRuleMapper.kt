package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.data.local.entity.RecurringRuleEntity
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule

fun RecurringRuleEntity.toDomain(): RecurringRule = RecurringRule(
    id = id,
    title = title,
    amountMinor = amountMinor,
    category = Category.fromName(category),
    frequency = runCatching { Frequency.valueOf(frequency) }.getOrDefault(Frequency.MONTHLY),
    nextRunAt = nextRunAt,
    active = active,
)

fun RecurringRule.toEntity(): RecurringRuleEntity = RecurringRuleEntity(
    id = id,
    title = title,
    amountMinor = amountMinor,
    category = category.name,
    frequency = frequency.name,
    nextRunAt = nextRunAt,
    active = active,
)
