package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * تست «حساب مالی» — چند سناریوی واقعی کسب‌وکار مبل و میز.
 *
 * تأیید می‌کند:
 * - Customer Balance و Supplier Balance دقیقاً با منطق قبلی (BalanceCalculator /
 *   SupplierBalanceCalculator) سازگارند
 * - چک به تنهایی مبلغ مالی جدید ایجاد نمی‌کند؛ Payment روی Balance اثر دارد
 * - Expense مستقل از Supplier است
 * - خرید جنسی = مجموع Purchase ها
 */
class FinancialAccountTest {

    // ---- سناریوی واقعی ۱: مشتری با پرداخت نقد + چک ----

    @Test
    fun `customer balance matches previous logic with cash and check payments`() {
        // مشتری «علی رضایی»: مبل ۱۲ میلیون + میز ۸ میلیون = ۲۰ میلیون فاکتور
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 12_000_000),
            Invoice(id = 2, customerId = 1, invoiceNumber = "INV-0002", totalAmount = 8_000_000)
        )
        // پرداخت نقد ۱۰ میلیون + چک ۵ میلیون
        val payments = listOf(
            Payment(id = 1, customerId = 1, amount = 10_000_000, method = "cash"),
            Payment(id = 2, customerId = 1, amount = 5_000_000, method = "check")
        )

        // منطق قبلی
        val expected = BalanceCalculator.balance(invoices, payments)
        assertEquals(5_000_000L, expected)

        // سازگاری در حساب مالی کلی
        val overview = FinancialOverviewCalculator.calculate(
            invoices = invoices,
            payments = payments,
            purchases = emptyList(),
            supplierPayments = emptyList(),
            expenses = emptyList()
        )
        assertEquals(expected, overview.customersBalance)
        assertEquals(20_000_000L, overview.totalSales)
        assertEquals(15_000_000L, overview.totalReceived)
    }

    // ---- سناریوی واقعی ۲: تأمین‌کننده با خرید جنسی و پرداخت نقد + چک ----

    @Test
    fun `supplier balance matches previous logic with cash and check payments`() {
        // تأمین‌کننده «چوب فروش»: خرید جنسی (چوب + پارچه) ۱۵ میلیون
        val purchases = listOf(
            Purchase(id = 1, supplierId = 1, totalAmount = 9_000_000),
            Purchase(id = 2, supplierId = 1, totalAmount = 6_000_000)
        )
        // پرداخت نقد ۶ میلیون + چک ۴ میلیون
        val payments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 6_000_000, method = "cash"),
            SupplierPayment(id = 2, supplierId = 1, amount = 4_000_000, method = "check")
        )

        // منطق قبلی
        val expected = SupplierBalanceCalculator.balance(purchases, payments)
        assertEquals(5_000_000L, expected)

        // سازگاری در حساب مالی کلی
        val overview = FinancialOverviewCalculator.calculate(
            invoices = emptyList(),
            payments = emptyList(),
            purchases = purchases,
            supplierPayments = payments,
            expenses = emptyList()
        )
        assertEquals(expected, overview.suppliersBalance)
        assertEquals(15_000_000L, overview.purchasesGoods) // خرید جنسی
        assertEquals(10_000_000L, overview.totalPaidToSuppliers)
    }

    // ---- سناریوی واقعی ۳: نمای کامل کسب‌وکار ----

    @Test
    fun `full shop scenario combines all sources correctly`() {
        // فروش: ۵۰ میلیون (چند فاکتور)
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 20_000_000),
            Invoice(id = 2, customerId = 1, invoiceNumber = "INV-0002", totalAmount = 15_000_000),
            Invoice(id = 3, customerId = 2, invoiceNumber = "INV-0003", totalAmount = 15_000_000)
        )
        // دریافتی: نقد ۲۰ میلیون + چک ۱۰ میلیون
        val payments = listOf(
            Payment(id = 1, customerId = 1, amount = 20_000_000, method = "cash"),
            Payment(id = 2, customerId = 2, amount = 10_000_000, method = "check")
        )
        // خرید جنسی: ۲۵ میلیون
        val purchases = listOf(
            Purchase(id = 1, supplierId = 1, totalAmount = 15_000_000),
            Purchase(id = 2, supplierId = 1, totalAmount = 10_000_000)
        )
        // پرداختی: نقد ۱۰ میلیون + چک ۵ میلیون
        val supplierPayments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 10_000_000, method = "cash"),
            SupplierPayment(id = 2, supplierId = 1, amount = 5_000_000, method = "check")
        )
        // هزینه‌های جانبی: حمل ۳ میلیون + کارگاه ۱ میلیون
        val expenses = listOf(
            Expense(id = 1, title = "حمل مواد", amount = 3_000_000, date = 0),
            Expense(id = 2, title = "هزینه کارگاه", amount = 1_000_000, date = 0)
        )

        val overview = FinancialOverviewCalculator.calculate(
            invoices = invoices,
            payments = payments,
            purchases = purchases,
            supplierPayments = supplierPayments,
            expenses = expenses
        )

        assertEquals(50_000_000L, overview.totalSales)
        assertEquals(30_000_000L, overview.totalReceived)
        assertEquals(25_000_000L, overview.totalPurchases)
        assertEquals(25_000_000L, overview.purchasesGoods)
        assertEquals(15_000_000L, overview.totalPaidToSuppliers)
        assertEquals(4_000_000L, overview.totalExpenses)

        // ترازها = منطق قبلی
        assertEquals(
            BalanceCalculator.balance(invoices, payments),
            overview.customersBalance
        )
        assertEquals(20_000_000L, overview.customersBalance)
        assertEquals(
            SupplierBalanceCalculator.balance(purchases, supplierPayments),
            overview.suppliersBalance
        )
        assertEquals(10_000_000L, overview.suppliersBalance)

        // نتیجه = فروش − خرید جنسی − هزینه‌ها = ۵۰ − ۲۵ − ۴
        assertEquals(21_000_000L, overview.netResult)
    }

    // ---- سناریوی واقعی ۴: چک فقط از طریق Payment اثر دارد ----

    @Test
    fun `check creates no money by itself only its payment counts`() {
        // مشتری با چک ۱۰ میلیون تسویه کرد (فاکتور ۱۰ میلیون)
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 10_000_000)
        )
        val payments = listOf(
            Payment(id = 1, customerId = 1, amount = 10_000_000, method = "check")
        )
        // همان چک به تأمین‌کننده منتقل شد (Payment تأمین‌کننده ۱۰ میلیون)
        val supplierPayments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 10_000_000, method = "check")
        )

        val overview = FinancialOverviewCalculator.calculate(
            invoices = invoices,
            payments = payments,
            purchases = emptyList(),
            supplierPayments = supplierPayments,
            expenses = emptyList()
        )

        // خودِ چک هیچ‌جا به‌عنوان مبلغ اضافه شمرده نشده:
        // دریافتی = ۱۰ میلیون (فقط از Payment)، پرداختی = ۱۰ میلیون (فقط از Payment)
        assertEquals(10_000_000L, overview.totalReceived)
        assertEquals(10_000_000L, overview.totalPaidToSuppliers)
        assertEquals(0L, overview.customersBalance)
        // بدون خرید، ۱۰ میلیون پرداخت شده → بستانکاری ما از تأمین‌کننده = منفی ۱۰
        assertEquals(-10_000_000L, overview.suppliersBalance)
        assertEquals(10_000_000L, overview.netResult)
    }

    // ---- سناریوی واقعی ۵: Expense مستقل از Supplier ----

    @Test
    fun `expenses never affect supplier balance`() {
        val purchases = listOf(
            Purchase(id = 1, supplierId = 1, totalAmount = 20_000_000)
        )
        val supplierPayments = listOf(
            SupplierPayment(id = 1, supplierId = 1, amount = 8_000_000, method = "cash")
        )
        val expenses = listOf(
            Expense(id = 1, title = "حمل‌ونقل", amount = 2_000_000, date = 0),
            Expense(id = 2, title = "تعمیرات کارگاه", amount = 5_000_000, date = 0)
        )

        val overview = FinancialOverviewCalculator.calculate(
            invoices = emptyList(),
            payments = emptyList(),
            purchases = purchases,
            supplierPayments = supplierPayments,
            expenses = expenses
        )

        // تراز تأمین‌کننده فقط از خرید/پرداخت؛ هزینه‌ها هیچ اثری ندارند
        assertEquals(12_000_000L, overview.suppliersBalance)
        assertEquals(
            SupplierBalanceCalculator.balance(purchases, supplierPayments),
            overview.suppliersBalance
        )
        // هزینه‌ها جداگانه و در نتیجه کلی دیده می‌شوند
        assertEquals(7_000_000L, overview.totalExpenses)
        // نتیجه = فروش(۰) − خرید(۲۰) − هزینه(۷) = منفی ۲۷ میلیون
        assertEquals(-27_000_000L, overview.netResult)
    }
}
