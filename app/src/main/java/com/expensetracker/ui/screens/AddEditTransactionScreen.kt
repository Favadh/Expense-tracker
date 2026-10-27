package com.expensetracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.data.model.AccountEntity
import com.expensetracker.data.model.CategoryEntity
import com.expensetracker.data.model.TransactionType
import com.expensetracker.data.util.CategoryIcons
import com.expensetracker.data.util.DateUtil
import com.expensetracker.ui.components.ConfirmDeleteDialog
import com.expensetracker.ui.theme.GreenLight
import com.expensetracker.ui.theme.PurpleTransfer
import com.expensetracker.ui.theme.RedExpense
import com.expensetracker.ui.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: Long? = null,
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()

    // Existing transaction if in edit mode
    val existingItem = remember(transactionId, allTransactions) {
        if (transactionId != null && transactionId > 0) {
            allTransactions.find { it.transaction.id == transactionId }
        } else null
    }

    var selectedType by remember {
        mutableStateOf(existingItem?.transaction?.type ?: TransactionType.EXPENSE)
    }

    var amountText by remember {
        mutableStateOf(if (existingItem != null) existingItem.transaction.amount.toString() else "")
    }

    var selectedCategoryId by remember {
        mutableStateOf(existingItem?.category?.id)
    }

    var selectedAccountId by remember {
        mutableStateOf(existingItem?.account?.id ?: (allAccounts.firstOrNull()?.id ?: 1L))
    }

    var selectedTargetAccountId by remember {
        mutableStateOf(existingItem?.targetAccount?.id ?: (allAccounts.getOrNull(1)?.id ?: 2L))
    }

    var noteText by remember {
        mutableStateOf(existingItem?.transaction?.note ?: "")
    }

    var selectedTimestamp by remember {
        mutableStateOf(existingItem?.transaction?.timestamp ?: System.currentTimeMillis())
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Ensure initial category is selected when categories load
    LaunchedEffect(expenseCategories, incomeCategories, selectedType) {
        if (selectedCategoryId == null) {
            val list = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories
            selectedCategoryId = list.firstOrNull()?.id
        }
    }

    // Date & Time pickers
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedTimestamp = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedTimestamp = calendar.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    if (showDeleteDialog && existingItem != null) {
        ConfirmDeleteDialog(
            title = "Delete Transaction",
            message = "Are you sure you want to delete this transaction?",
            onConfirm = {
                viewModel.deleteTransaction(existingItem)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingItem == null) "Add Transaction" else "Edit Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existingItem != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.INCOME to "Income",
                    TransactionType.TRANSFER to "Transfer"
                ).forEach { (type, label) ->
                    val isSelected = selectedType == type
                    val activeColor = when (type) {
                        TransactionType.EXPENSE -> RedExpense
                        TransactionType.INCOME -> GreenLight
                        TransactionType.TRANSFER -> PurpleTransfer
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Amount ($currencySymbol)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        placeholder = {
                            Text(
                                "0.00",
                                style = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Categories Selection (for Expense & Income)
            if (selectedType != TransactionType.TRANSFER) {
                val currentCategoryList = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentCategoryList, key = { it.id }) { category ->
                        val isSelected = selectedCategoryId == category.id
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryId = category.id }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) catColor else catColor.copy(alpha = 0.15f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIcons.getIcon(category.iconName),
                                    contentDescription = category.name,
                                    tint = if (isSelected) Color.White else catColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Account / Payment Mode Selector
            Text(
                text = if (selectedType == TransactionType.TRANSFER) "From Account" else "Payment Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allAccounts.forEach { account ->
                    val isSelected = selectedAccountId == account.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAccountId = account.id },
                        label = { Text(account.name) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Target Account (Only for Transfer)
            if (selectedType == TransactionType.TRANSFER) {
                Text(
                    text = "To Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allAccounts.filter { it.id != selectedAccountId }.forEach { account ->
                        val isSelected = selectedTargetAccountId == account.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTargetAccountId = account.id },
                            label = { Text(account.name) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Date & Time Picker Row
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Date", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateUtil.formatDate(selectedTimestamp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateUtil.formatTime(selectedTimestamp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Notes / Description
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Transaction Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.saveTransaction(
                            id = existingItem?.transaction?.id ?: 0L,
                            type = selectedType,
                            amount = amount,
                            categoryId = if (selectedType == TransactionType.TRANSFER) null else selectedCategoryId,
                            accountId = selectedAccountId,
                            targetAccountId = if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null,
                            note = noteText.trim(),
                            timestamp = selectedTimestamp
                        )
                        onNavigateBack()
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        TransactionType.EXPENSE -> RedExpense
                        TransactionType.INCOME -> GreenLight
                        TransactionType.TRANSFER -> PurpleTransfer
                    }
                )
            ) {
                Text(
                    text = if (existingItem == null) "Save Transaction" else "Update Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
