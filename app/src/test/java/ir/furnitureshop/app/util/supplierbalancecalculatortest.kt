package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * تست‌های تراز مالی تأمین‌کننده — بدون Double Counting.
 */
class SupplierBalanceCalculatorTest {

    private fun purchase(amount: Long, note: String? = null) = Purchase(
        id = 1,
        supplierId = 1,
        totalAmount = amount,
        note = note
    )

    private fun payment(
        amount: Long,
        method: PaymentMethod = PaymentMethod.CASH
    ) = SupplierPayment(
        id = 1,
        supplierId = 1,
        amount = amount,
        method = method.stored
    )

    // ---- قانون اصلی: خرید جنسی بدهی ایجاد می‌کند، پرداخت کاهش می‌دهد ----

    @Test
    fun `purchase 100 cash payment 30 gives balance 70`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(30))
        assertEquals(70L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    @Test
    fun `check payment reduces balance same as cash`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(30, PaymentMethod.CHECK))
        assertEquals(70L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    // ---- مبلغ خرید جنسی ----

    @Test
    fun `purchases goods total sums all purchases once`() {
        val purchases = listOf(purchase(100), purchase(50), purchase(30))
        assertEquals(180L, SupplierBalanceCalculator.purchasesTotal(purchases))
    }

    // ---- بدون Double Counting ----

    @Test
    fun `two payments counted once each`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(30), payment(30))
        assertEquals(40L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    @Test
    fun `mixed cash and check no double counting`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(
            payment(20, PaymentMethod.CASH),
            payment(30, PaymentMethod.CHECK),
            payment(10, PaymentMethod.CASH)
        )
        assertEquals(40L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    // ---- Payment مبلغ Purchase را تغییر نمی‌دهد ----

    @Test
    fun `payment never mutates purchase total`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(30))
        SupplierBalanceCalculator.balance(purchases, payments)
        assertEquals(100L, purchases.first().totalAmount)
    }

    // ---- Payment به تنهایی Purchase جدید ایجاد نمی‌کند ----

    @Test
    fun `payment alone does not create a purchase`() {
        // فقط پرداخت ثبت شده — هیچ خریدی ساخته نمی‌شود
        val purchases = emptyList<Purchase>()
        val payments = listOf(payment(30))
        assertEquals(0L, SupplierBalanceCalculator.purchasesTotal(purchases))
        // تراز = 0 − 30 = بستانکاری ما
        assertEquals(-30L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    @Test
    fun `check payment is not counted as purchase`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(30, PaymentMethod.CHECK))
        // purchasesTotal فقط از purchases می‌آید؛ چک به عنوان خرید شمرده نمی‌شود
        assertEquals(100L, SupplierBalanceCalculator.purchasesTotal(purchases))
        assertEquals(70L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    @Test
    fun `balance with multiple purchases and mixed payments`() {
        val purchases = listOf(purchase(100), purchase(50))
        val payments = listOf(
            payment(30, PaymentMethod.CASH),
            payment(20, PaymentMethod.CHECK)
        )
        assertEquals(100L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    // ---- حالت‌های مرزی ----

    @Test
    fun `no purchases no payments balance is zero`() {
        assertEquals(0L, SupplierBalanceCalculator.balance(emptyList(), emptyList()))
    }

    @Test
    fun `fully paid balance is zero`() {
        val purchases = listOf(purchase(100))
        val payments = listOf(payment(100))
        assertEquals(0L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    // ---- receiptImagePath بر تراز تأمین‌کننده اثر ندارد ----

    @Test
    fun `receipt image path does not affect supplier balance`() {
        val purchases = listOf(purchase(100))
        val paymentsWithReceipt = listOf(
            payment(30).copy(receiptImagePath = "/path/to/receipt.jpg")
        )
        val paymentsWithoutReceipt = listOf(payment(30))
        // تراز باید دقیقاً یکسان باشد، چه با رسید باشه چه بدون
        assertEquals(70L, SupplierBalanceCalculator.balance(purchases, paymentsWithReceipt))
        assertEquals(70L, SupplierBalanceCalculator.balance(purchases, paymentsWithoutReceipt))
    }

    @Test
    fun `receipt image path with check payment also has no effect`() {
        val purchases = listOf(purchase(200))
        val payments = listOf(
            payment(50, PaymentMethod.CHECK).copy(receiptImagePath = "/path/to/check_receipt.jpg")
        )
        assertEquals(150L, SupplierBalanceCalculator.balance(purchases, payments))
    }

    @Test
    fun `blank receipt image path treated as missing`() {
        val sp = payment(10).copy(receiptImagePath = "")
        // مسیر خالی باید مانند نال رفتار کند و تراز تغییر نکند
        val purchases = listOf(purchase(100))
        assertEquals(90L, SupplierBalanceCalculator.balance(purchases, listOf(sp)))
    }
}
