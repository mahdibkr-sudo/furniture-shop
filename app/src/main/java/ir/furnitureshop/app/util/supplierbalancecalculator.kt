package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment

/**
 * تراز مالی تأمین‌کننده — دقیق و بدون Double Counting.
 *
 * قوانین:
 * - هر Purchase (خرید جنسی) یک‌بار بدهی ایجاد می‌کند (مبلغ کالایی که گرفته‌ایم)
 * - هر SupplierPayment یک‌بار بدهی را کاهش می‌دهد — صرف‌نظر از روش نقد/چک
 * - چک فقط روش پرداخت است؛ مبلغ اضافه‌ای ایجاد نمی‌کند
 *
 * balance = Σ خرید جنسی − Σ پرداخت‌ها
 * (مثبت = بدهی ما به تأمین‌کننده، منفی = بستانکاری ما)
 */
object SupplierBalanceCalculator {

    /** مبلغ خرید جنسی = جمع مبالغ کالاهایی که از تأمین‌کننده گرفته‌ایم */
    fun purchasesTotal(purchases: List<Purchase>): Long =
        purchases.sumOf { it.totalAmount }

    fun paymentsTotal(payments: List<SupplierPayment>): Long =
        payments.sumOf { it.amount }

    fun balance(purchases: List<Purchase>, payments: List<SupplierPayment>): Long =
        purchasesTotal(purchases) - paymentsTotal(payments)
}
