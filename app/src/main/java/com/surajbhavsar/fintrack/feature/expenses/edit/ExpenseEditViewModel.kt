package com.surajbhavsar.fintrack.feature.expenses.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.core.common.toMinorUnits
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.usecase.AddExpenseUseCase
import com.surajbhavsar.fintrack.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val addExpense: AddExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
) : ViewModel() {

    private val expenseId: String? = savedStateHandle["expenseId"]

    private val _state = MutableStateFlow(ExpenseEditUiState(id = expenseId, isLoading = expenseId != null))
    val state: StateFlow<ExpenseEditUiState> = _state.asStateFlow()

    init {
        if (expenseId != null) loadExisting(expenseId)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val expense = expenseRepository.getById(id)
            if (expense == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Expense not found") }
                return@launch
            }
            _state.update {
                ExpenseEditUiState(
                    id = expense.id,
                    title = expense.title,
                    amount = (expense.amountMinor / 100.0).toString(),
                    note = expense.note.orEmpty(),
                    category = expense.category,
                    occurredAt = expense.occurredAt,
                )
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value) }
    fun onAmountChange(value: String) = _state.update { it.copy(amount = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }
    fun onCategoryChange(value: Category) = _state.update { it.copy(category = value) }
    fun onDateChange(value: Long) = _state.update { it.copy(occurredAt = value) }

    fun save() {
        val current = _state.value
        val amountMinor = current.amount.toDoubleOrNull()?.toMinorUnits() ?: 0L

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val outcome = if (current.id == null) {
                addExpense(
                    AddExpenseUseCase.Input(
                        title = current.title,
                        amountMinor = amountMinor,
                        category = current.category,
                        note = current.note,
                        occurredAt = current.occurredAt,
                    )
                )
            } else {
                val existing = expenseRepository.getById(current.id) ?: return@launch
                updateExpense(
                    existing.copy(
                        title = current.title,
                        amountMinor = amountMinor,
                        category = current.category,
                        note = current.note.takeIf { it.isNotBlank() },
                        occurredAt = current.occurredAt,
                    )
                )
            }
            outcome.fold(
                onSuccess = { _state.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { t -> _state.update { it.copy(isSaving = false, errorMessage = t.message) } },
            )
        }
    }

    fun onErrorShown() = _state.update { it.copy(errorMessage = null) }
}
