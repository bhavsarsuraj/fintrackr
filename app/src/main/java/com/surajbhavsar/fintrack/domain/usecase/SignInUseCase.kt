package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (!trimmedEmail.matches(EMAIL_REGEX)) return Result.failure(IllegalArgumentException("Enter a valid email"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Password is required"))
        return authRepository.signIn(trimmedEmail, password)
    }

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
