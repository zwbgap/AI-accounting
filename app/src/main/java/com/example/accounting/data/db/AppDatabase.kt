package com.example.accounting.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.accounting.data.db.dao.BudgetDao
import com.example.accounting.data.db.dao.CategoryDao
import com.example.accounting.data.db.dao.TransactionDao
import com.example.accounting.data.db.entity.Budget
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class, Budget::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "accounting.db"
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).categoryDao().insertAll(defaultCategories())
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).categoryDao()
                            // 清理重复分类
                            val categories = dao.getAllOnce()
                            val grouped = categories.groupBy { "${it.name}_${it.type}" }
                            for ((_, group) in grouped) {
                                if (group.size > 1) {
                                    group.drop(1).forEach { dao.delete(it) }
                                }
                            }
                            // 如果没有分类，插入默认分类
                            if (dao.count() == 0) {
                                dao.insertAll(defaultCategories())
                            }
                        }
                    }
                })
                .build()
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun defaultCategories() = listOf(
            // 支出分类
            Category(name = "餐饮", icon = "restaurant", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "交通", icon = "directions_car", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "购物", icon = "shopping_bag", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "娱乐", icon = "sports_esports", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "居住", icon = "home", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "医疗", icon = "local_hospital", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "教育", icon = "school", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "通讯", icon = "phone", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "服饰", icon = "checkroom", type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "其他", icon = "more_horiz", type = TransactionType.EXPENSE, isDefault = true),
            // 收入分类
            Category(name = "工资", icon = "payments", type = TransactionType.INCOME, isDefault = true),
            Category(name = "奖金", icon = "emoji_events", type = TransactionType.INCOME, isDefault = true),
            Category(name = "投资", icon = "trending_up", type = TransactionType.INCOME, isDefault = true),
            Category(name = "兼职", icon = "work", type = TransactionType.INCOME, isDefault = true),
            Category(name = "其他", icon = "more_horiz", type = TransactionType.INCOME, isDefault = true),
        )
    }
}
