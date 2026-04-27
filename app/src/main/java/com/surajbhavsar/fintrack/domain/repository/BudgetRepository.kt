package com.surajbhavsar.fintrack.domain.repository

import com.surajbhavsar.fintrack.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgetsForMonth(monthKey: String): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
}
