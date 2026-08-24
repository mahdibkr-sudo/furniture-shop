package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.repository.ExpenseRepository
import ir.furnitureshop.app.data.repository.InvoiceRepository
import ir.furnitureshop.app.data.repository.LegacyDebtRepository
import ir.furnitureshop.app.data.repository.PaymentRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.FinancialOverview
import ir.furnitureshop.app.util.FinancialOverviewCalculator
import ir.furnitureshop.app.util.YearFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * «حساب مالی کلی» — ترکیب همه منابع مالی:
 * فاکتورها، پرداخت‌های مشتریان، خریدها، پرداخت‌های تأمین‌کنندگان، هزینه‌های جانبی
 * و بدهی‌های قبلی مشتریان (انتقال‌یافته از سیستم مالی پیشین).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FinancialOverviewViewModel(
    invoiceRepository: InvoiceRepository,
    paymentRepository: PaymentRepository,
    supplierRepository: SupplierRepository,
    expenseRepository: ExpenseRepository,
    legacyDebtRepository: LegacyDebtRepository
) : ViewModel() {

    val uiState: StateFlow<FinancialOverview> =
        YearFilter.selectedYear.flatMapLatest { year ->
            combine(
                combine(
                    invoiceRepository.observeAll(),
                    paymentRepository.observeAll(),
                    supplierRepository.observeAllPurchases()
                ) { invoices, payments, purchases -> Triple(invoices, payments, purchases) },
                combine(
                    supplierRepository.observeAllPayments(),
                    expenseRepository.observeAll(),
                    legacyDebtRepository.observeAll()
                ) { supplierPayments, expenses, legacyDebts -> Triple(supplierPayments, expenses, legacyDebts) }
            ) { (invoices, payments, purchases), (supplierPayments, expenses, legacyDebts) ->
                FinancialOverviewCalculator.calculate(
                    invoices = invoices.filter { YearFilter.isInYear(it.date, year) },
                    payments = payments.filter { YearFilter.isInYear(it.date, year) },
                    purchases = purchases.filter { YearFilter.isInYear(it.date, year) },
                    supplierPayments = supplierPayments.filter { YearFilter.isInYear(it.date, year) },
                    expenses = expenses.filter { YearFilter.isInYear(it.date, year) },
                    legacyDebts = legacyDebts.filter { YearFilter.isInYear(it.date, year) }
                )
            }
        }.catch { e -> emit(FinancialOverview(loadError = e.message ?: "خطا در محاسبه آمار")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialOverview())

    companion object {
        fun factory(
            invoiceRepository: InvoiceRepository,
            paymentRepository: PaymentRepository,
            supplierRepository: SupplierRepository,
            expenseRepository: ExpenseRepository,
            legacyDebtRepository: LegacyDebtRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FinancialOverviewViewModel(
                    invoiceRepository = invoiceRepository,
                    paymentRepository = paymentRepository,
                    supplierRepository = supplierRepository,
                    expenseRepository = expenseRepository,
                    legacyDebtRepository = legacyDebtRepository
                )
            }
        }
    }
}
