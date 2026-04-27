package com.surajbhavsar.fintrack.data.repository

import com.surajbhavsar.fintrack.core.common.IoDispatcher
import com.surajbhavsar.fintrack.data.local.dao.ExpenseDao
import com.surajbhavsar.fintrack.data.mapper.toDomain
import com.surajbhavsar.fintrack.data.mapper.toEntity
import com.surajbhavsar.fintrack.data.remote.ExpenseRemoteDataSource
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
    private val remote: ExpenseRemoteDataSource,
    private val authRepository: AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ExpenseRepository {

    override fun observeExpenses(): Flow<List<Expense>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeExpensesForMonth(monthKey: String): Flow<List<Expense>> =
        dao.observeByMonth(monthKey).map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeTotalsByCategoryForMonth(monthKey: String): Flow<Map<Category, Long>> =
        dao.observeTotalsByCategory(monthKey)
            .map { rows -> rows.associate { Category.fromName(it.category) to it.total } }
            .flowOn(io)

    override suspend fun getById(id: String): Expense? = dao.getById(id)?.toDomain()

    override suspend fun addExpense(expense: Expense) {
        dao.upsert(expense.toEntity(pendingSync = true))
        runCatching { syncWithRemote() }
    }

    override suspend fun updateExpense(expense: Expense) {
        dao.upsert(expense.toEntity(pendingSync = true))
        runCatching { syncWithRemote() }
    }

    override suspend fun deleteExpense(id: String) {
        dao.softDelete(id, System.currentTimeMillis())
        runCatching { syncWithRemote() }
    }

    override suspend fun syncWithRemote() {
        val userId = authRepository.currentUserId.first() ?: return
        dao.pendingSync().forEach { entity ->
            if (entity.deleted) {
                remote.deleteExpense(userId, entity.id)
            } else {
                remote.pushExpense(userId, entity)
            }
            dao.markSynced(entity.id)
        }
    }

    override suspend fun pullFromRemote(sinceMillis: Long): Long {
        val userId = authRepository.currentUserId.first() ?: return sinceMillis
        val remoteEntities = remote.fetchSince(userId, sinceMillis)
        if (remoteEntities.isEmpty()) return sinceMillis

        val merged = remoteEntities.mapNotNull { incoming ->
            val local = dao.getById(incoming.id)
            when {
                local == null -> incoming.copy(pendingSync = false)
                incoming.updatedAt > local.updatedAt -> incoming.copy(pendingSync = false)
                else -> null
            }
        }
        if (merged.isNotEmpty()) dao.upsertAll(merged)
        return remoteEntities.maxOf { it.updatedAt }
    }
}
