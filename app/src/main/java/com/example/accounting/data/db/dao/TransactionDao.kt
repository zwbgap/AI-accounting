package com.example.accounting.data.db.dao

import androidx.room.*
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = :type ORDER BY date DESC")
    fun getByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = :type")
    fun getTotalByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<Double?>

    @Query("SELECT categoryName, SUM(amount) as total FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = :type GROUP BY categoryName ORDER BY total DESC")
    fun getCategoryTotals(startDate: Long, endDate: Long, type: TransactionType): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Transaction>>
}

data class CategoryTotal(
    val categoryName: String,
    val total: Double
)
