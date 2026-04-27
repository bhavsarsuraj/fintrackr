package com.surajbhavsar.fintrack.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.core.common.toMinorUnits
import com.surajbhavsar.fintrack.core.common.toMonthKey
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    preferences: PreferencesRepository,
) : ViewModel() {

    private val initialMonth = System.currentTimeMillis().toMonthKey()
    private val monthKeyFlow = MutableStateFlow(initialMonth)

    private val _state = MutableStateFlow(BudgetsUiState(monthKey = initialMonth))
    val state: StateFlow<BudgetsUiState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val budgetStream = monthKeyFlow.flatMapLatest { month ->
        budgetRepository.observeBudgetsForMonth(month)
    }

    init {
        viewModelScope.launch {
            combine(
                budgetStream,
                monthKeyFlow,
                preferences.currencyCode,
            ) { budgets, month, currency ->
                _state.value.copy(
                    isLoading = false,
                    monthKey = month,
                    budgets = budgets,
                    currencyCode = currency,
                )
            }.collect { _state.value = it }
        }
    }

    fun setMonth(month: String) { monthKeyFlow.value = month }

    fun startAdd() = _state.update { it.copy(editor = BudgetEditor()) }

    fun startEdit(budget: Budget) {
        _state.update {
            it.copy(
                editor = BudgetEditor(
                    existingId = budget.id,
                    category = budget.category,
                    amount = (budget.limitMinor / 100.0).toString(),
                ),
            )
        }
    }

    fun cancelEditor() = _state.update { it.copy(editor = null) }

    fun onEditorCategory(category: Category) =
        _state.update { it.copy(editor = it.editor?.copy(category = category)) }

    fun onEditorAmount(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(editor = it.editor?.copy(amount = sanitized)) }
    }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        val amountMinor = editor.amount.toDoubleOrNull()?.toMinorUnits() ?: 0L
        if (amountMinor <= 0) {
            _state.update { it.copy(errorMessage = "Amount must be greater than zero") }
            return
        }
        viewModelScope.launch {
            val budget = Budget(
                id = editor.existingId ?: UUID.randomUUID().toString(),
                category = editor.category,
                monthKey = _state.value.monthKey,
                limitMinor = amountMinor,
            )
            runCatching { budgetRepository.upsertBudget(budget) }
                .onSuccess { _state.update { it.copy(editor = null) } }
                .onFailure { t -> _state.update { it.copy(errorMessage = t.message) } }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { budgetRepository.deleteBudget(id) }
                .onFailure { t -> _state.update { it.copy(errorMessage = t.message) } }
        }
    }

    fun onErrorShown() = _state.update { it.copy(errorMessage = null) }
}
