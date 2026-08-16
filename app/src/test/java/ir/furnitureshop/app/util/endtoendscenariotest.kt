package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست End-to-End سناریوی کامل کسب‌وکار (لایه خالص):
 *
 * Customer → Invoice → Payment → Check → Supplier → Purchase → Transfer Check
 * → Expense → Financial Account
 *
 * + Product Price Change → Invoice Historical Price
 * + فاکتور بدون Payment معتبر
 * + عدم Duplicate
 */
class EndToEndScenarioTest {

    @Test
    fun `full business flow customer invoice payment check supplier purchase transfer expense overview`() {
        // ================= مرحله ۱: Customer =================
        val customer = Customer(id = 1, name = "علی رضایی", phone = "09121234567")

        // ================= مرحله ۲: Invoice (۲ محصول + پارچه ما + تخفیف) =================
        // محصولات (قیمت پایه لحظه صدور — Historical Price)
        val sofa = Product(id = 10, name = "مبل راحتی", basePrice = 12_000_000)
        val table = Product(id = 20, name = "میز ناهارخوری", basePrice = 8_000_000)

        // اقلام از snapshot قیمت لحظه صدور
        val cartItems = listOf(
            InvoiceCalculator.cartItemFrom(sofa),        // 12M × 1
            InvoiceCalculator.cartItemFrom(table)        // 8M × 1
        )
        val subtotal = InvoiceCalculator.subtotal(cartItems)
        assertEquals(20_000_000L, subtotal)

        // پارچه: فروشگاه تهیه کرده (۲M) + تخفیف ۱M → مبلغ نهایی
        val total = InvoiceCalculator.total(
            subtotal = subtotal,
            discount = 1_000_000,
            fabricCustomerProvided = false,
            fabricAmount = 2_000_000
        )
        assertEquals(21_000_000L, total)

        val invoice = Invoice(
            id = 1, customerId = customer.id, invoiceNumber = "INV-0001",
            discount = 1_000_000, fabricCustomerProvided = false,
            fabricAmount = 2_000_000, totalAmount = total
        )

        // ================= مرحله ۳: فاکتور بدون Payment معتبر است =================
        val balanceBeforePayment = BalanceCalculator.balance(listOf(invoice), emptyList())
        assertEquals(21_000_000L, balanceBeforePayment)

        // ================= مرحله ۴: Payment نقد (از پروفایل مشتری) =================
        val cashPayment = Payment(customerId = 1, amount = 5_000_000, method = PaymentMethod.CASH.stored)
        // Payment مبلغ Invoice را تغییر نمی‌دهد
        assertEquals(21_000_000L, invoice.totalAmount)
        val balanceAfterCash = BalanceCalculator.balance(listOf(invoice), listOf(cashPayment))
        assertEquals(16_000_000L, balanceAfterCash)

        // ================= مرحله ۵: دریافت Check از مشتری (Payment + Check) =================
        val (checkPayment, check) = CheckFlow.receiveFromCustomer(
            customerId = 1, amount = 10_000_000, checkNumber = "CHK-1001", bank = "ملت", dueDate = null
        )
        assertEquals(PaymentMethod.CHECK.stored, checkPayment.method)
        assertEquals(CheckFlow.HOLDER_SHOP, check.currentHolder)
        assertEquals(CheckStatus.IN_HAND.stored, check.status)
        assertTrue(CheckFlow.canTransfer(check))

        val payments = listOf(cashPayment, checkPayment)
        val customerBalance = BalanceCalculator.balance(listOf(invoice), payments)
        assertEquals(6_000_000L, customerBalance) // 21 − 5 − 10

        // چک تکراری ممنوع است (Duplicate)
        assertTrue(CheckFlow.isDuplicateNumber(listOf("CHK-1001"), "CHK-1001"))
        assertFalse(CheckFlow.isDuplicateNumber(listOf("CHK-1001"), "CHK-1002"))

        // ================= مرحله ۶: Supplier =================
        val supplier = ir.furnitureshop.app.data.entity.Supplier(id = 5, name = "چوب‌فروش", phone = null)

        // ================= مرحله ۷: Purchase (خرید جنسی) ۱۵M =================
        val purchases = listOf(
            Purchase(id = 1, supplierId = 5, totalAmount = 9_000_000),
            Purchase(id = 2, supplierId = 5, totalAmount = 6_000_000)
        )
        val purchasesTotal = SupplierBalanceCalculator.purchasesTotal(purchases)
        assertEquals(15_000_000L, purchasesTotal)
        assertEquals(15_000_000L, SupplierBalanceCalculator.balance(purchases, emptyList()))

        // ================= مرحله ۸: Transfer همان Check به Supplier (اتمیک) =================
        val (transferredCheck, supplierPayment) = CheckFlow.transferToSupplier(check, supplierId = 5)

        // همان چک منتقل شده — نه چک جدید
        assertEquals(check.id, transferredCheck.id)
        assertEquals(check.checkNumber, transferredCheck.checkNumber)
        assertEquals(check.amount, transferredCheck.amount)
        assertEquals("CHK-1001", transferredCheck.checkNumber)
        assertNotEquals(check.currentHolder, transferredCheck.currentHolder)
        assertEquals(CheckFlow.supplierHolder(5), transferredCheck.currentHolder)
        assertEquals(CheckStatus.TRANSFERRED.stored, transferredCheck.status)
        // بعد از انتقال قابل انتقال نیست → دوبار محاسبه نمی‌شود
        assertFalse(CheckFlow.canTransfer(transferredCheck))

        val supplierPayments = listOf(supplierPayment)
        assertEquals(10_000_000L, supplierPayment.amount)
        val supplierBalance = SupplierBalanceCalculator.balance(purchases, supplierPayments)
        assertEquals(5_000_000L, supplierBalance) // 15 − 10

        // ================= مرحله ۹: Expense (مستقل از Supplier) =================
        val expenses = listOf(
            ir.furnitureshop.app.data.entity.Expense(id = 1, title = "حمل مواد", amount = 3_000_000, date = 0)
        )
        // Expense هیچ اثری روی تراز تأمین‌کننده ندارد
        assertEquals(5_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))
        assertEquals(3_000_000L, FinancialOverviewCalculator.expensesTotal(expenses))

