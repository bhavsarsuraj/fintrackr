package com.surajbhavsar.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountMinor: Long,
    val category: String,
    val note: String?,
    val occurredAt: Long,
    val monthKey: String,
    val recurringRuleId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingSync: Boolean = true,
    val deleted: Boolean = false,
)
