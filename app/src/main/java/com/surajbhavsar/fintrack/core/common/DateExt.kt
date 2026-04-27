package com.surajbhavsar.fintrack.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

fun Long.toDisplayDate(): String = displayFormat.format(Date(this))

fun Long.toMonthKey(): String = monthKeyFormat.format(Date(this))

fun monthRangeMillis(monthKey: String): Pair<Long, Long> {
    val parts = monthKey.split("-")
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, parts[0].toInt())
        set(Calendar.MONTH, parts[1].toInt() - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis - 1
    return start to end
}
