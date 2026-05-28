package com.example.accounting.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.ai.AiSettingsManager
import com.example.accounting.ai.voice.TextAiParser
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.InputSource
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.CategoryRepository
import com.example.accounting.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val isParsing: Boolean = false,
    val inputText: String = "",
    val parsedAmount: String = "",
    val parsedCategory: String = "",
    val parsedNote: String = "",
    val parsedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val showResult: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val aiSettingsManager: AiSettingsManager
) : ViewModel() {

    private val parser = TextAiParser(aiSettingsManager)

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private var allCategories: List<Category> = emptyList()

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                allCategories = categories
                // 默认显示支出分类
                val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
                _uiState.update { it.copy(categories = expenseCategories) }
            }
        }
    }

    private fun updateCategoriesForType(type: TransactionType) {
        val filtered = allCategories.filter { it.type == type }
        _uiState.update { it.copy(categories = filtered) }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(parsedAmount = amount, errorMessage = null) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(parsedNote = note) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, parsedCategory = category.name) }
    }

    private fun matchCategory(categoryName: String) {
        val categories = _uiState.value.categories
        val matched = categories.find { it.name == categoryName }
            ?: categories.find { it.name == "其他" }
        _uiState.update { it.copy(selectedCategory = matched) }
    }

    fun parseFromText() {
        val input = _uiState.value.inputText
        if (input.isBlank()) return

        _uiState.update { it.copy(isParsing = true, showResult = false) }

        viewModelScope.launch {
            parser.parse(input).fold(
                onSuccess = { result ->
                    val type = if (result.type == "income") TransactionType.INCOME else TransactionType.EXPENSE
                    updateCategoriesForType(type)
                    _uiState.update {
                        it.copy(
                            isParsing = false,
                            parsedAmount = result.amount.toString(),
                            parsedCategory = result.category,
                            parsedNote = result.note,
                            parsedType = type,
                            showResult = true
                        )
                    }
                    matchCategory(result.category)
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isParsing = false,
                            parsedCategory = "其他",
                            parsedNote = input,
                            showResult = true,
                            errorMessage = "解析失败，请手动填写"
                        )
                    }
                    matchCategory("其他")
                }
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.parsedAmount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效金额") }
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = state.parsedType,
                categoryId = state.selectedCategory?.id,
                categoryName = state.selectedCategory?.name ?: state.parsedCategory.ifEmpty { "其他" },
                note = state.parsedNote,
                date = System.currentTimeMillis(),
                source = InputSource.TEXT_AI
            )
            transactionRepository.insert(transaction)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
