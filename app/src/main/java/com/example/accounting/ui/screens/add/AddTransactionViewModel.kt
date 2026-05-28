package com.example.accounting.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.InputSource
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.CategoryRepository
import com.example.accounting.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val date: Long = System.currentTimeMillis(),
    val isEditing: Boolean = false,
    val editId: Long = 0,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getByType(_uiState.value.type).collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories,
                        selectedCategory = state.selectedCategory ?: categories.firstOrNull()
                    )
                }
            }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getById(id) ?: return@launch
            val category = transaction.categoryId?.let { categoryRepository.getById(it) }
            _uiState.update {
                it.copy(
                    amount = transaction.amount.toString(),
                    note = transaction.note,
                    type = transaction.type,
                    selectedCategory = category,
                    date = transaction.date,
                    isEditing = true,
                    editId = id
                )
            }
            // 重新加载对应类型的分类
            categoryRepository.getByType(transaction.type).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun updateAmount(amount: String) {
        // 只允许数字和小数点
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = amount, errorMessage = null) }
        }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(type = type, selectedCategory = null) }
        viewModelScope.launch {
            categoryRepository.getByType(type).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效金额") }
            return
        }

        // 确保有分类
        val categoryName = state.selectedCategory?.name ?: "其他"

        viewModelScope.launch {
            val transaction = Transaction(
                id = if (state.isEditing) state.editId else 0,
                amount = amount,
                type = state.type,
                categoryId = state.selectedCategory?.id,
                categoryName = categoryName,
                note = state.note,
                date = state.date,
                source = InputSource.MANUAL
            )

            if (state.isEditing) {
                transactionRepository.update(transaction)
            } else {
                transactionRepository.insert(transaction)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun saveFromAI(amount: Double, categoryName: String, note: String, source: InputSource) {
        viewModelScope.launch {
            // 查找匹配的分类
            val categories = _uiState.value.categories
            val category = categories.find { it.name == categoryName }

            val transaction = Transaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                categoryId = category?.id,
                categoryName = categoryName.ifEmpty { "其他" },
                note = note,
                date = System.currentTimeMillis(),
                source = source
            )
            transactionRepository.insert(transaction)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
