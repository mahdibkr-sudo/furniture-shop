package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.util.BalanceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val invoices: List<Invoice> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val checks: List<Check> = emptyList(),
    val totalPurchases: Long = 0,
    val totalPaid: Long = 0,
    val error: String? = null
) {
    /** مثبت = بدهی، منفی = بستانکاری، صفر = تسویه */
    val balance: Long get() = totalPurchases - totalPaid
}

/**
 * پروفایل مشتری — اطلاعات، فاکتورها، پرداخت‌ها و تراز مالی.
 * تراز = Σ فاکتورها − Σ پرداخت‌ها (نقد و چک هر دو یک‌بار شمرده می‌شوند).
 * چک‌های دریافتی در Task بعدی به این بخش متصل می‌شوند.
 */
class CustomerProfileViewModel(
    private val repository: CustomerRepository,
    checkRepository: CheckRepository,
    private val customerId: Long
) : ViewModel() {

    val uiState: StateFlow<CustomerProfileUiState> =
        combine(
            repository.observeById(customerId),
            repository.invoicesFor(customerId),
            repository.paymentsFor(customerId),
            checkRepository.observeByCustomer(customerId)
        ) { customer, invoices, payments, checks ->
            CustomerProfileUiState(
                customer = customer,
                invoices = invoices,
                payments = payments,
                checks = checks,
                totalPurchases = BalanceCalculator.purchasesTotal(invoices),
                totalPaid = BalanceCalculator.paymentsTotal(payments)
            )
        }
            .catch { e ->
                // به جای کرش برنامه، خطا نمایش داده می‌شود تا قابل تشخیص باشد
                emit(CustomerProfileUiState(error = e.message ?: "خطا در بارگذاری پروفایل"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerProfileUiState())

    companion object {
        fun factory(
            repository: CustomerRepository,
            checkRepository: CheckRepository,
            customerId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CustomerProfileViewModel(repository, checkRepository, customerId) }
        }
    }
}
