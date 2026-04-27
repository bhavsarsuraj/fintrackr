package com.surajbhavsar.fintrack.domain.model

data class Budget(
    val id: String,
    val category: Category,
    val monthKey: String,
    val limitMinor: Long,
)
