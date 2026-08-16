package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ردیف‌های هر فاکتور (قلم کالا)
 *
 * قیمت تاریخی: unitPrice و productName در لحظه صدور فاکتور ذخیره می‌شوند؛
 * اگر بعداً قیمت پایه محصول تغییر کند یا خود محصول حذف شود، فاکتور قبلی
 * دست‌نخورده می‌ماند (فقط ارجاع نرم به productId).
 */
@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId"), Index("productId")]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    /** نام محصول در لحظه صدور (برای نمایش حتی پس از حذف محصول) */
    val productName: String,
    val quantity: Int = 1,
    /** قیمت واحد در لحظه صدور — تغییرات بعدی قیمت روی آن اثر ندارد */
    val unitPrice: Long = 0
)
