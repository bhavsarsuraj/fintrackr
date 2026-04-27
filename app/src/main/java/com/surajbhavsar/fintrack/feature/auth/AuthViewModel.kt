package com.surajbhavsar.fintrack.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.AuthState
import com.surajbhavsar.fintrack.domain.usecase.SignInUseCase
import com.surajbhavsar.fintrack.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setMode(mode: AuthMode) = _state.update {
        it.copy(mode = mode, errorMessage = null)
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _state.update {
        it.copy(confirmPassword = value, errorMessage = null)
    }

    fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = when (current.mode) {
                AuthMode.SignIn -> signInUseCase(current.email, current.password)
                AuthMode.SignUp -> signUpUseCase(current.email, current.password, current.confirmPassword)
            }
            result.fold(
                onSuccess = { _state.update { it.copy(isSubmitting = false) } },
                onFailure = { t ->
                    _state.update { it.copy(isSubmitting = false, errorMessage = humanise(t)) }
                },
            )
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    private fun humanise(t: Throwable): String = when {
        t.message?.contains("password is invalid", ignoreCase = true) == true -> "Incorrect password"
        t.message?.contains("no user record", ignoreCase = true) == true -> "No account with that email"
        t.message?.contains("email address is badly formatted", ignoreCase = true) == true -> "Enter a valid email"
        t.message?.contains("email address is already in use", ignoreCase = true) == true -> "An account with that email already exists"
        t.message?.contains("network error", ignoreCase = true) == true -> "Network error — check your connection"
        else -> t.message ?: "Something went wrong"
    }
}
