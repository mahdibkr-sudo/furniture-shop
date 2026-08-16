package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * تست‌های تراز مالی مشتری — بدون Double Counting.
 */
class BalanceCalculatorTest {

    private fun invoice(amount: Long) = Invoice(
        id = 1,
        customerId = 1,
        invoiceNumber = "INV-0001",
        totalAmount = amount
    )

    private fun payment(
        amount: Long,
        method: PaymentMethod = PaymentMethod.CASH
    ) = Payment(
        id = 1,
        customerId = 1,
        amount = amount,
        method = method.stored
    )

    // ---- قانون اصلی: Invoice بدهی ایجاد می‌کند، Payment کاهش می‌دهد ----

    @Test
    fun `invoice 100 cash payment 30 gives balance 70`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(30))
        assertEquals(70L, BalanceCalculator.balance(invoices, payments))
    }

    @Test
    fun `check payment reduces balance same as cash`() {
        // Invoice = 100 ، Check Payment = 30 → Balance = 70 (چک فقط روش پرداخت است)
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(30, PaymentMethod.CHECK))
        assertEquals(70L, BalanceCalculator.balance(invoices, payments))
    }

    // ---- بدون Double Counting ----

    @Test
    fun `two payments counted once each`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(30), payment(30))
        // 100 − (30+30) = 40
        assertEquals(40L, BalanceCalculator.balance(invoices, payments))
    }

    @Test
    fun `mixed cash and check payments no double counting`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(
            payment(20, PaymentMethod.CASH),
            payment(30, PaymentMethod.CHECK),
            payment(10, PaymentMethod.CASH)
        )
        assertEquals(40L, BalanceCalculator.balance(invoices, payments))
    }

    // ---- Payment مبلغ Invoice را تغییر نمی‌دهد ----

    @Test
    fun `payment never mutates invoice total`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(30))
        BalanceCalculator.balance(invoices, payments)
        // مبلغ فاکتور دست‌نخورده می‌ماند
        assertEquals(100L, invoices.first().totalAmount)
    }

    // ---- حالت‌های مرزی ----

    @Test
    fun `no invoices no payments balance is zero`() {
        assertEquals(0L, BalanceCalculator.balance(emptyList(), emptyList()))
    }

    @Test
    fun `fully paid invoice balance is zero`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(100))
        assertEquals(0L, BalanceCalculator.balance(invoices, payments))
    }

    @Test
    fun `overpaid customer is creditor (negative balance)`() {
        val invoices = listOf(invoice(100))
        val payments = listOf(payment(150))
        assertEquals(-50L, BalanceCalculator.balance(invoices, payments))
    }

    @Test
    fun `multiple invoices summed once`() {
        val invoices = listOf(invoice(100), invoice(50))
        val payments = listOf(payment(30))
        assertEquals(120L, BalanceCalculator.balance(invoices, payments))
    }
}
