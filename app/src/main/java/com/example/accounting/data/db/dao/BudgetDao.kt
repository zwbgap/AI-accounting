package com.example.accounting.data.db.dao

import androidx.room.*
import com.example.accounting.data.db.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month ORDER BY categoryId")
    fun getByMonth(year: Int, month: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND categoryId IS NULL LIMIT 1")
    fun getTotalBudget(year: Int, month: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND categoryId = :categoryId LIMIT 1")
    fun getCategoryBudget(year: Int, month: Int, categoryId: Long): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): Budget?
}
