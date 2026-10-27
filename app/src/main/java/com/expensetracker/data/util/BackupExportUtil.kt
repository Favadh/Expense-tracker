package com.expensetracker.data.util

import com.expensetracker.data.model.DebtEntity
import com.expensetracker.data.model.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.*

object BackupExportUtil {
    private val csvDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportTransactionsToCsv(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.append("ID,Type,Amount,Category,Account,Target Account,Date & Time,Note\n")

        for (item in transactions) {
            val tx = item.transaction
            val typeStr = tx.type.name
            val amountStr = tx.amount.toString()
            val categoryStr = escapeCsv(item.category?.name ?: "Uncategorized")
            val accountStr = escapeCsv(item.account?.name ?: "Unknown")
            val targetAccountStr = escapeCsv(item.targetAccount?.name ?: "")
            val dateStr = csvDateFormatter.format(Date(tx.timestamp))
            val noteStr = escapeCsv(tx.note)

            sb.append("${tx.id},$typeStr,$amountStr,$categoryStr,$accountStr,$targetAccountStr,$dateStr,$noteStr\n")
        }
        return sb.toString()
    }

    fun exportDebtsToCsv(debts: List<DebtEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Person Name,Amount,Type,Status,Due Date,Created Date,Note\n")

        for (debt in debts) {
            val typeStr = debt.type.name
            val statusStr = if (debt.isSettled) "Settled" else "Pending"
            val dueDateStr = debt.dueDate?.let { csvDateFormatter.format(Date(it)) } ?: ""
            val createdDateStr = csvDateFormatter.format(Date(debt.createdAt))
            val personStr = escapeCsv(debt.personName)
            val noteStr = escapeCsv(debt.note)

            sb.append("${debt.id},$personStr,${debt.amount},$typeStr,$statusStr,$dueDateStr,$createdDateStr,$noteStr\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
