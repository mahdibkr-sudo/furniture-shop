package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Product

/** قلم جاری در حال صدور فاکتور */
data class InvoiceCartItem(
    val productId: Long,
    val productName: String,
    /** قیمت واحد ثبت‌شده در لحظه افزودن (قیمت تاریخی) */
    val unitPrice: Long,
    val quantity: Int = 1
)

/**
 * محاسبات فاکتور — خالص و بدون وابستگی تا قابل تست واحد باشد.
 */
object InvoiceCalculator {

    /** ساخت قلم فاکتور از محصول؛ قیمت پایه همان لحظه snapshot می‌شود */
    fun cartItemFrom(product: Product): InvoiceCartItem =
        InvoiceCartItem(
            productId = product.id,
            productName = product.name,
            unitPrice = product.basePrice,
            quantity = 1
        )

    fun subtotal(items: List<InvoiceCartItem>): Long =
        items.sumOf { it.unitPrice * it.quantity }

    /**
     * مبلغ نهایی = جمع اقلام − تخفیف + پارچه (فقط وقتی پارچه را ما تهیه کرده‌ایم)
     * تخفیف از جمع اقلام بیشتر نمی‌شود.
     */
    fun total(
        subtotal: Long,
        discount: Long,
        fabricCustomerProvided: Boolean,
        fabricAmount: Long
    ): Long {
        val d = discount.coerceIn(0L, subtotal)
        val fabric = if (fabricCustomerProvided) 0L else fabricAmount.coerceAtLeast(0L)
        return subtotal - d + fabric
    }
}
