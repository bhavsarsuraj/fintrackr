package com.surajbhavsar.fintrack.feature.theme

import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val preferences: PreferencesRepository = mockk(relaxed = true)

    @Test
    fun `null preference maps to System mode`() = runTest {
        every { preferences.darkTheme } returns flowOf(null)
        val vm = ThemeViewModel(preferences)
        advanceUntilIdle()
        assertEquals(ThemeMode.System, vm.themeMode.value)
    }

    @Test
    fun `true preference maps to Dark`() = runTest {
        every { preferences.darkTheme } returns flowOf(true)
        val vm = ThemeViewModel(preferences)
        advanceUntilIdle()
        assertEquals(ThemeMode.Dark, vm.themeMode.value)
    }

    @Test
    fun `false preference maps to Light`() = runTest {
        every { preferences.darkTheme } returns flowOf(false)
        val vm = ThemeViewModel(preferences)
        advanceUntilIdle()
        assertEquals(ThemeMode.Light, vm.themeMode.value)
    }

    @Test
    fun `setMode System clears preference`() = runTest {
        every { preferences.darkTheme } returns flowOf(true)
        coEvery { preferences.clearDarkTheme() } returns Unit

        val vm = ThemeViewModel(preferences)
        vm.setMode(ThemeMode.System)
        advanceUntilIdle()

        coVerify { preferences.clearDarkTheme() }
    }

    @Test
    fun `setMode Dark sets preference true`() = runTest {
        every { preferences.darkTheme } returns flowOf(null)
        coEvery { preferences.setDarkTheme(true) } returns Unit

        val vm = ThemeViewModel(preferences)
        vm.setMode(ThemeMode.Dark)
        advanceUntilIdle()

        coVerify { preferences.setDarkTheme(true) }
    }

    @Test
    fun `setMode Light sets preference false`() = runTest {
        every { preferences.darkTheme } returns flowOf(null)
        coEvery { preferences.setDarkTheme(false) } returns Unit

        val vm = ThemeViewModel(preferences)
        vm.setMode(ThemeMode.Light)
        advanceUntilIdle()

        coVerify { preferences.setDarkTheme(false) }
    }
}
