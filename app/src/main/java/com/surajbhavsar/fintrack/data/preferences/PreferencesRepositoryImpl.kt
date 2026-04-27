package com.surajbhavsar.fintrack.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    override val currencyCode: Flow<String> = dataStore.data
        .map { it[KEY_CURRENCY] ?: "INR" }

    override val darkTheme: Flow<Boolean?> = dataStore.data
        .map { it[KEY_DARK_THEME] }

    override val lastSyncedAt: Flow<Long> = dataStore.data
        .map { it[KEY_LAST_SYNCED_AT] ?: 0L }

    override suspend fun setCurrencyCode(code: String) {
        dataStore.edit { it[KEY_CURRENCY] = code }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    override suspend fun clearDarkTheme() {
        dataStore.edit { it.remove(KEY_DARK_THEME) }
    }

    override suspend fun setLastSyncedAt(epochMillis: Long) {
        dataStore.edit { it[KEY_LAST_SYNCED_AT] = epochMillis }
    }

    private companion object {
        val KEY_CURRENCY = stringPreferencesKey("currency_code")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
    }
}
