package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مشتری — فقط نام و تلفن؛ آدرس لازم نیست.
 */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null
)
