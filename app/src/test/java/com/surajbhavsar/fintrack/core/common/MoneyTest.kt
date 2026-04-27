package com.surajbhavsar.fintrack.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `toMinorUnits rounds to nearest cent`() {
        assertEquals(4500L, 45.00.toMinorUnits())
        assertEquals(4501L, 45.005.toMinorUnits())  // round half up
        assertEquals(4500L, 45.004.toMinorUnits())
    }

    @Test
    fun `toMinorUnits handles zero and small values`() {
        assertEquals(0L, 0.0.toMinorUnits())
        assertEquals(1L, 0.01.toMinorUnits())
    }

    @Test
    fun `toMoney formats INR with two fraction digits`() {
        val formatted = 4500L.toMoney("INR")
        // Different locales render the symbol differently; assert digits/decimal substring.
        assert(formatted.contains("45")) { "Expected '45' in $formatted" }
        assert(formatted.contains(".00") || formatted.contains(",00")) {
            "Expected fractional digits in $formatted"
        }
    }

    @Test
    fun `toMoney formats USD`() {
        val formatted = 12345L.toMoney("USD")
        assert(formatted.contains("123")) { "Expected '123' in $formatted" }
    }
}
