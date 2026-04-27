package com.surajbhavsar.fintrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE deleted = 0 ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE deleted = 0 AND monthKey = :monthKey ORDER BY occurredAt DESC")
    fun observeByMonth(monthKey: String): Flow<List<ExpenseEntity>>

    @Query("SELECT category, SUM(amountMinor) AS total FROM expenses WHERE deleted = 0 AND monthKey = :monthKey GROUP BY category")
    fun observeTotalsByCategory(monthKey: String): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE pendingSync = 1")
    suspend fun pendingSync(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ExpenseEntity>)

    @Update
    suspend fun update(entity: ExpenseEntity)

    @Query("UPDATE expenses SET deleted = 1, pendingSync = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("UPDATE expenses SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)
}

data class CategoryTotal(val category: String, val total: Long)
