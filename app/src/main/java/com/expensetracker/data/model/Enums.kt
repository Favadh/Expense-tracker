package com.expensetracker.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class DebtType {
    LENT,       // You gave money (You'll get)
    BORROWED    // You took money (You'll give)
}

enum class TimePeriod(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    ALL("All Time")
}
