package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * تست‌های هزینه‌های جانبی و حساب مالی کلی.
 *
 * تأیید می‌کند:
 * - جمع کل هزینه‌ها درست محاسبه می‌شود
 * - Expense مستقل از Supplier است و Supplier Balance را تغییر نمی‌دهد
 * - «حساب مالی کلی» همه جریان‌ها (فروش، دریافتی، خرید، پرداختی، هزینه) را یکجا نشان می‌دهد
 */
class ExpenseAndOverviewTest {

    @Test
    fun `expenses total is sum of amounts`() {
        val expenses = listOf(
            Expense(id = 1, title = "حمل مواد", amount = 50, date = 0),
            Expense(id = 2, title = "حمل‌ونقل", amount = 30, date = 0),
            Expense(id = 3, title = "هزینه کارگاه", amount = 20, date = 0)
        )
        assertEquals(100L, FinancialOverviewCalculator.expensesTotal(expenses))
        assertEquals(0L, FinancialOverviewCalculator.expensesTotal(emptyList()))
    }

    @Test
    fun `expense does not change supplier balance`() {
        // بدون هزینه: خرید ۱۰۰ ، پرداخت ۶۰ → بدهی ۴۰
        val purchases = listOf(Purchase(id = 1, supplierId = 1, totalAmount = 100))
        val payments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 60, method = "cash")
        )
        val balanceBefore = SupplierBalanceCalculator.balance(purchases, payments)
        assertEquals(40L, balanceBefore)

        // ثبت هزینه‌های جانبی هیچ اثری روی Supplier Balance ندارد
        val expenses = listOf(
            Expense(id = 1, title = "حمل مواد", amount = 500, date = 0),
            Expense(id = 2, title = "حمل‌ونقل", amount = 200, date = 0)
        )
        val balanceAfter = SupplierBalanceCalculator.balance(purchases, payments)
        assertEquals(balanceBefore, balanceAfter)
        assertEquals(40L, balanceAfter)

        // هزینه‌ها فقط در جمع هزینه‌ها دیده می‌شوند
        assertEquals(700L, FinancialOverviewCalculator.expensesTotal(expenses))
    }

    @Test
    fun `financial overview combines all money flows without double counting`() {
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 1_000),
            Invoice(id = 2, customerId = 2, invoiceNumber = "INV-0002", totalAmount = 500)
        )
        val payments = listOf(
            Payment(id = 1, customerId = 1, amount = 400, method = "cash"),
            Payment(id = 2, customerId = 2, amount = 500, method = "check")
        )
        val purchases = listOf(
            Purchase(id = 1, supplierId = 1, totalAmount = 300)
        )
        val supplierPayments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 100, method = "cash"),
            SupplierPayment(id = 2, supplierId = 1, amount = 200, method = "check")
        )
        val expenses = listOf(
            Expense(id = 1, title = "حمل مواد", amount = 80, date = 0)
        )

        val overview = FinancialOverviewCalculator.calculate(
            invoices = invoices,
            payments = payments,
            purchases = purchases,
            supplierPayments = supplierPayments,
            expenses = expenses
        )

        assertEquals(1_500L, overview.totalSales)
        assertEquals(900L, overview.totalReceived)
        assertEquals(300L, overview.totalPurchases)
        assertEquals(300L, overview.totalPaidToSuppliers)
        assertEquals(80L, overview.totalExpenses)

        // نتیجه = فروش − خرید − هزینه = 1500 − 300 − 80
        assertEquals(1_120L, overview.netResult)

        // تراز مشتریان = فروش − دریافتی
        assertEquals(600L, overview.customersBalance)

        // تراز تأمین‌کنندگان = خرید − پرداختی
        assertEquals(0L, overview.suppliersBalance)
    }

    @Test
    fun `check payment counts once in overview via payment only`() {
        // یک چک فقط از طریق Payment دیده می‌شود؛ خود Check در محاسبات نیست
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 1_000)
        )
        val payments = listOf(
            Payment(id = 1, customerId = 1, amount = 1_000, method = "check")
        )
        val overview = FinancialOverviewCalculator.calculate(
            invoices = invoices,
            payments = payments,
            purchases = emptyList(),
            supplierPayments = emptyList(),
            expenses = emptyList()
        )
        assertEquals(1_000L, overview.totalSales)
        assertEquals(1_000L, overview.totalReceived)
        // نتیجه = فروش − خرید − هزینه؛ پرداخت چک فقط روش پرداخت است و نتیجه را کم نمی‌کند
        assertEquals(1_000L, overview.netResult)
        assertEquals(0L, overview.customersBalance)
    }
}
