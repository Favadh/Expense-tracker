package com.expensetracker.data.repository

import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.model.*
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()
    private val debtDao = database.debtDao()
    private val budgetDao = database.budgetDao()

    // Transactions
    val allTransactions: Flow<List<TransactionWithDetails>> = transactionDao.getAllTransactionsWithDetails()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsBetweenDates(start, end)

    suspend fun getTransactionById(id: Long): TransactionWithDetails? =
        transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        // Update account balances
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    accountDao.updateBalance(targetId, transaction.amount)
                }
            }
        }
        return id
    }

    suspend fun updateTransaction(newTx: TransactionEntity) {
        val oldTxWithDetails = transactionDao.getTransactionById(newTx.id)
        if (oldTxWithDetails != null) {
            val oldTx = oldTxWithDetails.transaction
            // Revert old transaction balance impact
            when (oldTx.type) {
                TransactionType.EXPENSE -> {
                    accountDao.updateBalance(oldTx.accountId, oldTx.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.updateBalance(oldTx.accountId, -oldTx.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.updateBalance(oldTx.accountId, oldTx.amount)
                    oldTx.targetAccountId?.let { targetId ->
                        accountDao.updateBalance(targetId, -oldTx.amount)
                    }
                }
            }
        }

        // Update transaction in DB
        transactionDao.updateTransaction(newTx)

        // Apply new transaction balance impact
        when (newTx.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(newTx.accountId, -newTx.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(newTx.accountId, newTx.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(newTx.accountId, -newTx.amount)
                newTx.targetAccountId?.let { targetId ->
                    accountDao.updateBalance(targetId, newTx.amount)
                }
            }
        }
    }

    suspend fun deleteTransaction(txWithDetails: TransactionWithDetails) {
        val tx = txWithDetails.transaction
        // Revert balance impact
        when (tx.type) {
            TransactionType.EXPENSE -> {
                accountDao.updateBalance(tx.accountId, tx.amount)
            }
            TransactionType.INCOME -> {
                accountDao.updateBalance(tx.accountId, -tx.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(tx.accountId, tx.amount)
                tx.targetAccountId?.let { targetId ->
                    accountDao.updateBalance(targetId, -tx.amount)
                }
            }
        }
        transactionDao.deleteTransaction(tx)
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(isExpense)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    // Accounts
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val totalAccountsBalance: Flow<Double> = accountDao.getTotalAccountsBalance()

    suspend fun insertAccount(account: AccountEntity): Long =
        accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) =
        accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) =
        accountDao.deleteAccount(account)

    // Debts
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    fun getDebtsByType(type: DebtType): Flow<List<DebtEntity>> =
        debtDao.getDebtsByType(type)

    fun getTotalUnsettledDebt(type: DebtType): Flow<Double> =
        debtDao.getTotalUnsettledAmountByType(type)

    suspend fun insertDebt(debt: DebtEntity): Long =
        debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) =
        debtDao.updateDebt(debt)

    suspend fun setDebtSettled(id: Long, isSettled: Boolean) =
        debtDao.setSettledStatus(id, isSettled)

    suspend fun deleteDebt(debt: DebtEntity) =
        debtDao.deleteDebt(debt)

    // Budgets
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(monthYear)

    fun getOverallBudget(monthYear: String): Flow<BudgetEntity?> =
        budgetDao.getOverallBudget(monthYear)

    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long =
        budgetDao.insertOrUpdateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)
}
