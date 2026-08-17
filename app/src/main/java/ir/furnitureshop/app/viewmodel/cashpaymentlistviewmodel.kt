package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.dao.PaymentWithCustomer
import ir.furnitureshop.app.data.repository.PaymentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CashPaymentListUiState(
    val loadError: String? = null,
    val payments: List<PaymentWithCustomer> = emptyList()
) {
    /** آیا این ردیف واقعاً نقد است؟ (دفاع دوم در برابر داده ناسازگار) */
    fun isCash(payment: PaymentWithCustomer): Boolean =
        payment.payment.method == ir.furnitureshop.app.data.entity.PaymentMethod.CASH.stored
}

class CashPaymentListViewModel(
    paymentRepository: PaymentRepository
) : ViewModel() {

    val uiState: StateFlow<CashPaymentListUiState> =
        paymentRepository.observeAllCashWithCustomer()
            .map { payments -> CashPaymentListUiState(payments = payments) }
            .catch { e -> emit(CashPaymentListUiState(loadError = e.message ?: "خطا در بارگذاری پرداخت‌های نقدی")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CashPaymentListUiState())

    companion object {
        fun factory(
            paymentRepository: PaymentRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CashPaymentListViewModel(paymentRepository) }
        }
    }
}
