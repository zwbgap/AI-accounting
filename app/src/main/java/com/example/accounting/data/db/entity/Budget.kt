package com.example.accounting.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long? = null,       // null 表示总预算
    val categoryName: String = "",      // 冗余存储，方便显示
    val amount: Double,                 // 预算金额
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val year: Int,
    val month: Int,                     // 月度预算用
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod { MONTHLY, WEEKLY, YEARLY }
