package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.LegacyDebt
import ir.furnitureshop.app.data.entity.Payment

/**
 * تراز مالی مشتری — دقیق و بدون Double Counting.
 *
 * قوانین:
 * - هر Invoice یک‌بار بدهی ایجاد می‌کند (totalAmount)
 * - هر LegacyDebt بدهی قبلی (سیستم مالی پیشین) بدون فاکتور اضافه می‌کند
 * - هر Payment یک‌بار بدهی را کاهش می‌دهد (amount) — صرف‌نظر از روش نقد/چک
 * - چک فقط روش پرداخت است؛ مبلغ اضافه‌ای ایجاد نمی‌کند
 * - Payment مبلغ Invoice را تغییر نمی‌دهد
 *
 * balance = Σ invoices.totalAmount + Σ legacy_debts.amount − Σ payments.amount
 * (مثبت = بدهی، منفی = بستانکار، صفر = تسویه)
 */
object BalanceCalculator {

    fun purchasesTotal(invoices: List<Invoice>): Long =
        invoices.sumOf { it.totalAmount }

    fun legacyDebtsTotal(debts: List<LegacyDebt>): Long =
        debts.sumOf { it.amount }

    fun paymentsTotal(payments: List<Payment>): Long =
        payments.sumOf { it.amount }

    fun balance(invoices: List<Invoice>, payments: List<Payment>): Long =
        purchasesTotal(invoices) - paymentsTotal(payments)

    fun balance(invoices: List<Invoice>, debts: List<LegacyDebt>, payments: List<Payment>): Long =
        purchasesTotal(invoices) + legacyDebtsTotal(debts) - paymentsTotal(payments)
}
