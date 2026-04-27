package com.surajbhavsar.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountMinor: Long,
    val category: String,
    val frequency: String,
    val nextRunAt: Long,
    val active: Boolean,
)
