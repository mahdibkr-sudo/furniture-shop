package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.LegacyDebt
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.LegacyDebtRepository
import ir.furnitureshop.app.util.BalanceCalculator
import ir.furnitureshop.app.util.YearFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val invoices: List<Invoice> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val checks: List<Check> = emptyList(),
    val legacyDebts: List<LegacyDebt> = emptyList(),
    val totalPurchases: Long = 0,
    val totalPaid: Long = 0,
    val totalLegacyDebts: Long = 0,
    val error: String? = null
) {
    /** مثبت = بدهی، منفی = بستانکاری، صفر = تسویه */
    val balance: Long get() = totalPurchases + totalLegacyDebts - totalPaid
}

/**
 * پروفایل مشتری — اطلاعات، فاکتورها، پرداخت‌ها و تراز مالی.
 * تراز = Σ فاکتورها + Σ بدهی‌های قبلی − Σ پرداخت‌ها (نقد و چک هر دو یک‌بار شمرده می‌شوند).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerProfileViewModel(
    private val repository: CustomerRepository,
    checkRepository: CheckRepository,
    legacyDebtRepository: LegacyDebtRepository,
    private val customerId: Long
) : ViewModel() {

    val uiState: StateFlow<CustomerProfileUiState> =
        YearFilter.selectedYear.flatMapLatest { year ->
            combine(
                combine(
                    repository.observeById(customerId),
                    repository.invoicesFor(customerId),
                    repository.paymentsFor(customerId)
                ) { customer, invoices, payments -> Triple(customer, invoices, payments) },
                combine(
                    checkRepository.observeByCustomer(customerId),
                    legacyDebtRepository.observeByCustomer(customerId)
                ) { checks, legacyDebts -> Pair(checks, legacyDebts) }
            ) { (customer, invoices, payments), (checks, legacyDebts) ->
                val yearInvoices = invoices.filter { YearFilter.isInYear(it.date, year) }
                val yearPayments = payments.filter { YearFilter.isInYear(it.date, year) }
                val yearChecks = checks.filter { YearFilter.isInYear(it.receiveDate, year) }
                val yearLegacyDebts = legacyDebts.filter { YearFilter.isInYear(it.date, year) }
                CustomerProfileUiState(
                    customer = customer,
                    invoices = yearInvoices,
                    payments = yearPayments,
                    checks = yearChecks,
                    legacyDebts = yearLegacyDebts,
                    totalPurchases = BalanceCalculator.purchasesTotal(yearInvoices),
                    totalPaid = BalanceCalculator.paymentsTotal(yearPayments),
                    totalLegacyDebts = BalanceCalculator.legacyDebtsTotal(yearLegacyDebts)
                )
            }
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
            legacyDebtRepository: LegacyDebtRepository,
            customerId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CustomerProfileViewModel(repository, checkRepository, legacyDebtRepository, customerId) }
        }
    }
}
