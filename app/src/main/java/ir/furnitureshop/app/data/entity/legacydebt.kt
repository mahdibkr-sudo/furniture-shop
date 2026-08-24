package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * بدهی قبلی مشتری از سیستم مالی پیشین — بدون فاکتور.
 *
 * - فقط یک عدد بدهی + توضیح اختیاری و تاریخ ثبت
 * - در تراز: balance = Σ فاکتورها + Σ بدهی‌های قبلی − Σ پرداخت‌ها
 * - onDelete CASCADE: با حذف مشتری پاک می‌شود
 */
@Entity(
    tableName = "legacy_debts",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class LegacyDebt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val description: String? = null
)
