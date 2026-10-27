package com.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.expensetracker.ui.navigation.AppNavigation
import com.expensetracker.ui.theme.ExpenseTrackerTheme
import com.expensetracker.ui.viewmodel.ExpenseViewModel
import com.expensetracker.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        val app = application as ExpenseApplication
        ExpenseViewModelFactory(
            application = app,
            expenseRepository = app.expenseRepository,
            preferenceRepository = app.preferenceRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            ExpenseTrackerTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
