package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * فاکتور فروش به مشتری
 *
 * - invoiceNumber: شماره فاکتور یکتا (مثلاً INV-0001)
 * - discount: تخفیف کل فاکتور (تومان) — در لحظه صدور ثبت می‌شود
 * - fabricCustomerProvided: true = مشتری خودش پارچه آورده (مبلغ پارچه اضافه نمی‌شود)
 *   false = فروشگاه پارچه تهیه کرده (مبلغ پارچه به فاکتور اضافه می‌شود)
 * - fabricMeters: متراژ پارچه (مثلاً ۳/۵ متر) — فقط وقتی فروشگاه تهیه کرده
 * - fabricUnitPrice: قیمت هر متر پارچه
 * - fabricAmount: مبلغ کل پارچه = متراژ × قیمت واحد (برای فاکتورهای قدیمی فقط مبلغ ذخیره شده)
 * - fabricName: نام پارچه (اختیاری) — در هر دو حالت تهیه توسط مشتری یا فروشگاه ثبت می‌شود
 * - totalAmount: مبلغ نهایی = جمع اقلام − تخفیف + (پارچه در صورت تهیه توسط ما)
 *
 * پارچه بخشی از همین فاکتور است، نه فاکتور جدا.
 */
@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("customerId"),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val discount: Long = 0,
    val fabricCustomerProvided: Boolean = false,
    val fabricMeters: Double = 0.0,
    val fabricUnitPrice: Long = 0,
    val fabricAmount: Long = 0,
    val totalAmount: Long = 0,
    val fabricName: String? = null,
    val note: String? = null
)
