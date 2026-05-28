package com.example.accounting.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val showAddDialog: Boolean = false,
    val editingCategory: Category? = null,
    val newCategoryName: String = "",
    val newCategoryIcon: String = "more_horiz"
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getByType(_uiState.value.selectedType).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type) }
        viewModelScope.launch {
            categoryRepository.getByType(type).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingCategory = null, newCategoryName = "", newCategoryIcon = "more_horiz") }
    }

    fun showEditDialog(category: Category) {
        _uiState.update { it.copy(showAddDialog = true, editingCategory = category, newCategoryName = category.name, newCategoryIcon = category.icon) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingCategory = null) }
    }

    fun updateNewName(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun updateNewIcon(icon: String) {
        _uiState.update { it.copy(newCategoryIcon = icon) }
    }

    fun saveCategory() {
        val state = _uiState.value
        if (state.newCategoryName.isBlank()) return

        viewModelScope.launch {
            if (state.editingCategory != null) {
                categoryRepository.update(
                    state.editingCategory.copy(name = state.newCategoryName, icon = state.newCategoryIcon)
                )
            } else {
                categoryRepository.insert(
                    Category(name = state.newCategoryName, icon = state.newCategoryIcon, type = state.selectedType)
                )
            }
            dismissDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            // 删除所有非默认分类
            val categories = _uiState.value.categories
            for (category in categories) {
                if (!category.isDefault) {
                    categoryRepository.delete(category)
                }
            }
        }
    }

    fun removeDuplicates() {
        viewModelScope.launch {
            categoryRepository.removeDuplicates()
        }
    }
}
