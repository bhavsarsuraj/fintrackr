package com.surajbhavsar.fintrack.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateExtTest {

    @Test
    fun `toMonthKey produces yyyy-MM format`() {
        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            set(2026, Calendar.APRIL, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val key = cal.timeInMillis.toMonthKey()
        assertEquals("2026-04", key)
    }

    @Test
    fun `monthRangeMillis returns start of month and inclusive last millis`() {
        val (start, end) = monthRangeMillis("2026-04")
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2026, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))

        val endCal = Calendar.getInstance().apply { timeInMillis = end }
        // End is in the same month
        assertEquals(Calendar.APRIL, endCal.get(Calendar.MONTH))
        // End is greater than start
        assertTrue(end > start)
    }

    @Test
    fun `toDisplayDate is non-empty`() {
        val display = 1_700_000_000_000L.toDisplayDate()
        assertTrue(display.isNotBlank())
    }
}
