package com.surajbhavsar.fintrack.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.core.common.toMinorUnits
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecurringRulesViewModel @Inject constructor(
    private val ruleRepository: RecurringRuleRepository,
    preferences: PreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringRulesUiState())
    val state: StateFlow<RecurringRulesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ruleRepository.observeActiveRules(),
                preferences.currencyCode,
            ) { rules, currency ->
                _state.value.copy(isLoading = false, rules = rules, currencyCode = currency)
            }.collect { _state.value = it }
        }
    }

    fun startAdd() = _state.update { it.copy(editor = RuleEditor()) }

    fun cancelEditor() = _state.update { it.copy(editor = null) }

    fun onTitle(value: String) = _state.update { it.copy(editor = it.editor?.copy(title = value)) }
    fun onAmount(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(editor = it.editor?.copy(amount = sanitized)) }
    }
    fun onCategory(value: Category) = _state.update { it.copy(editor = it.editor?.copy(category = value)) }
    fun onFrequency(value: Frequency) = _state.update { it.copy(editor = it.editor?.copy(frequency = value)) }
    fun onNextRunAt(value: Long) = _state.update { it.copy(editor = it.editor?.copy(nextRunAt = value)) }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        if (editor.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Title is required") }; return
        }
        val amountMinor = editor.amount.toDoubleOrNull()?.toMinorUnits() ?: 0L
        if (amountMinor <= 0L) {
            _state.update { it.copy(errorMessage = "Amount must be greater than zero") }; return
        }
        val rule = RecurringRule(
            id = UUID.randomUUID().toString(),
            title = editor.title.trim(),
            amountMinor = amountMinor,
            category = editor.category,
            frequency = editor.frequency,
            nextRunAt = editor.nextRunAt,
        )
        viewModelScope.launch {
            runCatching { ruleRepository.upsert(rule) }
                .onSuccess { _state.update { it.copy(editor = null) } }
                .onFailure { t -> _state.update { it.copy(errorMessage = t.message) } }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { ruleRepository.delete(id) }
    }

    fun onErrorShown() = _state.update { it.copy(errorMessage = null) }
}
