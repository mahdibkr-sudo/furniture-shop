package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * محصول — فقط مدل‌های مبل و میز
 *
 * basePrice فقط قیمت پایه سازه مبل است؛ قیمت پارچه هنگام صدور فاکتور
 * جداگانه محاسبه می‌شود.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** مسیر فایل محلی تصویر در حافظه داخلی اپ (نسبت به content://) */
    val image: String? = null,
    val basePrice: Long = 0
)
