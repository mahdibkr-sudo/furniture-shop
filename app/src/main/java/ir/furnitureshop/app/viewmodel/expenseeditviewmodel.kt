package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseEditUiState(
    val title: String = "",
    val amountText: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * افزودن / ویرایش هزینه جانبی.
 * برای ویرایش، expenseId > 0 است و مقادیر فعلی بارگذاری می‌شوند.
 */
class ExpenseEditViewModel(
    private val repository: ExpenseRepository,
    private val expenseId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseEditUiState())
    val uiState: StateFlow<ExpenseEditUiState> = _uiState.asStateFlow()

    init {
        if (expenseId > 0) {
            viewModelScope.launch {
                try {
                    repository.getById(expenseId)?.let { expense ->
                        _uiState.update {
                            it.copy(
                                title = expense.title,
                                amountText = expense.amount.toString(),
                                date = expense.date,
                                description = expense.description ?: ""
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری هزینه") }
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }

    fun onAmountChange(value: String) {
        val digits = value.filter { ch -> ch.isDigit() }
        _uiState.update { it.copy(amountText = digits) }
    }

    fun onDateChange(millis: Long?) {
        millis?.let { m -> _uiState.update { state -> state.copy(date = m) } }
    }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val title = state.title.trim()
        val amount = state.amountText.toLongOrNull() ?: 0

        when {
            title.isEmpty() -> {
                _uiState.update { it.copy(error = "عنوان هزینه را وارد کنید.") }
                return
            }
            amount <= 0 -> {
                _uiState.update { it.copy(error = "مبلغ باید بیشتر از صفر باشد.") }
                return
            }
        }

        viewModelScope.launch {
            try {
                val description = state.description.trim().ifEmpty { null }
                if (expenseId > 0) {
                    repository.update(
                        Expense(
                            id = expenseId,
                            title = title,
                            amount = amount,
                            date = state.date,
                            description = description
                        )
                    )
                } else {
                    repository.insert(
                        Expense(
                            title = title,
                            amount = amount,
                            date = state.date,
                            description = description
                        )
                    )
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در ذخیره هزینه") }
            }
        }
    }

    companion object {
        fun factory(
            repository: ExpenseRepository,
            expenseId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseEditViewModel(repository, expenseId) }
        }
    }
}
