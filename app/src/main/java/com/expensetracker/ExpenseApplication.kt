package com.expensetracker

import android.app.Application
import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ExpenseApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val expenseRepository by lazy { ExpenseRepository(database) }
    val preferenceRepository by lazy { PreferenceRepository(this) }
}
