package com.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.expensetracker.data.model.DebtEntity
import com.expensetracker.data.model.DebtType
import com.expensetracker.data.util.CurrencyFormatter
import com.expensetracker.data.util.DateUtil
import com.expensetracker.ui.components.AddDebtDialog
import com.expensetracker.ui.components.ConfirmDeleteDialog
import com.expensetracker.ui.theme.GreenLight
import com.expensetracker.ui.theme.RedExpense
import com.expensetracker.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val allDebts by viewModel.allDebts.collectAsState()
    val totalLent by viewModel.totalLentUnsettled.collectAsState()
    val totalBorrowed by viewModel.totalBorrowedUnsettled.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var selectedTab by remember { mutableStateOf(DebtType.LENT) }
    var showAddDialog by remember { mutableStateOf(false) }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }

    val filteredDebts = remember(allDebts, selectedTab) {
        allDebts.filter { it.type == selectedTab }
    }

    if (showAddDialog) {
        AddDebtDialog(
            initialType = selectedTab,
            onSave = { person, amount, type, note ->
                viewModel.addDebt(person, amount, type, note)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    debtToDelete?.let { debt ->
        ConfirmDeleteDialog(
            title = "Delete Record",
            message = "Are you sure you want to delete the debt record for ${debt.personName}?",
            onConfirm = {
                viewModel.deleteDebt(debt)
                debtToDelete = null
            },
            onDismiss = { debtToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debt & Loan Tracker",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Debt",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // You'll Get (Lent)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "You'll Get",
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = CurrencyFormatter.formatCompact(totalLent, currencySymbol),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GreenLight
                            )
                        }
                    }

                    // You'll Give (Borrowed)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RedExpense.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "You'll Give",
                                style = MaterialTheme.typography.labelMedium,
                                color = RedExpense,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = CurrencyFormatter.formatCompact(totalBorrowed, currencySymbol),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RedExpense
                            )
                        }
                    }
                }
            }

            // Tab Selector (Lent vs Borrowed)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == DebtType.LENT) GreenLight else Color.Transparent)
                            .clickable { selectedTab = DebtType.LENT }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You'll Get (Lent)",
                            fontWeight = if (selectedTab == DebtType.LENT) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == DebtType.LENT) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == DebtType.BORROWED) RedExpense else Color.Transparent)
                            .clickable { selectedTab = DebtType.BORROWED }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You'll Give (Borrowed)",
                            fontWeight = if (selectedTab == DebtType.BORROWED) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == DebtType.BORROWED) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Debt List
            if (filteredDebts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No ${if (selectedTab == DebtType.LENT) "lent" else "borrowed"} records found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredDebts, key = { it.id }) { debt ->
                    val isLent = debt.type == DebtType.LENT
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (debt.isSettled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Settle Checkbox
                            Checkbox(
                                checked = debt.isSettled,
                                onCheckedChange = { viewModel.toggleDebtSettled(debt) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Person & Note
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = debt.personName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (debt.isSettled) TextDecoration.LineThrough else TextDecoration.None
                                )

                                if (debt.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = debt.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Date: ${DateUtil.formatDate(debt.createdAt)}" +
                                            if (debt.isSettled) " • Settled" else " • Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (debt.isSettled) GreenLight else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            }

                            // Amount & Delete
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatCompact(debt.amount, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (debt.isSettled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    else if (isLent) GreenLight else RedExpense,
                                    textDecoration = if (debt.isSettled) TextDecoration.LineThrough else TextDecoration.None
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                IconButton(
                                    onClick = { debtToDelete = debt },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
