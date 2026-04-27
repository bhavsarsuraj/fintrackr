package com.surajbhavsar.fintrack

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.surajbhavsar.fintrack.work.RecurringExpenseScheduler
import com.surajbhavsar.fintrack.work.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FinTrackrApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var recurringExpenseScheduler: RecurringExpenseScheduler
    @Inject lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        recurringExpenseScheduler.schedule()
        syncScheduler.schedulePeriodic()
    }
}
