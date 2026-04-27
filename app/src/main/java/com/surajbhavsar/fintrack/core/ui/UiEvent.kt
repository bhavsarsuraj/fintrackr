package com.surajbhavsar.fintrack.core.ui

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data object NavigateBack : UiEvent
}
