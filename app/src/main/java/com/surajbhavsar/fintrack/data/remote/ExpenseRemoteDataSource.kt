package com.surajbhavsar.fintrack.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun userCollection(userId: String) =
        firestore.collection("users").document(userId).collection("expenses")

    suspend fun pushExpense(userId: String, entity: ExpenseEntity) {
        userCollection(userId).document(entity.id).set(entity.toMap()).await()
    }

    suspend fun deleteExpense(userId: String, id: String) {
        userCollection(userId).document(id).delete().await()
    }

    suspend fun fetchSince(userId: String, sinceMillis: Long): List<ExpenseEntity> {
        val snap = userCollection(userId)
            .whereGreaterThan("updatedAt", sinceMillis)
            .get()
            .await()
        return snap.documents.mapNotNull { it.data?.toExpenseEntity() }
    }
}

private fun ExpenseEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "amountMinor" to amountMinor,
    "category" to category,
    "note" to note,
    "occurredAt" to occurredAt,
    "monthKey" to monthKey,
    "recurringRuleId" to recurringRuleId,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "deleted" to deleted,
)

private fun Map<String, Any?>.toExpenseEntity(): ExpenseEntity? {
    val id = this["id"] as? String ?: return null
    return ExpenseEntity(
        id = id,
        title = this["title"] as? String ?: "",
        amountMinor = (this["amountMinor"] as? Number)?.toLong() ?: 0L,
        category = this["category"] as? String ?: "OTHER",
        note = this["note"] as? String,
        occurredAt = (this["occurredAt"] as? Number)?.toLong() ?: 0L,
        monthKey = this["monthKey"] as? String ?: "",
        recurringRuleId = this["recurringRuleId"] as? String,
        createdAt = (this["createdAt"] as? Number)?.toLong() ?: 0L,
        updatedAt = (this["updatedAt"] as? Number)?.toLong() ?: 0L,
        pendingSync = false,
        deleted = this["deleted"] as? Boolean ?: false,
    )
}
