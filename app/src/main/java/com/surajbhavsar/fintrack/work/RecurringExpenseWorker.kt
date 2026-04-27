package com.surajbhavsar.fintrack.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.surajbhavsar.fintrack.domain.usecase.GenerateRecurringExpensesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurringExpenseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val generateRecurring: GenerateRecurringExpensesUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        generateRecurring()
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }
}
