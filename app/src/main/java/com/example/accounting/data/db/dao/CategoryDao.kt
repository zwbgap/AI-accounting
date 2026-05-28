package com.example.accounting.data.db.dao

import androidx.room.*
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY type, id")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id")
    fun getByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllOnce(): List<Category>
}
