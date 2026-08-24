package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.LegacyDebt
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment

/**
 * «حساب مالی» — وضعیت مالی کلی کسب‌وکار از روی Entity های موجود.
 *
 * هیچ سیستم حسابداری جدیدی ساخته نمی‌شود؛ فقط داده‌های فعلی جمع می‌شوند:
 * - فروش = Σ فاکتورها (Invoice)
 * - دریافتی از مشتریان = Σ Payment ها (نقد و چک)
 * - خرید جنسی = Σ Purchase ها (خرید از تأمین‌کننده)
 * - پرداختی به تأمین‌کنندگان = Σ SupplierPayment ها (نقد و چک)
 * - هزینه‌های جانبی = Σ Expense ها (مستقل از Supplier)
 *
 * قوانین:
 * - Check به تنهایی مبلغ مالی جدید ایجاد نمی‌کند؛ Payment روی Balance اثر دارد.
 * - Customer Balance با `BalanceCalculator` (منطق قبلی تست‌شده) سازگار است.
 * - Supplier Balance با `SupplierBalanceCalculator` (منطق قبلی تست‌شده) سازگار است.
 * - Expense مستقل از Supplier است و تراز تأمین‌کننده را تغییر نمی‌دهد.
 */
data class FinancialOverview(
    val loadError: String? = null,
    val totalSales: Long = 0,
    val totalReceived: Long = 0,
    val totalPurchases: Long = 0,
    val totalPaidToSuppliers: Long = 0,
    val totalExpenses: Long = 0,
    val totalLegacyDebts: Long = 0
) {
    /** مبلغ خرید جنسی = جمع خریدها از تأمین‌کنندگان */
    val purchasesGoods: Long get() = totalPurchases

    /** تراز مشتریان = بدهی آن‌ها به ما + بدهی‌های قبلی (منطق BalanceCalculator) */
    val customersBalance: Long get() = totalSales + totalLegacyDebts - totalReceived

    /** تراز تأمین‌کنندگان = بدهی ما به آن‌ها (منطق قبلی SupplierBalanceCalculator) */
    val suppliersBalance: Long get() = totalPurchases - totalPaidToSuppliers

    /** سود/زیان ساده: فروش − خرید جنسی − هزینه‌های جانبی */
    val netResult: Long get() = totalSales - totalPurchases - totalExpenses
}

object FinancialOverviewCalculator {

    fun calculate(
        invoices: List<Invoice>,
        payments: List<Payment>,
        purchases: List<Purchase>,
        supplierPayments: List<SupplierPayment>,
        expenses: List<Expense>,
        legacyDebts: List<LegacyDebt> = emptyList()
    ): FinancialOverview = FinancialOverview(
        totalSales = BalanceCalculator.purchasesTotal(invoices),
        totalReceived = BalanceCalculator.paymentsTotal(payments),
        totalPurchases = SupplierBalanceCalculator.purchasesTotal(purchases),
        totalPaidToSuppliers = SupplierBalanceCalculator.paymentsTotal(supplierPayments),
        totalExpenses = expenses.sumOf { it.amount },
        totalLegacyDebts = BalanceCalculator.legacyDebtsTotal(legacyDebts)
    )

    /** جمع کل هزینه‌های جانبی */
    fun expensesTotal(expenses: List<Expense>): Long = expenses.sumOf { it.amount }
}
