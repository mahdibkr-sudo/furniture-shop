package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.viewmodel.CustomerProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * شبیه‌سازی پروفایل مشتری با داده واقعی — شامل فاکتورهای دارای فیلدهای جدید
 * (متراژ/قیمت واحد پارچه) تا مطمئن شویم نمایش و تراز سالم است.
 */
class CustomerProfileStateTest {

    private val customer = Customer(id = 1, name = "مشتری نمونه", phone = "09121234567")

    private val invoice = Invoice(
        id = 1,
        customerId = 1,
        invoiceNumber = "INV-0001",
        date = 1_700_000_000_000L,
        discount = 1_000_000,
        fabricCustomerProvided = false,
        fabricMeters = 3.5,
        fabricUnitPrice = 500_000,
        fabricAmount = 1_750_000,
        totalAmount = 21_750_000
    )

    @Test
    fun `profile state renders new fabric fields and balance`() {
        val payments = listOf(
            Payment(customerId = 1, amount = 10_000_000, method = "CASH", date = 1_700_000_000_000L)
        )
        val checks = listOf(
            Check(checkNumber = "CHK-1", amount = 5_000_000, receiveDate = 1_700_000_000_000L)
        )
        val state = CustomerProfileUiState(
            customer = customer,
            invoices = listOf(invoice),
            payments = payments,
            checks = checks,
            totalPurchases = BalanceCalculator.purchasesTotal(listOf(invoice)),
            totalPaid = BalanceCalculator.paymentsTotal(payments)
        )
        assertEquals(21_750_000L, state.totalPurchases)
        assertEquals(10_000_000L, state.totalPaid)
        assertEquals(11_750_000L, state.balance) // بدهی
        assertNull(state.error)
        // نمایش بدون خطا
        assertEquals("۱۴۰۲/۰۸/۲۴", JalaliDate.formatDate(invoice.date))
        assertEquals("۳٫۵", PriceFormatter.formatMeters(invoice.fabricMeters))
        assertEquals("۱٬۷۵۰٬۰۰۰", PriceFormatter.format(invoice.fabricAmount))
        assertEquals(CheckStatus.IN_HAND, CheckStatus.fromStored(checks[0].status))
    }

    @Test
    fun `profile with zero data is safe`() {
        val state = CustomerProfileUiState(customer = customer)
        assertEquals(0L, state.totalPurchases)
        assertEquals(0L, state.totalPaid)
        assertEquals(0L, state.balance)
        assertNull(state.error)
    }
}
