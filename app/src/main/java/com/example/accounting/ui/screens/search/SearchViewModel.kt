package com.example.accounting.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Transaction> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()

    init {
        viewModelScope.launch {
            transactionRepository.getAll().collect { transactions ->
                allTransactions = transactions
                // 如果有搜索词，重新过滤
                if (_uiState.value.query.isNotBlank()) {
                    search(_uiState.value.query)
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
        } else {
            search(query)
        }
    }

    private fun search(query: String) {
        val lowerQuery = query.lowercase()
        val results = allTransactions.filter { transaction ->
            transaction.note.lowercase().contains(lowerQuery) ||
                    transaction.categoryName.lowercase().contains(lowerQuery) ||
                    transaction.amount.toString().contains(query)
        }
        _uiState.update { it.copy(results = results, isSearching = false) }
    }
}
