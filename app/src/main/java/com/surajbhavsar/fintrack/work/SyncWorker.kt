package com.surajbhavsar.fintrack.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.surajbhavsar.fintrack.domain.usecase.SyncExpensesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncExpenses: SyncExpensesUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        syncExpenses().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
}
