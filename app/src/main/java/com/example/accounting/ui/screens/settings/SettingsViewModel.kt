package com.example.accounting.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounting.data.repository.TransactionRepository
import com.example.accounting.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val exportResult: Result<String>? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportData(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            try {
                val transactions = transactionRepository.getAll().first()
                val result = CsvExporter.export(context, transactions)
                _uiState.update { it.copy(isExporting = false, exportResult = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportResult = Result.failure(e)
                    )
                }
            }
        }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }
}
