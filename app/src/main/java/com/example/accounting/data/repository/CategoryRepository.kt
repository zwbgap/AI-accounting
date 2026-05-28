package com.example.accounting.data.repository

import com.example.accounting.data.db.dao.CategoryDao
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    fun getByType(type: TransactionType): Flow<List<Category>> = categoryDao.getByType(type)

    suspend fun getById(id: Long): Category? = categoryDao.getById(id)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)

    suspend fun removeDuplicates() {
        val categories = categoryDao.getAllOnce()
        val grouped = categories.groupBy { "${it.name}_${it.type}" }
        for ((_, group) in grouped) {
            if (group.size > 1) {
                // 保留第一个，删除其余的
                group.drop(1).forEach { categoryDao.delete(it) }
            }
        }
    }
}
