package com.surajbhavsar.fintrack.core.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Long.toMoney(currencyCode: String = "INR"): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance(currencyCode)
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    return format.format(this / 100.0)
}

fun Double.toMinorUnits(): Long = Math.round(this * 100)
