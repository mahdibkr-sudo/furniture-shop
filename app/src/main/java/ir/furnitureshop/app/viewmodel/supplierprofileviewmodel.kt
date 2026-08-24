package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.entity.SupplierPayment
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.SupplierBalanceCalculator
import ir.furnitureshop.app.util.YearFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SupplierProfileUiState(
    val loadError: String? = null,
    val supplier: Supplier? = null,
    val purchases: List<Purchase> = emptyList(),
    val payments: List<SupplierPayment> = emptyList(),
    val checks: List<Check> = emptyList(),
    val totalPurchases: Long = 0,
    val totalPaid: Long = 0
) {
    /** مبلغ خرید جنسی = جمع مبالغ کالاهای گرفته‌شده */
    val purchasesGoods: Long get() = totalPurchases

    /** مثبت = بدهی ما به تأمین‌کننده، منفی = بستانکاری ما، صفر = تسویه */
    val balance: Long get() = totalPurchases - totalPaid
}

/**
 * پروفایل تأمین‌کننده — اطلاعات، خریدها (جنسی)، پرداخت‌ها، چک‌های منتقل‌شده و تراز.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SupplierProfileViewModel(
    private val repository: SupplierRepository,
    checkRepository: CheckRepository,
    private val supplierId: Long
) : ViewModel() {

    val uiState: StateFlow<SupplierProfileUiState> =
        YearFilter.selectedYear.flatMapLatest { year ->
            combine(
                repository.observeById(supplierId),
                repository.purchasesFor(supplierId),
                repository.paymentsFor(supplierId),
                checkRepository.observeBySupplier(supplierId)
            ) { supplier, purchases, payments, checks ->
                val yearPurchases = purchases.filter { YearFilter.isInYear(it.date, year) }
                val yearPayments = payments.filter { YearFilter.isInYear(it.date, year) }
                SupplierProfileUiState(
                    supplier = supplier,
                    purchases = yearPurchases,
                    payments = yearPayments,
                    checks = checks.filter { YearFilter.isInYear(it.receiveDate, year) },
                    totalPurchases = SupplierBalanceCalculator.purchasesTotal(yearPurchases),
                    totalPaid = SupplierBalanceCalculator.paymentsTotal(yearPayments)
                )
            }
        }.catch { e -> emit(SupplierProfileUiState(loadError = e.message ?: "خطا در بارگذاری پروفایل تأمین‌کننده")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupplierProfileUiState())

    companion object {
        fun factory(
            repository: SupplierRepository,
            checkRepository: CheckRepository,
            supplierId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SupplierProfileViewModel(repository, checkRepository, supplierId) }
        }
    }
}
