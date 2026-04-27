package com.surajbhavsar.fintrack.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.AuthState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val state = if (user == null) AuthState.SignedOut
            else AuthState.SignedIn(userId = user.uid, email = user.email)
            trySend(state)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUserId: Flow<String?> = authState.map { state ->
        when (state) {
            is AuthState.SignedIn -> state.userId
            else -> null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("Sign-in returned no user id")
    }

    override suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("Sign-up returned no user id")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
