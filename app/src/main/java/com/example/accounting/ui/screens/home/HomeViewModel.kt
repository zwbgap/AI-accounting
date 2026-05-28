package com.example.accounting.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import com.example.accounting.data.repository.TransactionRepository
import com.example.accounting.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val todayTransactions: List<Transaction> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val monthRange = DateUtils.getCurrentMonthStartEnd()
    private val todayRange = DateUtils.getTodayStartEnd()

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.getTotalByDateRangeAndType(monthRange.first, monthRange.second, TransactionType.INCOME),
        transactionRepository.getTotalByDateRangeAndType(monthRange.first, monthRange.second, TransactionType.EXPENSE),
        transactionRepository.getByDateRange(todayRange.first, todayRange.second),
        transactionRepository.getRecent(20)
    ) { income, expense, today, recent ->
        HomeUiState(
            monthIncome = income ?: 0.0,
            monthExpense = expense ?: 0.0,
            todayTransactions = today,
            recentTransactions = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }
}
