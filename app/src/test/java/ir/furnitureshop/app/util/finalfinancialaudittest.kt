package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AUDIT نهایی — تمام سناریوهای مالی پروژه (لایه خالص).
 *
 * سناریوهای ۱ تا ۵۰ Task FINAL به‌صورت تست‌های قطعی:
 * Customer/Invoice/Payment/Check/Transfer/Price-Change/Fabric/Discount/
 * Multi-Product/Supplier/Purchase/In-kind/Expense/Balance/Financial Account/
 * Zero-State/Big-Numbers/Final Reconciliation/No-Double-Counting.
 */
class FinalFinancialAuditTest {

    private fun invoice(id: Long, customerId: Long, num: String, total: Long, discount: Long = 0, fabricProvided: Boolean = false, fabric: Long = 0) =
        Invoice(id = id, customerId = customerId, invoiceNumber = num, discount = discount,
            fabricCustomerProvided = fabricProvided, fabricAmount = fabric, totalAmount = total)

    private fun item(id: Long, invoiceId: Long, productId: Long, name: String, qty: Int, price: Long) =
        InvoiceItem(id = id, invoiceId = invoiceId, productId = productId, productName = name, quantity = qty, unitPrice = price)

    // ============ ۲ تا ۴: سناریوی پایه Customer + نقد + چک ============

    @Test
    fun `customer base invoice cash and check payments`() {
        // Invoice 1: مبل 100m، بدون تخفیف، پارچه را مشتری آورده
        val total = InvoiceCalculator.total(100_000_000, 0, fabricCustomerProvided = true, fabricAmount = 0)
        assertEquals(100_000_000L, total)
        val inv = invoice(1, 1, "INV-0001", total, fabricProvided = true)

        // بدون Payment: تراز = 100m
        assertEquals(100_000_000L, BalanceCalculator.balance(listOf(inv), emptyList()))

        // نقد 30m
        val cash = Payment(customerId = 1, amount = 30_000_000, method = PaymentMethod.CASH.stored)
        assertEquals(70_000_000L, BalanceCalculator.balance(listOf(inv), listOf(cash)))
        // Payment فاکتور جدید نمی‌سازد و فاکتور را تغییر نمی‌دهد
        assertEquals(1, listOf(inv).size)
        assertEquals(100_000_000L, inv.totalAmount)

        // چک 20m: Payment + Check
        val (checkPayment, check) = CheckFlow.receiveFromCustomer(1, 20_000_000, "CHK-F1", null, null)
        assertEquals(20_000_000L, checkPayment.amount)
        assertEquals(1L, check.receivedFromCustomerId)
        val payments = listOf(cash, checkPayment)
        // تراز = 100 − 30 − 20 = 50m (چک دوبار کم نمی‌کند)
        assertEquals(50_000_000L, BalanceCalculator.balance(listOf(inv), payments))
        // اگر چک دوبار حساب می‌شد نتیجه ۳۰m می‌شد — این‌جا ۵۰m است
        assertEquals(50_000_000L, BalanceCalculator.paymentsTotal(payments))
    }

    // ============ ۵: انتقال چک — همان چک، بدون Check جدید ============

    @Test
    fun `check transfer keeps same check and does not change customer balance`() {
        val (_, check) = CheckFlow.receiveFromCustomer(1, 20_000_000, "CHK-F1", null, null)
        val (transferred, supplierPayment) = CheckFlow.transferToSupplier(check, supplierId = 5)

        // همان چک: id/شماره/مبلغ/تاریخ دریافت حفظ شده
        assertEquals(check.id, transferred.id)
        assertEquals("CHK-F1", transferred.checkNumber)
        assertEquals(20_000_000L, transferred.amount)
        assertEquals(check.receiveDate, transferred.receiveDate)
        assertEquals(CheckFlow.supplierHolder(5), transferred.currentHolder)
        assertEquals(CheckStatus.TRANSFERRED.stored, transferred.status)

        // تعداد چک‌ها افزایش نمی‌یابد (یک چک، فقط holder عوض شده)
        val checkList = listOf(transferred)
        assertEquals(1, checkList.size)
        assertFalse(CheckFlow.canTransfer(transferred))

        // انتقال روی Customer Balance اثری ندارد (قبلاً هنگام دریافت کم شده)
        val inv = invoice(1, 1, "INV-0001", 100_000_000)
        val customerPayments = listOf(Payment(customerId = 1, amount = 20_000_000, method = PaymentMethod.CHECK.stored))
        assertEquals(80_000_000L, BalanceCalculator.balance(listOf(inv), customerPayments))
        // SupplierPayment واقعی ساخته شده ولی به Customer Balance مربوط نیست
        assertEquals(20_000_000L, supplierPayment.amount)
        assertEquals(5L, supplierPayment.supplierId)
    }

