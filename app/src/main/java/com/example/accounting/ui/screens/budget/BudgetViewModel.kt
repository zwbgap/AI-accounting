package com.example.accounting.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.dao.CategoryTotal
import com.example.accounting.data.db.entity.Budget
import com.example.accounting.data.db.entity.BudgetPeriod
import com.example.accounting.data.db.entity.Category
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.BudgetRepository
import com.example.accounting.data.repository.CategoryRepository
import com.example.accounting.data.repository.TransactionRepository
import com.example.accounting.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BudgetItem(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentage: Double
)

data class BudgetUiState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val totalBudget: Budget? = null,
    val totalSpent: Double = 0.0,
    val categoryBudgets: List<BudgetItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingBudget: Budget? = null,
    val newBudgetAmount: String = "",
    val selectedCategory: Category? = null,
    val isTotalBudget: Boolean = true
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        val (startDate, endDate) = DateUtils.getMonthStartEnd(state.year, state.month)

        // 加载分类
        viewModelScope.launch {
            categoryRepository.getByType(TransactionType.EXPENSE).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        // 加载总预算
        viewModelScope.launch {
            budgetRepository.getTotalBudget(state.year, state.month).collect { budget ->
                _uiState.update { it.copy(totalBudget = budget) }
            }
        }

        // 加载本月总支出
        viewModelScope.launch {
            transactionRepository.getTotalByDateRangeAndType(startDate, endDate, TransactionType.EXPENSE)
                .collect { total ->
                    _uiState.update { it.copy(totalSpent = total ?: 0.0) }
                }
        }

        // 加载分类预算
        viewModelScope.launch {
            budgetRepository.getByMonth(state.year, state.month).collect { budgets ->
                val categoryBudgets = budgets.filter { it.categoryId != null }.map { budget ->
                    // 这里简化处理，实际应该查询每个分类的支出
                    BudgetItem(
                        budget = budget,
                        spent = 0.0, // 需要单独查询
                        remaining = budget.amount,
                        percentage = 0.0
                    )
                }
                _uiState.update { it.copy(categoryBudgets = categoryBudgets) }
            }
        }
    }

    fun previousMonth() {
        val state = _uiState.value
        val newMonth = if (state.month == 1) 12 else state.month - 1
        val newYear = if (state.month == 1) state.year - 1 else state.year
        _uiState.update { it.copy(year = newYear, month = newMonth) }
        loadData()
    }

    fun nextMonth() {
        val state = _uiState.value
        val newMonth = if (state.month == 12) 1 else state.month + 1
        val newYear = if (state.month == 12) state.year + 1 else state.year
        _uiState.update { it.copy(year = newYear, month = newMonth) }
        loadData()
    }

    fun showAddDialog(isTotal: Boolean) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                isTotalBudget = isTotal,
                newBudgetAmount = "",
                selectedCategory = null,
                editingBudget = null
            )
        }
    }

    fun showEditDialog(budget: Budget) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingBudget = budget,
                newBudgetAmount = budget.amount.toString(),
                isTotalBudget = budget.categoryId == null,
                selectedCategory = _uiState.value.categories.find { it.id == budget.categoryId }
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingBudget = null) }
    }

    fun updateAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(newBudgetAmount = amount) }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun saveBudget() {
        val state = _uiState.value
        val amount = state.newBudgetAmount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            val budget = Budget(
                id = state.editingBudget?.id ?: 0,
                categoryId = if (state.isTotalBudget) null else state.selectedCategory?.id,
                categoryName = if (state.isTotalBudget) "" else state.selectedCategory?.name ?: "",
                amount = amount,
                period = BudgetPeriod.MONTHLY,
                year = state.year,
                month = state.month
            )

            if (state.editingBudget != null) {
                budgetRepository.update(budget)
            } else {
                budgetRepository.insert(budget)
            }

            dismissDialog()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
        }
    }
}
