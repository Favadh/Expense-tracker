package com.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.data.util.CurrencyFormatter
import com.expensetracker.ui.theme.GreenLight
import com.expensetracker.ui.theme.RedExpense
import kotlin.math.max

data class MonthlyBarData(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

@Composable
fun BarChartView(
    dataList: List<MonthlyBarData>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (dataList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No transaction data available for comparison",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    val maxAmount = max(1.0, dataList.maxOfOrNull { max(it.income, it.expense) } ?: 1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GreenLight)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Income", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(RedExpense)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Expense", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barGroupWidth = totalWidth / dataList.size
            val barWidth = (barGroupWidth * 0.28f).coerceAtMost(30.dp.toPx())

            dataList.forEachIndexed { index, item ->
                val groupCenterX = index * barGroupWidth + (barGroupWidth / 2)

                val incomeBarHeight = ((item.income / maxAmount) * (totalHeight - 30.dp.toPx())).toFloat()
                val expenseBarHeight = ((item.expense / maxAmount) * (totalHeight - 30.dp.toPx())).toFloat()

                // Income bar (left)
                val incomeLeft = groupCenterX - barWidth - 2.dp.toPx()
                val incomeTop = totalHeight - 20.dp.toPx() - incomeBarHeight
                drawRoundRect(
                    color = GreenLight,
                    topLeft = Offset(incomeLeft, incomeTop),
                    size = Size(barWidth, incomeBarHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Expense bar (right)
                val expenseLeft = groupCenterX + 2.dp.toPx()
                val expenseTop = totalHeight - 20.dp.toPx() - expenseBarHeight
                drawRoundRect(
                    color = RedExpense,
                    topLeft = Offset(expenseLeft, expenseTop),
                    size = Size(barWidth, expenseBarHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataList.forEach { item ->
                Text(
                    text = item.monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
