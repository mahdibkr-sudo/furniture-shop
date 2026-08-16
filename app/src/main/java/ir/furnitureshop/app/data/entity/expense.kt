package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * هزینه جانبی/جزئی کسب‌وکار (حمل مواد، حمل‌ونقل، هزینه‌های کارگاه و...).
 *
 * کاملاً مستقل از Supplier است و روی تراز تأمین‌کننده اثر ندارد.
 * در «حساب مالی کلی» قابل مشاهده است.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val date: Long,
    val description: String? = null
)
