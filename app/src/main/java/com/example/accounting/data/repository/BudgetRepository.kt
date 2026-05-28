package com.example.accounting.data.repository

import com.example.accounting.data.db.dao.BudgetDao
import com.example.accounting.data.db.entity.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getByMonth(year: Int, month: Int): Flow<List<Budget>> = budgetDao.getByMonth(year, month)

    fun getTotalBudget(year: Int, month: Int): Flow<Budget?> = budgetDao.getTotalBudget(year, month)

    fun getCategoryBudget(year: Int, month: Int, categoryId: Long): Flow<Budget?> =
        budgetDao.getCategoryBudget(year, month, categoryId)

    suspend fun getById(id: Long): Budget? = budgetDao.getById(id)

    suspend fun insert(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun update(budget: Budget) = budgetDao.update(budget)

    suspend fun delete(budget: Budget) = budgetDao.delete(budget)
}
