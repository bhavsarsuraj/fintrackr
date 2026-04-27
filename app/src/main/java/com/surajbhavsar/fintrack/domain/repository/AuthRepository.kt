package com.surajbhavsar.fintrack.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val userId: String, val email: String?) : AuthState
}

interface AuthRepository {
    val currentUserId: Flow<String?>
    val authState: Flow<AuthState>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signOut()
}
