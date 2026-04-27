package com.surajbhavsar.fintrack.feature.auth

enum class AuthMode { SignIn, SignUp }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
