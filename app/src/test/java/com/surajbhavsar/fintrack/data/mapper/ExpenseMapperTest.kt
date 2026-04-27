package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.data.local.entity.ExpenseEntity
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseMapperTest {

    @Test
    fun `entity round-trips through domain model`() {
        val entity = ExpenseEntity(
            id = "id-1",
            title = "Lunch",
            amountMinor = 4500,
            category = "FOOD",
            note = "with team",
            occurredAt = 1_700_000_000_000,
            monthKey = "2023-11",
            recurringRuleId = null,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_000_000,
            pendingSync = true,
            deleted = false,
        )

        val domain = entity.toDomain()
        assertEquals("id-1", domain.id)
        assertEquals("Lunch", domain.title)
        assertEquals(4500L, domain.amountMinor)
        assertEquals(Category.FOOD, domain.category)
        assertEquals("with team", domain.note)
    }

    @Test
    fun `unknown category name maps to OTHER`() {
        val entity = ExpenseEntity(
            id = "x", title = "x", amountMinor = 1, category = "WHAT_IS_THIS",
            note = null, occurredAt = 0, monthKey = "2023-11",
            recurringRuleId = null, createdAt = 0, updatedAt = 0,
        )
        assertEquals(Category.OTHER, entity.toDomain().category)
    }

    @Test
    fun `domain to entity stamps monthKey from occurredAt`() {
        val expense = Expense(
            id = "id-2",
            title = "Coffee",
            amountMinor = 200,
            category = Category.FOOD,
            note = null,
            occurredAt = 1_700_000_000_000,
        )
        val entity = expense.toEntity()
        assertEquals("id-2", entity.id)
        assertEquals("FOOD", entity.category)
        assertTrue(entity.monthKey.matches(Regex("\\d{4}-\\d{2}")))
        assertTrue(entity.pendingSync)
        assertFalse(entity.deleted)
    }

    @Test
    fun `domain to entity respects pendingSync and deleted overrides`() {
        val expense = Expense(
            id = "id-3", title = "X", amountMinor = 100,
            category = Category.OTHER, note = null, occurredAt = 0,
        )
        val entity = expense.toEntity(pendingSync = false, deleted = true)
        assertFalse(entity.pendingSync)
        assertTrue(entity.deleted)
    }
}
