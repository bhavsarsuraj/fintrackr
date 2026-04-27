package com.surajbhavsar.fintrack.data.repository

import com.surajbhavsar.fintrack.core.common.IoDispatcher
import com.surajbhavsar.fintrack.data.local.dao.RecurringRuleDao
import com.surajbhavsar.fintrack.data.mapper.toDomain
import com.surajbhavsar.fintrack.data.mapper.toEntity
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringRuleRepositoryImpl @Inject constructor(
    private val dao: RecurringRuleDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : RecurringRuleRepository {

    override fun observeActiveRules(): Flow<List<RecurringRule>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun rulesDueAt(epochMillis: Long): List<RecurringRule> = withContext(io) {
        dao.dueAt(epochMillis).map { it.toDomain() }
    }

    override suspend fun upsert(rule: RecurringRule) = withContext(io) {
        dao.upsert(rule.toEntity())
    }

    override suspend fun delete(id: String) = withContext(io) {
        dao.delete(id)
    }
}
