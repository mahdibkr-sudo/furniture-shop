package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * پرداخت انجام‌شده به تأمین‌کننده — مستقل و متصل به خود تأمین‌کننده.
 *
 * - روش «چک» فقط روش پرداخت است؛ سیستم کامل Check در Task بعدی اضافه می‌شود
 * - تراز تأمین‌کننده = Σ خرید جنسی − Σ پرداخت‌ها
 * - receiptImagePath: مسیر تصویر رسید (اختیاری) — فقط برای پرداخت نقدی
 *   در صورت حذف یا عدم‌دسترسی، خود تراکنش مالی معتبر باقی می‌ماند.
 */
@Entity(
    tableName = "supplier_payments",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId")]
)
data class SupplierPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val amount: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val method: String = PaymentMethod.CASH.stored,
    val receiptImagePath: String? = null
)
