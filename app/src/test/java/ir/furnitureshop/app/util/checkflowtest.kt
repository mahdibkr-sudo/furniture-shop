package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.Purchase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست سناریوی کامل: Customer → Check → Supplier
 *
 * تأیید می‌کند:
 * - دریافت چک از مشتری = Payment + Check
 * - انتقال چک به تأمین‌کننده = همان چک (جدید ساخته نمی‌شود)، فقط holder/status تغییر می‌کند
 * - Check به تنهایی Balance را تغییر نمی‌دهد؛ Payment تغییر می‌دهد
 * - یک Check هرگز دوبار محاسبه نمی‌شود
 * - Duplicate Check ممنوع است
 */
class CheckFlowTest {

    @Test
    fun `full customer to check to supplier scenario`() {
        // === مرحله ۱: دریافت چک از مشتری (مشتری ۱) ===
        val (payment, check) = CheckFlow.receiveFromCustomer(
            customerId = 1,
            amount = 100,
            checkNumber = "CHK-001",
            bank = "ملت",
            dueDate = null
        )

        // Payment با روش چک ثبت شد
        assertEquals(PaymentMethod.CHECK.stored, payment.method)
        assertEquals(100L, payment.amount)
        // Check به مشتری متصل است و در دست فروشگاه است
        assertEquals(1L, check.receivedFromCustomerId)
        assertEquals(CheckFlow.HOLDER_SHOP, check.currentHolder)
        assertEquals(CheckStatus.IN_HAND.stored, check.status)
        assertTrue(CheckFlow.canTransfer(check))

        // === تراز مشتری: Invoice=100 ، Payment(چک)=100 → 0 ===
        val invoices = listOf(
            Invoice(id = 1, customerId = 1, invoiceNumber = "INV-0001", totalAmount = 100)
        )
        val customerBalance = BalanceCalculator.balance(invoices, listOf(payment))
        assertEquals(0L, customerBalance)

        // === مرحله ۲: انتقال همان چک به تأمین‌کننده (تأمین‌کننده ۵) ===
        val (transferredCheck, supplierPayment) = CheckFlow.transferToSupplier(check, supplierId = 5)

        // همان چک است (id و checkNumber و amount حفظ شده) — چک جدید ساخته نشده
        assertEquals(check.id, transferredCheck.id)
        assertEquals("CHK-001", transferredCheck.checkNumber)
        assertEquals(100L, transferredCheck.amount)
        // فقط holder و status تغییر کردند
        assertEquals(CheckFlow.supplierHolder(5), transferredCheck.currentHolder)
        assertEquals(CheckStatus.TRANSFERRED.stored, transferredCheck.status)
        // Payment برای تأمین‌کننده با روش چک ثبت شد
        assertEquals(5L, supplierPayment.supplierId)
        assertEquals(100L, supplierPayment.amount)
        assertEquals(PaymentMethod.CHECK.stored, supplierPayment.method)

        // بعد از انتقال، چک دیگر قابل انتقال نیست → دوبار محاسبه نمی‌شود
        assertFalse(CheckFlow.canTransfer(transferredCheck))

        // === تراز تأمین‌کننده: Purchase=100 ، Payment(چک)=100 → 0 ===
        val purchases = listOf(
            Purchase(id = 1, supplierId = 5, totalAmount = 100)
        )
        val supplierBalance =
            SupplierBalanceCalculator.balance(purchases, listOf(supplierPayment))
        assertEquals(0L, supplierBalance)

        // === جمع: چک در تراز هر دو طرف فقط یک‌بار از طریق Payment اثر گذاشته ===
        // مشتری: 100 (Invoice) − 100 (Payment) = 0
        // تأمین‌کننده: 100 (Purchase) − 100 (Payment) = 0
        // هیچ Double Counting رخ نداده است
    }

    @Test
    fun `duplicate check number is rejected`() {
        val existing = listOf("CHK-001", "chk-002")
        assertTrue(CheckFlow.isDuplicateNumber(existing, "CHK-001"))
        assertTrue(CheckFlow.isDuplicateNumber(existing, "ChK-002")) // نادیده‌گرفتن حروف بزرگ
        assertFalse(CheckFlow.isDuplicateNumber(existing, "CHK-003"))
    }

    @Test
    fun `transfer requires check in hand`() {
        val check = Check(
            id = 1,
            checkNumber = "CHK-001",
            amount = 100,
            receivedFromCustomerId = 1,
            currentHolder = CheckFlow.HOLDER_SHOP,
            status = CheckStatus.IN_HAND.stored
        )
        // قابل انتقال است
        val (transferred, _) = CheckFlow.transferToSupplier(check, supplierId = 2)
        assertEquals(CheckFlow.supplierHolder(2), transferred.currentHolder)

        // تلاش برای انتقال دوباره باید خطا بدهد
        val again = runCatching { CheckFlow.transferToSupplier(transferred, supplierId = 3) }
        assertTrue(again.isFailure)
    }

    @Test
    fun `check payment reduces supplier balance same as cash`() {
        val (_, check) = CheckFlow.receiveFromCustomer(1, 100, "CHK-009", null, null)
        val (_, supplierPayment) = CheckFlow.transferToSupplier(check, supplierId = 2)
        val purchases = listOf(Purchase(id = 1, supplierId = 2, totalAmount = 100))
        // چک (از طریق Payment) بدهی را کاهش می‌دهد؛ مبلغ اضافه‌ای ایجاد نمی‌کند
        assertEquals(0L, SupplierBalanceCalculator.balance(purchases, listOf(supplierPayment)))
    }
}
