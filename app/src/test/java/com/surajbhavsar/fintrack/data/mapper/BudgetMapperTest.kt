package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.data.local.entity.BudgetEntity
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetMapperTest {

    @Test
    fun `entity to domain preserves fields and decodes category`() {
        val entity = BudgetEntity(
            id = "b1",
            category = "GROCERIES",
            monthKey = "2026-04",
            limitMinor = 500_00,
        )
        val domain = entity.toDomain()
        assertEquals("b1", domain.id)
        assertEquals(Category.GROCERIES, domain.category)
        assertEquals("2026-04", domain.monthKey)
        assertEquals(500_00L, domain.limitMinor)
    }

    @Test
    fun `domain to entity encodes category as enum name`() {
        val budget = Budget(
            id = "b2",
            category = Category.HEALTH,
            monthKey = "2026-04",
            limitMinor = 1_000_00,
        )
        val entity = budget.toEntity()
        assertEquals("HEALTH", entity.category)
        assertEquals("2026-04", entity.monthKey)
        assertEquals(1_000_00L, entity.limitMinor)
    }
}
