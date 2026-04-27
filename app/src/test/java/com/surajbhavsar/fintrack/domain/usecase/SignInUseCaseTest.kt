package com.surajbhavsar.fintrack.domain.usecase

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInUseCaseTest {

    private val auth: AuthRepository = mockk(relaxed = true)
    private val useCase = SignInUseCase(auth)

    @Test
    fun `fails when email is blank`() = runTest {
        val result = useCase("  ", "secret123")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { auth.signIn(any(), any()) }
    }

    @Test
    fun `fails when email is malformed`() = runTest {
        val result = useCase("not-an-email", "secret123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `fails when password is blank`() = runTest {
        val result = useCase("user@example.com", "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `delegates to repository when input is valid and trims email`() = runTest {
        coEvery { auth.signIn("user@example.com", "secret123") } returns Result.success("uid-1")

        val result = useCase("  user@example.com  ", "secret123")

        assertTrue(result.isSuccess)
        coVerify { auth.signIn("user@example.com", "secret123") }
    }
}
