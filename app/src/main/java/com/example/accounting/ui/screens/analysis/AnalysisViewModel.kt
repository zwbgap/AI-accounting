package com.example.accounting.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.dao.CategoryTotal
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.TransactionRepository
import com.example.accounting.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

enum class TimePeriod { WEEK, MONTH, YEAR }

data class AnalysisUiState(
    val period: TimePeriod = TimePeriod.MONTH,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun updatePeriod(period: TimePeriod) {
        _uiState.update { it.copy(period = period) }
        loadData()
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

    private fun loadData() {
        val state = _uiState.value
        val (startDate, endDate) = when (state.period) {
            TimePeriod.MONTH -> DateUtils.getMonthStartEnd(state.year, state.month)
            TimePeriod.WEEK -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, state.year)
                cal.set(Calendar.MONTH, state.month - 1)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 7)
                cal.add(Calendar.MILLISECOND, -1)
                start to cal.timeInMillis
            }
            TimePeriod.YEAR -> {
                val cal = Calendar.getInstance()
                cal.set(state.year, 0, 1, 0, 0, 0)
                val start = cal.timeInMillis
                cal.set(state.year + 1, 0, 1, 0, 0, 0)
                cal.add(Calendar.MILLISECOND, -1)
                start to cal.timeInMillis
            }
        }

        // 收入
        transactionRepository.getTotalByDateRangeAndType(startDate, endDate, TransactionType.INCOME)
            .onEach { income -> _uiState.update { it.copy(totalIncome = income ?: 0.0) } }
            .launchIn(viewModelScope)

        // 支出
        transactionRepository.getTotalByDateRangeAndType(startDate, endDate, TransactionType.EXPENSE)
            .onEach { expense -> _uiState.update { it.copy(totalExpense = expense ?: 0.0) } }
            .launchIn(viewModelScope)

        // 分类统计（默认显示支出分类）
        transactionRepository.getCategoryTotals(startDate, endDate, TransactionType.EXPENSE)
            .onEach { totals -> _uiState.update { it.copy(categoryTotals = totals) } }
            .launchIn(viewModelScope)
    }
}
