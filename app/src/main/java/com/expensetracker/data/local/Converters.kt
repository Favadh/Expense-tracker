package com.expensetracker.data.local

import androidx.room.TypeConverter
import com.expensetracker.data.model.DebtType
import com.expensetracker.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }

    @TypeConverter
    fun fromDebtType(type: DebtType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toDebtType(value: String?): DebtType? {
        return value?.let { DebtType.valueOf(it) }
    }
}
