package ir.furnitureshop.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** روش دریافت پرداخت */
enum class PaymentMethod(val stored: String, val label: String) {
    CASH("cash", "نقد"),
    CHECK("check", "چک");

    companion object {
        fun fromStored(value: String): PaymentMethod? =
            entries.firstOrNull { it.stored == value }
    }
}

/**
 * پرداخت دریافت‌شده از مشتری — مستقل از فاکتور و متصل به خود مشتری.
 *
 * - هر پرداخت یک‌بار در تراز شمرده می‌شود (بدون Double Counting)
 * - روش «چک» فقط روش پرداخت است؛ خودِ چک مبلغ اضافه‌ای ایجاد نمی‌کند
 * - Payment هرگز مبلغ Invoice را تغییر نمی‌دهد
 *
 * balance = جمع فاکتورها − جمع پرداخت‌ها (نقد و چک هر دو بدهی را کاهش می‌دهند)
 */
@Entity(
    tableName = "payments",
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
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val method: String = PaymentMethod.CASH.stored
)
