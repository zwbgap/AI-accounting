package com.example.accounting.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
    private val dayFormat = SimpleDateFormat("MM月dd日", Locale.CHINA)

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatMonth(timestamp: Long): String = monthFormat.format(Date(timestamp))

    fun formatDay(timestamp: Long): String = dayFormat.format(Date(timestamp))

    fun getMonthStartEnd(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis
        return start to end
    }

    fun getCurrentMonthStartEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return getMonthStartEnd(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun getTodayStartEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis
        return start to end
    }

    fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            "¥${amount.toLong()}"
        } else {
            String.format("¥%.2f", amount)
        }
    }
}
