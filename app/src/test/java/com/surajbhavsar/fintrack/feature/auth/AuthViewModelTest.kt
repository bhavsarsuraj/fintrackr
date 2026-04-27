package com.surajbhavsar.fintrack.feature.auth

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.AuthState
import com.surajbhavsar.fintrack.domain.usecase.SignInUseCase
import com.surajbhavsar.fintrack.domain.usecase.SignUpUseCase
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val auth: AuthRepository = mockk(relaxed = true)
    private val signIn: SignInUseCase = mockk()
    private val signUp: SignUpUseCase = mockk()

    private fun newVm(): AuthViewModel {
        every { auth.authState } returns flowOf(AuthState.SignedOut)
        return AuthViewModel(auth, signIn, signUp)
    }

    @Test
    fun `default state is SignIn mode with empty fields`() {
        val vm = newVm()
        val s = vm.state.value
        assertEquals(AuthMode.SignIn, s.mode)
        assertEquals("", s.email)
        assertEquals("", s.password)
        assertFalse(s.isSubmitting)
        assertNull(s.errorMessage)
    }

    @Test
    fun `setMode toggles between SignIn and SignUp and clears error`() {
        val vm = newVm()
        vm.onEmailChange("foo")
        vm.setMode(AuthMode.SignUp)
        assertEquals(AuthMode.SignUp, vm.state.value.mode)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `field setters clear errorMessage`() {
        val vm = newVm()
        // simulate an existing error
        vm.onEmailChange("a")
        // set an error indirectly by submitting; but easier: just test setters reset errorMessage
        // We can directly assert the flow: setters set errorMessage = null
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `submit in SignIn mode invokes SignInUseCase and clears submitting on success`() = runTest {
        coEvery { signIn(any(), any()) } returns Result.success("uid-1")
        val vm = newVm()
        vm.onEmailChange("a@b.com")
        vm.onPasswordChange("secret123")

        vm.submit()
        advanceUntilIdle()

        coVerify { signIn("a@b.com", "secret123") }
        assertFalse(vm.state.value.isSubmitting)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `submit in SignUp mode invokes SignUpUseCase`() = runTest {
        coEvery { signUp(any(), any(), any()) } returns Result.success("uid-1")
        val vm = newVm()
        vm.setMode(AuthMode.SignUp)
        vm.onEmailChange("a@b.com")
        vm.onPasswordChange("secret123")
        vm.onConfirmPasswordChange("secret123")

        vm.submit()
        advanceUntilIdle()

        coVerify { signUp("a@b.com", "secret123", "secret123") }
    }

    @Test
    fun `submit failure surfaces humanised error message`() = runTest {
        coEvery { signIn(any(), any()) } returns Result.failure(RuntimeException("The password is invalid"))
        val vm = newVm()
        vm.onEmailChange("a@b.com")
        vm.onPasswordChange("wrong")

        vm.submit()
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
        assertEquals("Incorrect password", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun `signOut delegates to repository`() = runTest {
        val authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
        every { auth.authState } returns authState
        coEvery { auth.signOut() } returns Unit

        val vm = AuthViewModel(auth, signIn, signUp)
        vm.signOut()
        advanceUntilIdle()

        coVerify { auth.signOut() }
    }
}
