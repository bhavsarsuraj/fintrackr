package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        expenseRepository.syncWithRemote()
        val since = preferencesRepository.lastSyncedAt.first()
        val newCursor = expenseRepository.pullFromRemote(since)
        if (newCursor > since) preferencesRepository.setLastSyncedAt(newCursor)
    }
}
