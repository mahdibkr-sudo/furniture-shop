package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.repository.ExpenseRepository
import ir.furnitureshop.app.util.FinancialOverviewCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

data class ExpenseListUiState(
    val loadError: String? = null,
    val expenses: List<Expense> = emptyList(),
    val totalAmount: Long = 0
)

/** لیست هزینه‌های جانبی + جمع کل */
class ExpenseListViewModel(repository: ExpenseRepository) : ViewModel() {

    val uiState: StateFlow<ExpenseListUiState> = repository.observeAll()
        .map { expenses ->
            ExpenseListUiState(
                expenses = expenses,
                totalAmount = FinancialOverviewCalculator.expensesTotal(expenses)
            )
        }
        .catch { e -> emit(ExpenseListUiState(loadError = e.message ?: "خطا در بارگذاری هزینه‌ها")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseListUiState())

    companion object {
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseListViewModel(repository) }
        }
    }
}
