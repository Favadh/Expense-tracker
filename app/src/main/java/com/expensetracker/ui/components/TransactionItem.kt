package com.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.data.model.TransactionType
import com.expensetracker.data.model.TransactionWithDetails
import com.expensetracker.data.util.CategoryIcons
import com.expensetracker.data.util.CurrencyFormatter
import com.expensetracker.data.util.DateUtil
import com.expensetracker.ui.theme.*

@Composable
fun TransactionItem(
    item: TransactionWithDetails,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    val category = item.category
    val account = item.account
    val targetAccount = item.targetAccount

    val isExpense = tx.type == TransactionType.EXPENSE
    val isTransfer = tx.type == TransactionType.TRANSFER

    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#2196F3"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val amountPrefix = when (tx.type) {
        TransactionType.EXPENSE -> "-"
        TransactionType.INCOME -> "+"
        TransactionType.TRANSFER -> "⇄ "
    }

    val amountColor = when (tx.type) {
        TransactionType.EXPENSE -> RedExpense
        TransactionType.INCOME -> GreenLight
        TransactionType.TRANSFER -> PurpleTransfer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTransfer) PurpleTransfer.copy(alpha = 0.15f)
                        else categoryColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isTransfer) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Transfer",
                        tint = PurpleTransfer,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = CategoryIcons.getIcon(category?.iconName),
                        contentDescription = category?.name ?: "Category",
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column (Title, Account, Note)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val title = when (tx.type) {
                    TransactionType.TRANSFER -> "Transfer: ${account?.name ?: "Account"} → ${targetAccount?.name ?: "Account"}"
                    else -> if (tx.note.isNotBlank()) tx.note else category?.name ?: "General"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isTransfer && category != null && tx.note.isNotBlank()) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Text(
                        text = if (isTransfer) "Transfer" else account?.name ?: "Cash",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Amount & Time Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$amountPrefix${CurrencyFormatter.formatCompact(tx.amount, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = DateUtil.formatTime(tx.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}
