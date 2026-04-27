package com.surajbhavsar.fintrack.data.repository

import com.surajbhavsar.fintrack.core.common.IoDispatcher
import com.surajbhavsar.fintrack.data.local.dao.BudgetDao
import com.surajbhavsar.fintrack.data.mapper.toDomain
import com.surajbhavsar.fintrack.data.mapper.toEntity
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : BudgetRepository {

    override fun observeBudgetsForMonth(monthKey: String): Flow<List<Budget>> =
        dao.observeByMonth(monthKey).map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun upsertBudget(budget: Budget) = withContext(io) {
        dao.upsert(budget.toEntity())
    }

    override suspend fun deleteBudget(id: String) = withContext(io) {
        dao.delete(id)
    }
}
