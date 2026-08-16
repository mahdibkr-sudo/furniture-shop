package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تأمین‌کننده — فروشنده مواد اولیه، رنگ‌کار، خدمات‌دهنده یا خریدار.
 * فقط نام و تلفن؛ آدرس لازم نیست. نیازی به تعریف Service یا Product ندارد.
 */
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null
)
