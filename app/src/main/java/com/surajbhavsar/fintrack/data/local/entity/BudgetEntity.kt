package com.surajbhavsar.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val monthKey: String,
    val limitMinor: Long,
    val updatedAt: Long = System.currentTimeMillis(),
)
