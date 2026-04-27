package com.surajbhavsar.fintrack.domain.repository

import com.surajbhavsar.fintrack.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun observeActiveRules(): Flow<List<RecurringRule>>
    suspend fun rulesDueAt(epochMillis: Long): List<RecurringRule>
    suspend fun upsert(rule: RecurringRule)
    suspend fun delete(id: String)
}
