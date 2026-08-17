package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.dao.PaymentWithCustomer
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.viewmodel.CashPaymentListUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست حالت UI لیست «پرداخت‌های نقدی».
 *
 * تأیید می‌کند:
 * - حالت پیش‌فرض بدون خطا و بدون ردیف است
 * - فقط ردیف‌های نقد به‌عنوان نقد شناخته می‌شوند (دفاع در برابر داده ناسازگار)
 * - خطای بارگذاری در state منتشر می‌شود (به‌جای کرش)
 */
class CashPaymentListTest {

    private fun row(method: String, customerId: Long = 1): PaymentWithCustomer =
        PaymentWithCustomer(
            payment = Payment(customerId = customerId, amount = 500, method = method),
            customerName = "مشتری تست"
        )

    @Test
    fun `default state has no error and empty list`() {
        val state = CashPaymentListUiState()
        assertNull(state.loadError)
        assertTrue(state.payments.isEmpty())
    }

    @Test
    fun `cash rows are recognized as cash`() {
        val state = CashPaymentListUiState(payments = listOf(row(PaymentMethod.CASH.stored)))
        assertEquals(1, state.payments.size)
        assertTrue(state.isCash(state.payments.first()))
    }

    @Test
    fun `check rows are not recognized as cash`() {
        val state = CashPaymentListUiState(payments = listOf(row(PaymentMethod.CHECK.stored)))
        assertFalse(state.isCash(state.payments.first()))
    }

    @Test
    fun `load error is carried in state instead of crashing`() {
        val state = CashPaymentListUiState(loadError = "خطا در بارگذاری پرداخت‌های نقدی")
        assertEquals("خطا در بارگذاری پرداخت‌های نقدی", state.loadError)
    }

    @Test
    fun `customer name is preserved for navigation to profile`() {
        val state = CashPaymentListUiState(
            payments = listOf(
                PaymentWithCustomer(
                    payment = Payment(id = 7, customerId = 3, amount = 1000),
                    customerName = "علی"
                )
            )
        )
        val row = state.payments.first()
        assertEquals("علی", row.customerName)
        assertEquals(3L, row.payment.customerId)
    }
}
