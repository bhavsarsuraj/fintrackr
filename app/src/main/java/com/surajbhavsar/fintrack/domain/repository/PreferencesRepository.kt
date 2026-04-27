package com.surajbhavsar.fintrack.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val currencyCode: Flow<String>
    val darkTheme: Flow<Boolean?>
    val lastSyncedAt: Flow<Long>
    suspend fun setCurrencyCode(code: String)
    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun clearDarkTheme()
    suspend fun setLastSyncedAt(epochMillis: Long)
}