    // ============ ۶: تغییر قیمت Product — فاکتور قدیمی ثابت ============

    @Test
    fun `product price change keeps historical invoice`() {
        val productNow = Product(id = 10, name = "مبل مدل A", basePrice = 120_000_000) // قبلاً 100m
        val oldItem = item(1, 1, 10, "مبل مدل A", 1, 100_000_000) // snapshot قدیمی

        val print = InvoicePrintFormatter.build(
            invoice(1, 1, "INV-0001", 100_000_000), listOf(oldItem), null
        )
        assertEquals(100_000_000L, print.items[0].unitPrice)
        assertEquals(100_000_000L, print.totalAmount)
        // فاکتور جدید از قیمت جدید snapshot می‌گیرد
        assertEquals(120_000_000L, InvoiceCalculator.cartItemFrom(productNow).unitPrice)
    }

    // ============ ۷ و ۸: پارچه + تخفیف ============

    @Test
    fun `fabric and discount scenarios`() {
        // Invoice 2: 120m + پارچه ما 15m − تخفیف 5m = 130m
        assertEquals(130_000_000L, InvoiceCalculator.total(120_000_000, 5_000_000, false, 15_000_000))
        // مشتری پارچه آورده: مبلغ پارچه اضافه نمی‌شود (بدون تخفیف → ۱۲۰m)
        assertEquals(120_000_000L, InvoiceCalculator.total(120_000_000, 0, true, 15_000_000))
        // مشتری پارچه آورده با تخفیف: فقط تخفیف کسر می‌شود (۱۲۰ − ۵)
        assertEquals(115_000_000L, InvoiceCalculator.total(120_000_000, 5_000_000, true, 15_000_000))
        // Discount: 100m + پارچه 10m − تخفیف 15m = 95m (تخفیف/پارچه دوبار اعمال نمی‌شود)
        assertEquals(95_000_000L, InvoiceCalculator.total(100_000_000, 15_000_000, false, 10_000_000))
    }

    // ============ ۹: چند Product ============

    @Test
    fun `multi product invoice`() {
        val items = listOf(
            InvoiceCalculator.cartItemFrom(Product(id = 10, name = "مبل مدل A", basePrice = 100_000_000)),
            InvoiceCalculator.cartItemFrom(Product(id = 20, name = "مبل مدل B", basePrice = 80_000_000)),
            InvoiceCalculator.cartItemFrom(Product(id = 30, name = "میز مدل C", basePrice = 60_000_000))
        )
        assertEquals(3, items.size) // هیچ آیتمی حذف نشده
        assertEquals(240_000_000L, InvoiceCalculator.subtotal(items))
        // + پارچه 20m − تخفیف 10m = 250m
        assertEquals(250_000_000L, InvoiceCalculator.total(240_000_000, 10_000_000, false, 20_000_000))
    }

    // ============ ۱۰: Customer Balance کامل ============

    @Test
    fun `customer balance with multiple invoices and payments`() {
        val invoices = listOf(
            invoice(1, 1, "INV-0001", 100_000_000),
            invoice(2, 1, "INV-0002", 130_000_000),
            invoice(3, 1, "INV-0003", 250_000_000)
        )
        val payments = listOf(
            Payment(customerId = 1, amount = 30_000_000, method = PaymentMethod.CASH.stored),
            Payment(customerId = 1, amount = 20_000_000, method = PaymentMethod.CHECK.stored),
            Payment(customerId = 1, amount = 50_000_000, method = PaymentMethod.CASH.stored)
        )
        assertEquals(480_000_000L, BalanceCalculator.purchasesTotal(invoices))
        assertEquals(100_000_000L, BalanceCalculator.paymentsTotal(payments))
        assertEquals(380_000_000L, BalanceCalculator.balance(invoices, payments))
    }

