package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * خرید از تأمین‌کننده
 */
@Entity(
    tableName = "purchases",
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
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Long = 0,
    val note: String? = null
)
