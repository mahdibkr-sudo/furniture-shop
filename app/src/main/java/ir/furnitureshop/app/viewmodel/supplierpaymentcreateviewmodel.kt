package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.SupplierPayment
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.DigitNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplierPaymentCreateUiState(
    val supplierName: String? = null,
    val amountText: String = "",
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * ثبت پرداخت نقدی به تأمین‌کننده.
 *
 * پرداخت با چک فقط از طریق «پرداخت با چک موجود» (انتقال همان چک) انجام می‌شود —
 * این صفحه فقط نقد ثبت می‌کند تا هیچ Payment چکی بدون Check ثبت نشود.
 */
class SupplierPaymentCreateViewModel(
    private val repository: SupplierRepository,
    private val supplierId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierPaymentCreateUiState())
    val uiState: StateFlow<SupplierPaymentCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val supplier = repository.getById(supplierId)
                _uiState.update { it.copy(supplierName = supplier?.name) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری تأمین‌کننده") }
            }
        }
    }

    fun onAmountChange(raw: String) {
        _uiState.update { it.copy(amountText = DigitNormalizer.digitsOnly(raw), error = null) }
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
                repository.insertPayment(
                    SupplierPayment(
                        supplierId = supplierId,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        method = PaymentMethod.CASH.stored
                    )
                )
                _uiState.update { it.copy(isSaved = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در ثبت پرداخت") }
            }
        }
    }

    companion object {
        fun factory(
            repository: SupplierRepository,
            supplierId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SupplierPaymentCreateViewModel(repository, supplierId)
            }
        }
    }
}
