package com.surajbhavsar.fintrack.feature.settings

import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.AuthState
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import com.surajbhavsar.fintrack.work.SyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val preferences: PreferencesRepository = mockk(relaxed = true)
    private val syncScheduler: SyncScheduler = mockk(relaxed = true)
    private val authRepo: AuthRepository = mockk()

    @Test
    fun `currency reflects preference flow`() = runTest {
        every { preferences.currencyCode } returns flowOf("USD")
        every { preferences.lastSyncedAt } returns flowOf(0L)
        every { authRepo.authState } returns flowOf(AuthState.SignedOut)

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        advanceUntilIdle()

        assertEquals("USD", vm.currency.value)
    }

    @Test
    fun `userEmail reflects signed-in state`() = runTest {
        every { preferences.currencyCode } returns flowOf("INR")
        every { preferences.lastSyncedAt } returns flowOf(0L)
        every { authRepo.authState } returns flowOf(
            AuthState.SignedIn(userId = "u-1", email = "a@b.com"),
        )

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        advanceUntilIdle()

        assertEquals("a@b.com", vm.userEmail.value)
    }

    @Test
    fun `userEmail is null when signed out`() = runTest {
        every { preferences.currencyCode } returns flowOf("INR")
        every { preferences.lastSyncedAt } returns flowOf(0L)
        every { authRepo.authState } returns flowOf(AuthState.SignedOut)

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        advanceUntilIdle()

        assertEquals(null, vm.userEmail.value)
    }

    @Test
    fun `setCurrency writes preference`() = runTest {
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        every { preferences.lastSyncedAt } returns MutableStateFlow(0L)
        every { authRepo.authState } returns MutableStateFlow(AuthState.SignedOut)
        coEvery { preferences.setCurrencyCode(any()) } returns Unit

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        vm.setCurrency("EUR")
        advanceUntilIdle()

        coVerify { preferences.setCurrencyCode("EUR") }
    }

    @Test
    fun `syncNow triggers scheduler`() {
        every { preferences.currencyCode } returns flowOf("INR")
        every { preferences.lastSyncedAt } returns flowOf(0L)
        every { authRepo.authState } returns flowOf(AuthState.SignedOut)

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        vm.syncNow()

        verify { syncScheduler.runOnce() }
    }

    @Test
    fun `signOut delegates to auth repo`() = runTest {
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        every { preferences.lastSyncedAt } returns MutableStateFlow(0L)
        every { authRepo.authState } returns MutableStateFlow(AuthState.SignedOut)
        coEvery { authRepo.signOut() } returns Unit

        val vm = SettingsViewModel(preferences, syncScheduler, authRepo)
        vm.signOut()
        advanceUntilIdle()

        coVerify { authRepo.signOut() }
    }
}
