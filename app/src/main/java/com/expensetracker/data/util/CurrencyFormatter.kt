package com.expensetracker.data.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))
    private val compactFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.getDefault()))

    fun formatAmount(amount: Double, symbol: String = "₹"): String {
        return "$symbol ${format.format(amount)}"
    }

    fun formatCompact(amount: Double, symbol: String = "₹"): String {
        return if (amount % 1.0 == 0.0) {
            "$symbol ${compactFormat.format(amount)}"
        } else {
            "$symbol ${format.format(amount)}"
        }
    }
}
