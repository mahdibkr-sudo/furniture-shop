package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** وضعیت چک — «برگشتی» لازم نیست */
enum class CheckStatus(val stored: String, val label: String) {
    IN_HAND("in_hand", "در دست فروشگاه"),
    TRANSFERRED("transferred", "انتقال‌یافته (خرج شده)");

    companion object {
        fun fromStored(value: String): CheckStatus? =
            entries.firstOrNull { it.stored == value }
    }
}

/**
 * چک — یکپارچه بین مشتری و تأمین‌کننده.
 *
 * - هنگام دریافت از مشتری: Payment (برای مشتری) + Check ثبت می‌شود
 * - هنگام پرداخت به تأمین‌کننده: همان Check منتقل می‌شود (Check جدید ساخته نمی‌شود)
 *   فقط currentHolder و status تغییر می‌کنند + Payment برای تأمین‌کننده ثبت می‌شود
 * - currentHolder: "shop" = در دست فروشگاه | "supplier:<id>" = منتقل‌شده به تأمین‌کننده
 * - Check به تنهایی Balance را تغییر نمی‌دهد؛ Payment باعث تغییر Balance است
 */
@Entity(
    tableName = "checks",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["receivedFromCustomerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["checkNumber"], unique = true),
        Index("receivedFromCustomerId")
    ]
)
data class Check(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** شماره چک — یکتا (Duplicate ممنوع) */
    val checkNumber: String,
    val amount: Long = 0,
    val bank: String? = null,
    /** تاریخ دریافت از مشتری */
    val receiveDate: Long = System.currentTimeMillis(),
    /** سررسید */
    val dueDate: Long? = null,
    /** چک از این مشتری دریافت شده است */
    val receivedFromCustomerId: Long? = null,
    /** صاحب فعلی: "shop" یا "supplier:<id>" */
    val currentHolder: String = CheckFlow.HOLDER_SHOP,
    val status: String = CheckStatus.IN_HAND.stored
)

/**
 * منطق خالص جریان چک — قابل تست واحد.
 *
 * قوانین:
 * - «خرج شده» و «انتقال یافته» یک مفهوم هستند (TRANSFERRED)
 * - چک از Customer دریافت → Payment + Check (currentHolder = shop)
 * - چک به Supplier → همان Check منتقل می‌شود؛ فقط holder/status تغییر می‌کند
 * - Check به تنهایی Balance را تغییر نمی‌دهد؛ Payment تغییر می‌دهد
 * - یک Check فقط یک‌بار قابل انتقال است → هرگز در Customer و Supplier دوبار محاسبه نمی‌شود
 */
object CheckFlow {

    const val HOLDER_SHOP = "shop"
    private const val SUPPLIER_PREFIX = "supplier:"

    fun supplierHolder(supplierId: Long): String = "$SUPPLIER_PREFIX$supplierId"

    fun supplierIdOf(holder: String): Long? =
        holder.removePrefix(SUPPLIER_PREFIX).toLongOrNull()

    fun holderLabel(holder: String, supplierName: String? = null): String = when {
        holder == HOLDER_SHOP -> "در دست فروشگاه"
        supplierIdOf(holder) != null -> supplierName ?: "تأمین‌کننده #${supplierIdOf(holder)}"
        else -> holder
    }

    /** فقط چکِ در دست فروشگاه قابل انتقال است */
    fun canTransfer(check: Check): Boolean =
        check.currentHolder == HOLDER_SHOP && check.status == CheckStatus.IN_HAND.stored

    /** بررسی شماره تکراری (Duplicate Check ممنوع) */
    fun isDuplicateNumber(existingNumbers: List<String>, number: String): Boolean =
        existingNumbers.any { it.equals(number.trim(), ignoreCase = true) }

    /**
     * دریافت چک از مشتری:
     * Payment (برای مشتری) + Check با holder=shop ساخته می‌شود.
     */
    fun receiveFromCustomer(
        customerId: Long,
        amount: Long,
        checkNumber: String,
        bank: String?,
        dueDate: Long?
    ): Pair<Payment, Check> {
        val payment = Payment(
            customerId = customerId,
            amount = amount,
            date = System.currentTimeMillis(),
            method = PaymentMethod.CHECK.stored
        )
        val check = Check(
            checkNumber = checkNumber.trim(),
            amount = amount,
            bank = bank?.trim()?.ifEmpty { null },
            receiveDate = System.currentTimeMillis(),
            dueDate = dueDate,
            receivedFromCustomerId = customerId,
            currentHolder = HOLDER_SHOP,
            status = CheckStatus.IN_HAND.stored
        )
        return payment to check
    }

    /**
     * انتقال همان چک به تأمین‌کننده:
     * فقط holder/status تغییر می‌کند + Payment برای تأمین‌کننده ساخته می‌شود.
     * Check جدید ساخته نمی‌شود.
     */
    fun transferToSupplier(
        check: Check,
        supplierId: Long
    ): Pair<Check, SupplierPayment> {
        require(canTransfer(check)) { "این چک قابل انتقال نیست" }
        val updated = check.copy(
            currentHolder = supplierHolder(supplierId),
            status = CheckStatus.TRANSFERRED.stored
        )
        val payment = SupplierPayment(
            supplierId = supplierId,
            amount = check.amount,
            date = System.currentTimeMillis(),
            method = PaymentMethod.CHECK.stored
        )
        return updated to payment
    }
}
