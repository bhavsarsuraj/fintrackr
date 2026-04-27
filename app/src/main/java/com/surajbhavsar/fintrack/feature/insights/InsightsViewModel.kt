package com.surajbhavsar.fintrack.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.core.common.toMonthKey
import com.surajbhavsar.fintrack.core.ui.UiState
import com.surajbhavsar.fintrack.domain.model.MonthlyInsight
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.usecase.GetMonthlyInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getMonthlyInsights: GetMonthlyInsightsUseCase,
    preferences: PreferencesRepository,
) : ViewModel() {

    private val initialMonth = System.currentTimeMillis().toMonthKey()
    private val _monthKey = MutableStateFlow(initialMonth)
    val monthKey: StateFlow<String> = _monthKey.asStateFlow()

    private val _state = MutableStateFlow<UiState<MonthlyInsight>>(UiState.Loading)
    val state: StateFlow<UiState<MonthlyInsight>> = _state.asStateFlow()

    val currency: StateFlow<String> = preferences.currencyCode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "INR")

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            _monthKey.flatMapLatest { month -> getMonthlyInsights(month) }
                .collect { _state.value = UiState.Success(it) }
        }
    }

    fun setMonth(month: String) { _monthKey.value = month }
}
