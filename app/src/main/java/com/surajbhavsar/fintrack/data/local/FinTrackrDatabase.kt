package com.surajbhavsar.fintrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surajbhavsar.fintrack.data.local.dao.BudgetDao
import com.surajbhavsar.fintrack.data.local.dao.ExpenseDao
import com.surajbhavsar.fintrack.data.local.dao.RecurringRuleDao
import com.surajbhavsar.fintrack.data.local.entity.BudgetEntity
import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import com.surajbhavsar.fintrack.data.local.entity.RecurringRuleEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FinTrackrDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        const val DB_NAME = "fintrackr.db"
    }
}