        // ================= مرحله ۱۰: Financial Account =================
        val overview = FinancialOverviewCalculator.calculate(
            invoices = listOf(invoice),
            payments = payments,
            purchases = purchases,
            supplierPayments = supplierPayments,
            expenses = expenses
        )
        assertEquals(21_000_000L, overview.totalSales)
        assertEquals(15_000_000L, overview.totalReceived)
        assertEquals(15_000_000L, overview.totalPurchases)
        assertEquals(15_000_000L, overview.purchasesGoods)
        assertEquals(10_000_000L, overview.totalPaidToSuppliers)
        assertEquals(3_000_000L, overview.totalExpenses)
        assertEquals(6_000_000L, overview.customersBalance)
        assertEquals(5_000_000L, overview.suppliersBalance)
        // نتیجه = 21 − 15 − 3
        assertEquals(3_000_000L, overview.netResult)

        // چک دوبار محاسبه نشده: دریافتی فقط از Payment ها (۵+۱۰) و
        // پرداختی تأمین‌کننده فقط از SupplierPayment (۱۰) — خود چک جایی اضافه نشده
        assertEquals(15_000_000L, overview.totalReceived)
        assertEquals(10_000_000L, overview.totalPaidToSuppliers)
    }

    @Test
    fun `product price change does not affect old invoice`() {
        // محصول الان گران‌تر شده (۱۲M → ۱۵M)
        val productNow = Product(id = 10, name = "مبل راحتی", basePrice = 15_000_000)

        // فاکتور قدیمی: snapshot قیمت ۱۲M در InvoiceItem ذخیره شده
        val oldItem = InvoiceItem(
            id = 1, invoiceId = 1, productId = 10,
            productName = "مبل راحتی", quantity = 1, unitPrice = 12_000_000
        )
        val oldInvoice = Invoice(
            id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 12_000_000
        )

        // فاکتور جدید: از قیمت جدید محصول snapshot می‌گیرد
        val newItem = InvoiceCalculator.cartItemFrom(productNow)

        // خروجی فاکتور قدیمی از snapshot خودش می‌خواند — نه قیمت فعلی محصول
        val printData = InvoicePrintFormatter.build(oldInvoice, listOf(oldItem), Customer(id = 1, name = "علی"))
        assertEquals(12_000_000L, printData.items[0].unitPrice)
        assertEquals(12_000_000L, printData.items[0].itemTotal)
        assertEquals(12_000_000L, printData.totalAmount)

        // فاکتور جدید قیمت جدید را دارد
        assertEquals(15_000_000L, newItem.unitPrice)
        // Balance فاکتور قدیمی هم دست‌نخورده
        assertEquals(12_000_000L, BalanceCalculator.balance(listOf(oldInvoice), emptyList()))
    }

    @Test
    fun `fabric and discount apply only within their own invoice`() {
        // فاکتور ۱: پارچه ما (۲M) + تخفیف ۱M
        val total1 = InvoiceCalculator.total(20_000_000, 1_000_000, fabricCustomerProvided = false, fabricAmount = 2_000_000)
        assertEquals(21_000_000L, total1)

        // فاکتور ۲ (همان مشتری): بدون پارچه و بدون تخفیف — مستقل از فاکتور ۱
        val total2 = InvoiceCalculator.total(8_000_000, 0, fabricCustomerProvided = false, fabricAmount = 0)
        assertEquals(8_000_000L, total2)

        // پارچه مشتری‌آورده در فاکتور ۳: هیچ مبلغی اضافه نمی‌شود
        val total3 = InvoiceCalculator.total(10_000_000, 0, fabricCustomerProvided = true, fabricAmount = 5_000_000)
        assertEquals(10_000_000L, total3)

        // خروجی چاپی هر فاکتور فقط اطلاعات خودش را دارد
        val inv1 = Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", discount = 1_000_000, fabricCustomerProvided = false, fabricAmount = 2_000_000, totalAmount = total1)
        val inv2 = Invoice(id = 2, customerId = 1, invoiceNumber = "INV-0002", totalAmount = total2)
        val inv3 = Invoice(id = 3, customerId = 1, invoiceNumber = "INV-0003", fabricCustomerProvided = true, fabricAmount = 5_000_000, totalAmount = total3)

        val data1 = InvoicePrintFormatter.build(inv1, listOf(InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل", quantity = 1, unitPrice = 12_000_000)), null)
        val data2 = InvoicePrintFormatter.build(inv2, listOf(InvoiceItem(id = 2, invoiceId = 2, productId = 20, productName = "میز", quantity = 2, unitPrice = 4_000_000)), null)
        val data3 = InvoicePrintFormatter.build(inv3, listOf(InvoiceItem(id = 3, invoiceId = 3, productId = 10, productName = "مبل", quantity = 1, unitPrice = 10_000_000)), null)

        assertTrue(data1.fabricLine!!.contains("۲٬۰۰۰٬۰۰۰"))
        assertNull(data2.fabricLine)
        assertNull(data3.fabricLine) // مشتری آورده → نمایش داده نمی‌شود
        assertEquals(8_000_000L, data2.totalAmount) // تخفیف فاکتور ۱ به اینجا نشت نکرده
    }
}