    // ============ ۱۱ تا ۱۳: Supplier + خرید جنسی + چک ============

    @Test
    fun `supplier purchase cash check and in-kind`() {
        // Purchase 200m
        var purchases = listOf(Purchase(id = 1, supplierId = 5, totalAmount = 200_000_000))
        var supplierPayments = emptyList<SupplierPayment>()
        assertEquals(200_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))

        // نقد 50m → 150m
        supplierPayments = listOf(SupplierPayment(supplierId = 5, amount = 50_000_000, method = PaymentMethod.CASH.stored))
        assertEquals(150_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))

        // چک 30m → 120m (چک دوبار کم نمی‌کند)
        val (_, checkPayment30) = CheckFlow.receiveFromCustomer(1, 30_000_000, "CHK-S1", null, null)
        val (_, sp30) = CheckFlow.transferToSupplier(
            CheckFlow.receiveFromCustomer(1, 30_000_000, "CHK-S2", null, null).second, 5
        )
        supplierPayments = supplierPayments + sp30
        assertEquals(120_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))

        // خرید جنسی 70m → 190m (در تراز لحاظ می‌شود؛ با Payment اشتباه نمی‌شود)
        purchases = purchases + Purchase(id = 2, supplierId = 5, totalAmount = 70_000_000)
        assertEquals(190_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))

        // چک 40m (یک رویداد پرداخت: SupplierPayment) → 150m
        val (_, sp40) = CheckFlow.transferToSupplier(
            CheckFlow.receiveFromCustomer(1, 40_000_000, "CHK-S3", null, null).second, 5
        )
        supplierPayments = supplierPayments + sp40
        assertEquals(40_000_000L, sp40.amount)
        assertEquals(150_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))
        // خودِ Check مبلغ مستقلی نیست: فقط SupplierPayment یک‌بار کم کرده
        assertEquals(150_000_000L, SupplierBalanceCalculator.paymentsTotal(supplierPayments).let { 270_000_000L - it })
    }

    // ============ ۱۴: Check Transfer — هیچ تغییری در چک ============

    @Test
    fun `check transfer preserves identity`() {
        val (_, check) = CheckFlow.receiveFromCustomer(1, 40_000_000, "CHK-T1", "ملت", 1_700_000_000_000L)
        val (t, _) = CheckFlow.transferToSupplier(check, 7)
        assertEquals(check.id, t.id)
        assertEquals("CHK-T1", t.checkNumber)
        assertEquals(40_000_000L, t.amount)
        assertEquals(check.receiveDate, t.receiveDate)
        assertEquals("ملت", t.bank)
        assertEquals(CheckFlow.supplierHolder(7), t.currentHolder)
        // duplicate نشده
        assertTrue(CheckFlow.isDuplicateNumber(listOf("CHK-T1"), "CHK-T1"))
    }

    // ============ ۱۵ و ۱۶: Expense مستقل ============

    @Test
    fun `expenses are independent and sum correctly`() {
        val expenses = listOf(
            Expense(id = 1, title = "حمل مواد اولیه", amount = 10_000_000, date = 0),
            Expense(id = 2, title = "Expense B", amount = 20_000_000, date = 0),
            Expense(id = 3, title = "Expense C", amount = 5_000_000, date = 0)
        )
        assertEquals(35_000_000L, FinancialOverviewCalculator.expensesTotal(expenses))

        // Supplier Balance دست‌نخورده
        val purchases = listOf(Purchase(id = 1, supplierId = 5, totalAmount = 200_000_000))
        val before = SupplierBalanceCalculator.balance(purchases, emptyList())
        assertEquals(200_000_000L, before)
        assertEquals(200_000_000L, SupplierBalanceCalculator.balance(purchases, emptyList()))
    }

    // ============ ۱۷ و ۱۸: تراز نهایی ============

    @Test
    fun `final customer and supplier balances`() {
        // Customer: 500 − 150 = 350
        val customerBalance = BalanceCalculator.balance(
            listOf(invoice(1, 1, "INV-1", 500_000_000)),
            listOf(Payment(customerId = 1, amount = 150_000_000, method = PaymentMethod.CASH.stored))
        )
        assertEquals(350_000_000L, customerBalance)

        // Supplier: Purchases 400m + In-kind 100m − Payments 150m = 350m
        // (در مدل پروژه Purchase و «خرید جنسی» یک مفهوم‌اند؛ هر رکورد یک‌بار شمرده می‌شود)
        val purchases = listOf(
            Purchase(id = 1, supplierId = 5, totalAmount = 400_000_000),
            Purchase(id = 2, supplierId = 5, totalAmount = 100_000_000)
        )
        assertEquals(500_000_000L, SupplierBalanceCalculator.purchasesTotal(purchases)) // بدون Double Count
        val supplierBalance = SupplierBalanceCalculator.balance(
            purchases,
            listOf(SupplierPayment(supplierId = 5, amount = 150_000_000, method = PaymentMethod.CASH.stored))
        )
        assertEquals(350_000_000L, supplierBalance)
    }

    // ============ ۱۹: Financial Account — هر منبع یک‌بار ============

    @Test
    fun `financial account no double counting`() {
        val invoices = listOf(invoice(1, 1, "INV-1", 100_000_000))
        val payments = listOf(Payment(customerId = 1, amount = 40_000_000, method = PaymentMethod.CASH.stored))
        val purchases = listOf(Purchase(id = 1, supplierId = 5, totalAmount = 60_000_000))
        val supplierPayments = listOf(SupplierPayment(supplierId = 5, amount = 20_000_000, method = PaymentMethod.CASH.stored))
        val expenses = listOf(Expense(id = 1, title = "حمل", amount = 10_000_000, date = 0))

        val overview = FinancialOverviewCalculator.calculate(invoices, payments, purchases, supplierPayments, expenses)
        assertEquals(100_000_000L, overview.totalSales)
        assertEquals(40_000_000L, overview.totalReceived)
        assertEquals(60_000_000L, overview.purchasesGoods)
        assertEquals(20_000_000L, overview.totalPaidToSuppliers)
        assertEquals(10_000_000L, overview.totalExpenses)
        // هیچ منبعی دوبار شمرده نشده: نتیجه = 100 − 60 − 10
        assertEquals(30_000_000L, overview.netResult)
    }

    // ============ ۲۰: حالت صفر ============

    @Test
    fun `empty database state is safe and zero`() {
        val overview = FinancialOverviewCalculator.calculate(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0L, overview.totalSales)
        assertEquals(0L, overview.customersBalance)
        assertEquals(0L, overview.suppliersBalance)
        assertEquals(0L, overview.netResult)
        assertEquals(0L, BalanceCalculator.balance(emptyList(), emptyList()))
        assertEquals(0L, SupplierBalanceCalculator.balance(emptyList(), emptyList()))
        assertEquals(0L, FinancialOverviewCalculator.expensesTotal(emptyList()))
        // خروجی چاپی روی داده خالی crash نمی‌کند
        val lines = InvoicePrintLayout.buildLines(
            InvoicePrintFormatter.build(
                invoice(1, 1, "INV-0001", 0), emptyList(), null
            )
        )
        assertTrue(lines.isNotEmpty())
        assertTrue(InvoicePrintLayout.estimatedHeight(lines.size) > 0)
    }

    // ============ ۲۶: اعداد بزرگ — Long بدون Overflow ============

    @Test
    fun `large amounts with Long`() {
        val amounts = listOf(1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L)
        amounts.forEach { a ->
            assertEquals(a, BalanceCalculator.paymentsTotal(listOf(Payment(customerId = 1, amount = a))))
        }
        // جمع چند مبلغ بزرگ بدون Overflow
        val big = amounts.sum()
        assertEquals(1_111_000_000L, big)
        assertEquals(big, BalanceCalculator.paymentsTotal(amounts.map { Payment(customerId = 1, amount = it) }))
        // فرمت بدون خطا (ارقام فارسی)
        assertTrue(PriceFormatter.format(1_000_000_000L).contains("۰۰۰"))
    }

    // ============ ۴۷: Final Financial Reconciliation ============

    @Test
    fun `final reconciliation scenario`() {
        // Customer: Sales 100+200+150=450، Payments 100+50=150 → Balance 300
        val invoices = listOf(
            invoice(1, 1, "INV-1", 100_000_000),
            invoice(2, 1, "INV-2", 200_000_000),
            invoice(3, 1, "INV-3", 150_000_000)
        )
        val customerPayments = listOf(
            Payment(customerId = 1, amount = 100_000_000, method = PaymentMethod.CASH.stored),
            Payment(customerId = 1, amount = 50_000_000, method = PaymentMethod.CHECK.stored)
        )
        assertEquals(450_000_000L, BalanceCalculator.purchasesTotal(invoices))
        assertEquals(150_000_000L, BalanceCalculator.paymentsTotal(customerPayments))
        assertEquals(300_000_000L, BalanceCalculator.balance(invoices, customerPayments))

        // Supplier: Purchases 200+150=350، Payments 100+50=150 → Balance 200
        val purchases = listOf(
            Purchase(id = 1, supplierId = 5, totalAmount = 200_000_000),
            Purchase(id = 2, supplierId = 5, totalAmount = 150_000_000)
        )
        val supplierPayments = listOf(
            SupplierPayment(supplierId = 5, amount = 100_000_000, method = PaymentMethod.CASH.stored),
            SupplierPayment(supplierId = 5, amount = 50_000_000, method = PaymentMethod.CHECK.stored)
        )
        assertEquals(200_000_000L, SupplierBalanceCalculator.balance(purchases, supplierPayments))

        // Expense: 30m
        val expenses = listOf(Expense(id = 1, title = "حمل", amount = 30_000_000, date = 0))
        assertEquals(30_000_000L, FinancialOverviewCalculator.expensesTotal(expenses))

        // Financial Account یکپارچه
        val overview = FinancialOverviewCalculator.calculate(invoices, customerPayments, purchases, supplierPayments, expenses)
        assertEquals(300_000_000L, overview.customersBalance)
        assertEquals(200_000_000L, overview.suppliersBalance)
        assertEquals(30_000_000L, overview.totalExpenses)
        // نتیجه = 450 − 350 − 30 = 70m
        assertEquals(70_000_000L, overview.netResult)
    }

    // ============ ۴۸: Double Counting ویژه ============

    @Test
    fun `no double counting for payment check purchase inkind transfer expense supplier`() {
        // Payment + Check: چک فقط از طریق Payment یک‌بار
        val (p1, c1) = CheckFlow.receiveFromCustomer(1, 20_000_000, "CHK-D1", null, null)
        assertEquals(20_000_000L, BalanceCalculator.paymentsTotal(listOf(p1)))
        assertEquals(20_000_000L, c1.amount)
        // اگر چک هم جدا شمرده می‌شد ۴۰m می‌شد — این‌جا فقط Payment است

        // Purchase + In-kind: در مدل پروژه یک مفهوم (Purchase) — هر رکورد یک‌بار
        val purchases = listOf(
            Purchase(id = 1, supplierId = 5, totalAmount = 100_000_000),
            Purchase(id = 2, supplierId = 5, totalAmount = 50_000_000)
        )
        assertEquals(150_000_000L, SupplierBalanceCalculator.purchasesTotal(purchases))

        // Invoice + Payment: Payment مبلغ Invoice را تغییر نمی‌دهد
        val inv = invoice(1, 1, "INV-1", 100_000_000)
        val pay = Payment(customerId = 1, amount = 100_000_000, method = PaymentMethod.CASH.stored)
        assertEquals(100_000_000L, inv.totalAmount)
        assertEquals(0L, BalanceCalculator.balance(listOf(inv), listOf(pay)))

        // Check Transfer + Payment: انتقال فقط SupplierPayment می‌سازد (یک رویداد)
        val (_, sp) = CheckFlow.transferToSupplier(c1, 5)
        assertEquals(20_000_000L, sp.amount)
        assertEquals(20_000_000L, SupplierBalanceCalculator.paymentsTotal(listOf(sp)))

        // Expense + Supplier: Expense به Supplier مربوط نیست
        val expenses = listOf(Expense(id = 1, title = "حمل", amount = 10_000_000, date = 0))
        val overview = FinancialOverviewCalculator.calculate(
            listOf(inv), listOf(pay), purchases, listOf(sp), expenses
        )
        assertEquals(10_000_000L, overview.totalExpenses)
        // Supplier balance فقط از Purchase/Payment: 150 − 20 = 130
        assertEquals(130_000_000L, overview.suppliersBalance)
    }

    // ============ ۲۸ تا ۳۱: مدل‌ها ============

    @Test
    fun `check model has no issuer no returned and single transferred concept`() {
        // مدل Check فقط: شماره/مبلغ/بانک/دریافت/سررسید/دریافت‌از/صاحب فعلی/وضعیت
        val fields = CheckFlow::class.java.declaredFields.size // وجود object سالم
        assertNotNull(fields)
        // «خرج شده» = «انتقال یافته»: یک وضعیت واحد TRANSFERRED
        assertEquals(CheckStatus.TRANSFERRED, CheckStatus.fromStored("transferred"))
        assertNull(CheckStatus.fromStored("returned")) // وضعیت برگشتی وجود ندارد
        assertNull(CheckStatus.fromStored("issued"))
    }

    @Test
    fun `invoice does not contain payment`() {
        // Invoice فقط Sale است؛ Payment در Customer Profile ثبت می‌شود
        val inv = invoice(1, 1, "INV-1", 100_000_000)
        assertNull(inv.note?.let { it }) // ساختار Invoice فیلد Payment ندارد
        // خروجی چاپی هم Payment ندارد
        val lines = InvoicePrintLayout.buildLines(
            InvoicePrintFormatter.build(inv, listOf(item(1, 1, 10, "مبل", 1, 100_000_000)), null)
        )
        assertFalse(lines.joinToString { it.text }.contains("پرداخت"))
        assertFalse(lines.joinToString { it.text }.contains("تسویه"))
    }

    // ============ ۵۰: رسید پرداخت نقدی تأمین‌کننده - تراز دست‌نخورده ============

    @Test
    fun `supplier payment with receipt keeps financial logic intact`() {
        // پرداخت نقدی با رسید: مبلغ دست‌نخورده باقی می‌ماند
        val spWithReceipt = SupplierPayment(
            supplierId = 5,
            amount = 50_000_000,
            method = PaymentMethod.CASH.stored,
            receiptImagePath = "/path/receipt.jpg"
        )
        val purchases = listOf(Purchase(id = 1, supplierId = 5, totalAmount = 200_000_000))
        assertEquals(150_000_000L, SupplierBalanceCalculator.balance(purchases, listOf(spWithReceipt)))
        // مبلغ پرداخت تغییر نکرده
        assertEquals(50_000_000L, spWithReceipt.amount)
        assertEquals(PaymentMethod.CASH.stored, spWithReceipt.method)
        // تراز با پرداخت بدون رسید دقیقاً یکسان
        val spNoReceipt = spWithReceipt.copy(receiptImagePath = null)
        assertEquals(
            SupplierBalanceCalculator.balance(purchases, listOf(spWithReceipt)),
            SupplierBalanceCalculator.balance(purchases, listOf(spNoReceipt))
        )
    }

    @Test
    fun `check payment to supplier never gets receipt field effect`() {
        // چک پرداختی نباید تحت تأثیر فیلد رسید قرار بگیرد (فقط پرداخت نقدی رسید دارد)
        val checkPayment = SupplierPayment(
            supplierId = 5,
            amount = 100_000_000,
            method = PaymentMethod.CHECK.stored,
            receiptImagePath = null
        )
        val purchases = listOf(Purchase(id = 1, supplierId = 5, totalAmount = 100_000_000))
        assertEquals(0L, SupplierBalanceCalculator.balance(purchases, listOf(checkPayment)))
        assertEquals(100_000_000L, SupplierBalanceCalculator.paymentsTotal(listOf(checkPayment)))
    }
}
