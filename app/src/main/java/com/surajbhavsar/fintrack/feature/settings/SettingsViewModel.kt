package com.surajbhavsar.fintrack.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.AuthState
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.work.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val syncScheduler: SyncScheduler,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _currency = MutableStateFlow("INR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    val lastSyncedAt: StateFlow<Long> = preferences.lastSyncedAt
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val userEmail: StateFlow<String?> = authRepository.authState
        .map { state -> if (state is AuthState.SignedIn) state.email else null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            preferences.currencyCode.collect { _currency.value = it }
        }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch { preferences.setCurrencyCode(code) }
    }

    fun syncNow() {
        syncScheduler.runOnce()
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
