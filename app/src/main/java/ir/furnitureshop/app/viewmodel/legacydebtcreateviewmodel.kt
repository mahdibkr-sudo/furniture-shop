package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.LegacyDebt
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.LegacyDebtRepository
import ir.furnitureshop.app.util.DigitNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LegacyDebtCreateUiState(
    val customerName: String? = null,
    val amountText: String = "",
    val description: String = "",
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * ثبت بدهی قبلی مشتری (از سیستم مالی پیشین) — بدون فاکتور.
 * فقط مبلغ + توضیح اختیاری؛ مستقیماً بدهی مشتری را افزایش می‌دهد.
 */
class LegacyDebtCreateViewModel(
    private val legacyDebtRepository: LegacyDebtRepository,
    customerRepository: CustomerRepository,
    private val customerId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegacyDebtCreateUiState())
    val uiState: StateFlow<LegacyDebtCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val customer = customerRepository.getById(customerId)
                _uiState.update { it.copy(customerName = customer?.name) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری مشتری") }
            }
        }
    }

    fun onAmountChange(raw: String) {
        _uiState.update { it.copy(amountText = DigitNormalizer.digitsOnly(raw), error = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: 0L
        if (amount <= 0) {
            _uiState.update { it.copy(error = "مبلغ معتبری وارد کنید") }
            return
        }
        viewModelScope.launch {
            try {
                legacyDebtRepository.insert(
                    LegacyDebt(
                        customerId = customerId,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        description = state.description.trim().ifEmpty { null }
                    )
                )
                _uiState.update { it.copy(isSaved = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در ثبت بدهی") }
            }
        }
    }

    companion object {
        fun factory(
            legacyDebtRepository: LegacyDebtRepository,
            customerRepository: CustomerRepository,
            customerId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LegacyDebtCreateViewModel(
                    legacyDebtRepository = legacyDebtRepository,
                    customerRepository = customerRepository,
                    customerId = customerId
                )
            }
        }
    }
}
