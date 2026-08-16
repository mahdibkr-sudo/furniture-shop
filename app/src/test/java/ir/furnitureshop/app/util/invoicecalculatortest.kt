package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceCalculatorTest {

    private val sofa = Product(id = 1, name = "مبل سه‌نفره", basePrice = 10_000_000)
    private val table = Product(id = 2, name = "میز ناهارخوری", basePrice = 5_000_000)

    // ---- چند محصول ----

    @Test
    fun `subtotal with multiple products`() {
        val items = listOf(
            InvoiceCalculator.cartItemFrom(sofa),
            InvoiceCalculator.cartItemFrom(table),
            InvoiceCalculator.cartItemFrom(sofa).copy(quantity = 2)
        )
        // 10M + 5M + (10M × 2) = 35M
        assertEquals(35_000_000L, InvoiceCalculator.subtotal(items))
    }

    @Test
    fun `subtotal empty cart is zero`() {
        assertEquals(0L, InvoiceCalculator.subtotal(emptyList()))
    }

    // ---- تخفیف ----

    @Test
    fun `discount reduces total`() {
        val subtotal = InvoiceCalculator.subtotal(listOf(InvoiceCalculator.cartItemFrom(sofa)))
        assertEquals(8_000_000L, InvoiceCalculator.total(subtotal, 2_000_000, false, 0))
    }

    @Test
    fun `discount never exceeds subtotal`() {
        val subtotal = InvoiceCalculator.subtotal(listOf(InvoiceCalculator.cartItemFrom(table)))
        assertEquals(0L, InvoiceCalculator.total(subtotal, 99_000_000, false, 0))
    }

    // ---- پارچه ----

    @Test
    fun `customer provided fabric is not added`() {
        val subtotal = InvoiceCalculator.subtotal(listOf(InvoiceCalculator.cartItemFrom(sofa)))
        assertEquals(10_000_000L, InvoiceCalculator.total(subtotal, 0, true, 3_000_000))
    }

    @Test
    fun `shop provided fabric is added to total`() {
        val subtotal = InvoiceCalculator.subtotal(listOf(InvoiceCalculator.cartItemFrom(sofa)))
        assertEquals(13_000_000L, InvoiceCalculator.total(subtotal, 0, false, 3_000_000))
    }

    // ---- قیمت تاریخی ----

    @Test
    fun `cart item keeps price snapshot after product price change`() {
        val item = InvoiceCalculator.cartItemFrom(sofa) // قیمت لحظه افزودن = 10M

        // بعداً قیمت پایه محصول تغییر می‌کند
        val updatedProduct = sofa.copy(basePrice = 25_000_000)

        assertEquals(10_000_000L, item.unitPrice)
        assertEquals(25_000_000L, updatedProduct.basePrice)
        // فاکتور قبلی دست‌نخورده می‌ماند
        assertEquals(10_000_000L, InvoiceCalculator.subtotal(listOf(item)))
    }
}
