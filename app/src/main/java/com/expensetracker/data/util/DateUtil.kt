package com.expensetracker.data.util

import com.expensetracker.data.model.TimePeriod
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private val fullDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthYearKeyFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val dayHeaderFormatter = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())

    fun formatDate(timestamp: Long): String = fullDateFormatter.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormatter.format(Date(timestamp))
    fun formatMonthYear(timestamp: Long): String = monthYearFormatter.format(Date(timestamp))
    fun formatMonthYearKey(timestamp: Long): String = monthYearKeyFormatter.format(Date(timestamp))
    fun formatDayHeader(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val isSameDay = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        today.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Today, " + SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
            isYesterday -> "Yesterday, " + SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
            else -> dayHeaderFormatter.format(Date(timestamp))
        }
    }

    fun getTimeRangeForPeriod(period: TimePeriod, referenceTime: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = referenceTime

        return when (period) {
            TimePeriod.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            TimePeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            TimePeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            TimePeriod.YEARLY -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            TimePeriod.ALL -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
