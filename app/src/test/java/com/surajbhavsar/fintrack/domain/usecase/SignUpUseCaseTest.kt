package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpUseCaseTest {

    private val auth: AuthRepository = mockk(relaxed = true)
    private val useCase = SignUpUseCase(auth)

    @Test
    fun `fails when password too short`() = runTest {
        val result = useCase("user@example.com", "short", "short")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { auth.signUp(any(), any()) }
    }

    @Test
    fun `fails when passwords do not match`() = runTest {
        val result = useCase("user@example.com", "secret123", "secret456")
        assertTrue(result.isFailure)
    }

    @Test
    fun `fails when email is malformed`() = runTest {
        val result = useCase("nope", "secret123", "secret123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `delegates to repository when inputs are valid`() = runTest {
        coEvery { auth.signUp("user@example.com", "secret123") } returns Result.success("uid-1")

        val result = useCase("user@example.com", "secret123", "secret123")

        assertTrue(result.isSuccess)
        coVerify { auth.signUp("user@example.com", "secret123") }
    }
}
