package com.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.InsertChart)
    object Debt : Screen("debt", "Debts", Icons.Default.Handshake)
    object Budget : Screen("budget", "Budget", Icons.Default.PieChart)
    object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalance)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AddTransaction : Screen("add_transaction", "Add Transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}", "Edit Transaction") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }

    companion object {
        val bottomNavItems = listOf(Home, Analytics, Debt, Budget, Settings)
    }
}
