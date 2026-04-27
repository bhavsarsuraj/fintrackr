package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String): Result<String> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (!trimmedEmail.matches(SignInUseCase.EMAIL_REGEX)) return Result.failure(IllegalArgumentException("Enter a valid email"))
        if (password.length < MIN_PASSWORD_LENGTH) return Result.failure(IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters"))
        if (password != confirmPassword) return Result.failure(IllegalArgumentException("Passwords do not match"))
        return authRepository.signUp(trimmedEmail, password)
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
