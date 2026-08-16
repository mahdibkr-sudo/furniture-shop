package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.ExpenseRepository
import ir.furnitureshop.app.data.repository.InvoiceRepository
import ir.furnitureshop.app.data.repository.PaymentRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.FinancialOverview
import ir.furnitureshop.app.util.FinancialOverviewCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/**
 * «حساب مالی کلی» — ترکیب همه منابع مالی:
 * فاکتورها، پرداخت‌های مشتریان، خریدها، پرداخت‌های تأمین‌کنندگان و هزینه‌های جانبی.
 */
class FinancialOverviewViewModel(
    invoiceRepository: InvoiceRepository,
    paymentRepository: PaymentRepository,
    supplierRepository: SupplierRepository,
    expenseRepository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<FinancialOverview> =
        combine(
            invoiceRepository.observeAll(),
            paymentRepository.observeAll(),
            supplierRepository.observeAllPurchases(),
            supplierRepository.observeAllPayments(),
            expenseRepository.observeAll()
        ) { invoices, payments, purchases, supplierPayments, expenses ->
            FinancialOverviewCalculator.calculate(
                invoices = invoices,
                payments = payments,
                purchases = purchases,
                supplierPayments = supplierPayments,
                expenses = expenses
            )
        }.catch { e -> emit(FinancialOverview(loadError = e.message ?: "خطا در محاسبه آمار")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialOverview())

    companion object {
        fun factory(
            invoiceRepository: InvoiceRepository,
            paymentRepository: PaymentRepository,
            supplierRepository: SupplierRepository,
            expenseRepository: ExpenseRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FinancialOverviewViewModel(
                    invoiceRepository = invoiceRepository,
                    paymentRepository = paymentRepository,
                    supplierRepository = supplierRepository,
                    expenseRepository = expenseRepository
                )
            }
        }
    }
}
