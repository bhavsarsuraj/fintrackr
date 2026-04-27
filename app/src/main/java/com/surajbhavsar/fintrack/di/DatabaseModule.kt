package com.surajbhavsar.fintrack.di

import android.content.Context
import androidx.room.Room
import com.surajbhavsar.fintrack.data.local.FinTrackrDatabase
import com.surajbhavsar.fintrack.data.local.dao.BudgetDao
import com.surajbhavsar.fintrack.data.local.dao.ExpenseDao
import com.surajbhavsar.fintrack.data.local.dao.RecurringRuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinTrackrDatabase =
        Room.databaseBuilder(context, FinTrackrDatabase::class.java, FinTrackrDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExpenseDao(db: FinTrackrDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideBudgetDao(db: FinTrackrDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideRecurringRuleDao(db: FinTrackrDatabase): RecurringRuleDao = db.recurringRuleDao()
}
