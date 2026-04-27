package com.surajbhavsar.fintrack.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.core.common.toMonthKey
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.usecase.DeleteExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    preferences: PreferencesRepository,
    private val deleteExpense: DeleteExpenseUseCase,
) : ViewModel() {

    private val initialMonth = System.currentTimeMillis().toMonthKey()

    private val monthKey = MutableStateFlow(initialMonth)
    private val categoryFilter = MutableStateFlow<Category?>(null)

    private val _state = MutableStateFlow(ExpensesUiState(monthKey = initialMonth))
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expensesStream = monthKey.flatMapLatest { month ->
        expenseRepository.observeExpensesForMonth(month)
    }

    init {
        viewModelScope.launch {
            combine(
                expensesStream,
                categoryFilter,
                monthKey,
                preferences.currencyCode,
            ) { allExpenses, filter, month, currency ->
                val filtered = if (filter == null) allExpenses
                else allExpenses.filter { it.category == filter }
                ExpensesUiState(
                    isLoading = false,
                    monthKey = month,
                    categoryFilter = filter,
                    expenses = filtered,
                    totalMinor = filtered.sumOf { it.amountMinor },
                    currencyCode = currency,
                )
            }.collect { _state.value = it }
        }
    }

    fun setMonth(month: String) { monthKey.value = month }
    fun setCategoryFilter(category: Category?) { categoryFilter.value = category }

    fun onDelete(id: String) {
        viewModelScope.launch {
            deleteExpense(id).onFailure { t ->
                _state.update { it.copy(errorMessage = t.message) }
            }
        }
    }

    fun onErrorShown() = _state.update { it.copy(errorMessage = null) }
}
