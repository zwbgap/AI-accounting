package com.example.accounting.data.repository

import com.example.accounting.data.db.dao.CategoryTotal
import com.example.accounting.data.db.dao.TransactionDao
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAll(): Flow<List<Transaction>> = transactionDao.getAll()

    fun getRecent(limit: Int = 10): Flow<List<Transaction>> = transactionDao.getRecent(limit)

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getByDateRange(startDate, endDate)

    fun getByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getByDateRangeAndType(startDate, endDate, type)

    fun getTotalByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<Double?> =
        transactionDao.getTotalByDateRangeAndType(startDate, endDate, type)

    fun getCategoryTotals(startDate: Long, endDate: Long, type: TransactionType): Flow<List<CategoryTotal>> =
        transactionDao.getCategoryTotals(startDate, endDate, type)

    suspend fun getById(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)
}
