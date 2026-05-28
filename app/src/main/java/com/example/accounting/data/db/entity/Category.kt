package com.example.accounting.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val type: TransactionType,
    val isDefault: Boolean = false
)

enum class TransactionType { INCOME, EXPENSE }

enum class InputSource { MANUAL, OCR, VOICE, TEXT_AI }
