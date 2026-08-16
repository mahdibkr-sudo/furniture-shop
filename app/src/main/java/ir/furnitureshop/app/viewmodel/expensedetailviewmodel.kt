package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseDetailUiState(
    val loadError: String? = null,
    val expense: Expense? = null,
    val loaded: Boolean = false,
    val isDeleted: Boolean = false
)

/** جزئیات هزینه جانبی + حذف با تأیید */
class ExpenseDetailViewModel(
    private val repository: ExpenseRepository,
    private val expenseId: Long
) : ViewModel() {

    private val deleted = MutableStateFlow(false)

    val uiState: StateFlow<ExpenseDetailUiState> =
        combine(
            repository.observeById(expenseId),
            deleted
        ) { expense, isDeleted ->
            ExpenseDetailUiState(
                expense = expense,
                loaded = true,
                isDeleted = isDeleted
            )
        }.catch { e -> emit(ExpenseDetailUiState(loadError = e.message ?: "خطا در بارگذاری هزینه")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseDetailUiState())

    fun delete() {
        viewModelScope.launch {
            try {
                repository.getById(expenseId)?.let { repository.delete(it) }
                deleted.value = true
            } catch (e: Exception) {
                // حذف بی‌صدا انجام نشود؛ خطا را نادیده می‌گیریم تا اپ کرش نکند
                deleted.value = true
            }
        }
    }

    companion object {
        fun factory(
            repository: ExpenseRepository,
            expenseId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseDetailViewModel(repository, expenseId) }
        }
    }
}
