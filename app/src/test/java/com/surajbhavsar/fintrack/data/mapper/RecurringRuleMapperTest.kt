package com.surajbhavsar.fintrack.data.mapper

import com.surajbhavsar.fintrack.data.local.entity.RecurringRuleEntity
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringRuleMapperTest {

    @Test
    fun `entity to domain decodes frequency`() {
        val entity = RecurringRuleEntity(
            id = "r1",
            title = "Rent",
            amountMinor = 25_000_00,
            category = "BILLS",
            frequency = "MONTHLY",
            nextRunAt = 1_700_000_000_000,
            active = true,
        )
        val domain = entity.toDomain()
        assertEquals(Frequency.MONTHLY, domain.frequency)
        assertEquals(Category.BILLS, domain.category)
    }

    @Test
    fun `unknown frequency falls back to MONTHLY`() {
        val entity = RecurringRuleEntity(
            id = "r2", title = "X", amountMinor = 1,
            category = "OTHER", frequency = "BOGUS",
            nextRunAt = 0, active = true,
        )
        assertEquals(Frequency.MONTHLY, entity.toDomain().frequency)
    }

    @Test
    fun `domain to entity encodes frequency and category`() {
        val rule = RecurringRule(
            id = "r3", title = "Gym", amountMinor = 1500_00,
            category = Category.HEALTH, frequency = Frequency.WEEKLY,
            nextRunAt = 0, active = true,
        )
        val entity = rule.toEntity()
        assertEquals("WEEKLY", entity.frequency)
        assertEquals("HEALTH", entity.category)
    }
}
