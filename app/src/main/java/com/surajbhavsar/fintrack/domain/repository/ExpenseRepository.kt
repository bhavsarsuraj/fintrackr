package com.surajbhavsar.fintrack.domain.repository

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    fun observeExpensesForMonth(monthKey: String): Flow<List<Expense>>
    fun observeTotalsByCategoryForMonth(monthKey: String): Flow<Map<Category, Long>>
    suspend fun getById(id: String): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
    suspend fun syncWithRemote()
    suspend fun pullFromRemote(sinceMillis: Long): Long
}
