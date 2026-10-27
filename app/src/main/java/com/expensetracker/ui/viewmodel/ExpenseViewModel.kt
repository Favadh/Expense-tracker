package com.expensetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.model.*
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.repository.PreferenceRepository
import com.expensetracker.data.util.BackupExportUtil
import com.expensetracker.data.util.DateUtil
import com.expensetracker.ui.components.MonthlyBarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(
    application: Application,
    private val expenseRepository: ExpenseRepository,
    private val preferenceRepository: PreferenceRepository
) : AndroidViewModel(application) {

    // Settings
    val currencySymbol = preferenceRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val themeMode = preferenceRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val appLockEnabled = preferenceRepository.appLockEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appPin = preferenceRepository.appPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Selected Time Period Filter
    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTHLY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    // All categories & accounts
    val allCategories = expenseRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = expenseRepository.getCategoriesByType(isExpense = true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories = expenseRepository.getCategoriesByType(isExpense = false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts = expenseRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAccountsBalance = expenseRepository.totalAccountsBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filtered transactions based on selected period
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredTransactions: StateFlow<List<TransactionWithDetails>> = _selectedPeriod
        .flatMapLatest { period ->
            val range = DateUtil.getTimeRangeForPeriod(period)
            expenseRepository.getTransactionsBetween(range.first, range.second)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped transactions by date header (e.g. "Today, 26 Sep", "Yesterday", etc.)
    val groupedTransactions: StateFlow<Map<String, List<TransactionWithDetails>>> = filteredTransactions
        .map { list ->
            list.groupBy { DateUtil.formatDayHeader(it.transaction.timestamp) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Financial summaries for selected period
    val periodIncome: StateFlow<Double> = filteredTransactions
        .map { list ->
            list.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val periodExpense: StateFlow<Double> = filteredTransactions
        .map { list ->
            list.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category breakdown for analytics pie chart
    val categorySpendings: StateFlow<List<CategorySpending>> = filteredTransactions
        .map { list ->
            val expenseTx = list.filter { it.transaction.type == TransactionType.EXPENSE }
            expenseTx.groupBy { it.category?.id ?: -1L }
                .map { (_, txGroup) ->
                    val cat = txGroup.first().category
                    CategorySpending(
                        categoryId = cat?.id ?: -1L,
                        categoryName = cat?.name ?: "Other",
                        iconName = cat?.iconName ?: "more_horiz",
                        colorHex = cat?.colorHex ?: "#795548",
                        totalAmount = txGroup.sumOf { it.transaction.amount },
                        transactionCount = txGroup.size
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Bar Chart Data (last 6 months comparison)
    val allTransactions = expenseRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBarData: StateFlow<List<MonthlyBarData>> = allTransactions
        .map { list ->
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val result = mutableListOf<MonthlyBarData>()

            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                val mLabel = monthFormat.format(cal.time)
                val targetYear = cal.get(Calendar.YEAR)
                val targetMonth = cal.get(Calendar.MONTH)

                val monthTx = list.filter {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = it.transaction.timestamp }
                    itemCal.get(Calendar.YEAR) == targetYear && itemCal.get(Calendar.MONTH) == targetMonth
                }

                val inc = monthTx.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
                val exp = monthTx.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

                result.add(MonthlyBarData(monthLabel = mLabel, income = inc, expense = exp))
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Debts
    val allDebts = expenseRepository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLentUnsettled = expenseRepository.getTotalUnsettledDebt(DebtType.LENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBorrowedUnsettled = expenseRepository.getTotalUnsettledDebt(DebtType.BORROWED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Budgets
    private val currentMonthKey = DateUtil.formatMonthYearKey(System.currentTimeMillis())

    val overallBudget: StateFlow<BudgetEntity?> = expenseRepository.getOverallBudget(currentMonthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Actions
    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun saveTransaction(
        id: Long = 0,
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        accountId: Long,
        targetAccountId: Long? = null,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = TransactionEntity(
                id = id,
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                targetAccountId = targetAccountId,
                note = note,
                timestamp = timestamp
            )
            if (id == 0L) {
                expenseRepository.insertTransaction(entity)
            } else {
                expenseRepository.updateTransaction(entity)
            }
        }
    }

    fun deleteTransaction(txWithDetails: TransactionWithDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteTransaction(txWithDetails)
        }
    }

    fun addAccount(name: String, balance: Double, iconName: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.insertAccount(
                AccountEntity(name = name, balance = balance, iconName = iconName, colorHex = colorHex)
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteAccount(account)
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, isExpense: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.insertCategory(
                CategoryEntity(name = name, iconName = iconName, colorHex = colorHex, isExpense = isExpense)
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteCategory(category)
        }
    }

    fun addDebt(personName: String, amount: Double, type: DebtType, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.insertDebt(
                DebtEntity(personName = personName, amount = amount, type = type, note = note)
            )
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.setDebtSettled(debt.id, !debt.isSettled)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteDebt(debt)
        }
    }

    fun setBudgetLimit(limitAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.insertOrUpdateBudget(
                BudgetEntity(
                    id = overallBudget.value?.id ?: 0,
                    categoryId = null,
                    monthlyLimitAmount = limitAmount,
                    monthYear = currentMonthKey
                )
            )
        }
    }

    fun setCurrency(symbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.setCurrencySymbol(symbol)
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.setThemeMode(mode)
        }
    }

    fun setAppLock(enabled: Boolean, pin: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceRepository.setAppLock(enabled, pin)
        }
    }

    fun exportTransactionsCsv(): String {
        return BackupExportUtil.exportTransactionsToCsv(allTransactions.value)
    }

    fun exportDebtsCsv(): String {
        return BackupExportUtil.exportDebtsToCsv(allDebts.value)
    }
}

class ExpenseViewModelFactory(
    private val application: Application,
    private val expenseRepository: ExpenseRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(application, expenseRepository, preferenceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
