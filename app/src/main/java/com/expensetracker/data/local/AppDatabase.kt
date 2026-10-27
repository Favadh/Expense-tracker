package com.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expensetracker.data.local.dao.*
import com.expensetracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        DebtEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun debtDao(): DebtDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    .addCallback(DatabaseInitializerCallback(scope) { INSTANCE })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DatabaseInitializerCallback(
    private val scope: CoroutineScope,
    private val databaseProvider: () -> AppDatabase?
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch(Dispatchers.IO) {
            databaseProvider()?.let { database ->
                populateDefaultData(database)
            }
        }
    }

    private suspend fun populateDefaultData(database: AppDatabase) {
        val categoryDao = database.categoryDao()
        val accountDao = database.accountDao()

        // Seed Default Expense Categories
        val expenseCategories = listOf(
            CategoryEntity(name = "Food & Dining", iconName = "restaurant", colorHex = "#FF5722", isExpense = true, isDefault = true),
            CategoryEntity(name = "Groceries", iconName = "shopping_cart", colorHex = "#4CAF50", isExpense = true, isDefault = true),
            CategoryEntity(name = "Transport", iconName = "directions_car", colorHex = "#2196F3", isExpense = true, isDefault = true),
            CategoryEntity(name = "Shopping", iconName = "shopping_bag", colorHex = "#E91E63", isExpense = true, isDefault = true),
            CategoryEntity(name = "Bills & Utilities", iconName = "receipt_long", colorHex = "#FF9800", isExpense = true, isDefault = true),
            CategoryEntity(name = "Entertainment", iconName = "movie", colorHex = "#9C27B0", isExpense = true, isDefault = true),
            CategoryEntity(name = "Healthcare", iconName = "medical_services", colorHex = "#F44336", isExpense = true, isDefault = true),
            CategoryEntity(name = "Education", iconName = "school", colorHex = "#3F51B5", isExpense = true, isDefault = true),
            CategoryEntity(name = "Travel", iconName = "flight", colorHex = "#00BCD4", isExpense = true, isDefault = true),
            CategoryEntity(name = "Personal Care", iconName = "spa", colorHex = "#009688", isExpense = true, isDefault = true),
            CategoryEntity(name = "Others", iconName = "more_horiz", colorHex = "#795548", isExpense = true, isDefault = true)
        )

        // Seed Default Income Categories
        val incomeCategories = listOf(
            CategoryEntity(name = "Salary", iconName = "payments", colorHex = "#2E7D32", isExpense = false, isDefault = true),
            CategoryEntity(name = "Freelance / Business", iconName = "work", colorHex = "#1565C0", isExpense = false, isDefault = true),
            CategoryEntity(name = "Investment", iconName = "trending_up", colorHex = "#6A1B9A", isExpense = false, isDefault = true),
            CategoryEntity(name = "Rental Income", iconName = "home", colorHex = "#E65100", isExpense = false, isDefault = true),
            CategoryEntity(name = "Gifts & Bonus", iconName = "card_giftcard", colorHex = "#AD1457", isExpense = false, isDefault = true),
            CategoryEntity(name = "Other Income", iconName = "attach_money", colorHex = "#37474F", isExpense = false, isDefault = true)
        )

        categoryDao.insertAllCategories(expenseCategories + incomeCategories)

        // Seed Default Accounts
        val defaultAccounts = listOf(
            AccountEntity(name = "Cash", balance = 0.0, iconName = "payments", colorHex = "#4CAF50", isDefault = true),
            AccountEntity(name = "Bank Account", balance = 0.0, iconName = "account_balance", colorHex = "#2196F3", isDefault = false),
            AccountEntity(name = "Credit Card", balance = 0.0, iconName = "credit_card", colorHex = "#E91E63", isDefault = false),
            AccountEntity(name = "UPI / Digital Wallet", balance = 0.0, iconName = "account_balance_wallet", colorHex = "#9C27B0", isDefault = false)
        )

        accountDao.insertAllAccounts(defaultAccounts)
    }
}
