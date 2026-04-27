package com.surajbhavsar.fintrack.feature.recurring

import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import com.surajbhavsar.fintrack.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringRulesViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val ruleRepo: RecurringRuleRepository = mockk(relaxed = true)
    private val preferences: PreferencesRepository = mockk(relaxed = true)

    @Test
    fun `startAdd opens editor`() = runTest {
        every { ruleRepo.observeActiveRules() } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")

        val vm = RecurringRulesViewModel(ruleRepo, preferences)
        vm.startAdd()

        assertNotNull(vm.state.value.editor)
    }

    @Test
    fun `saveEditor with empty title sets error`() = runTest {
        every { ruleRepo.observeActiveRules() } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")

        val vm = RecurringRulesViewModel(ruleRepo, preferences)
        vm.startAdd()
        vm.onAmount("100")
        vm.saveEditor()
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
        coVerify(exactly = 0) { ruleRepo.upsert(any()) }
    }

    @Test
    fun `saveEditor with zero amount sets error`() = runTest {
        every { ruleRepo.observeActiveRules() } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")

        val vm = RecurringRulesViewModel(ruleRepo, preferences)
        vm.startAdd()
        vm.onTitle("Rent")
        vm.onAmount("0")
        vm.saveEditor()
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun `saveEditor persists rule with chosen fields`() = runTest {
        every { ruleRepo.observeActiveRules() } returns MutableStateFlow(emptyList())
        every { preferences.currencyCode } returns MutableStateFlow("INR")
        val captured = slot<RecurringRule>()
        coEvery { ruleRepo.upsert(capture(captured)) } returns Unit

        val vm = RecurringRulesViewModel(ruleRepo, preferences)
        vm.startAdd()
        vm.onTitle("Rent")
        vm.onAmount("250.00")
        vm.onCategory(Category.BILLS)
        vm.onFrequency(Frequency.MONTHLY)
        vm.onNextRunAt(123L)
        vm.saveEditor()
        advanceUntilIdle()

        assertEquals("Rent", captured.captured.title)
        assertEquals(25_000L, captured.captured.amountMinor)
        assertEquals(Category.BILLS, captured.captured.category)
        assertEquals(Frequency.MONTHLY, captured.captured.frequency)
        assertEquals(123L, captured.captured.nextRunAt)
        assertNull(vm.state.value.editor)
    }
}
