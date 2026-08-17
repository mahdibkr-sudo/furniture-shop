package ir.furnitureshop.app.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import ir.furnitureshop.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

data class PaymentWithCustomer(
    @Embedded val payment: Payment,
    @ColumnInfo(name = "customerName") val customerName: String?
)

@Dao
interface PaymentDao {

    /** پرداخت‌های یک مشتری (نقد و چک) — برای پروفایل و محاسبه تراز */
    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    fun observeByCustomer(customerId: Long): Flow<List<Payment>>

    /** همه پرداخت‌های مشتریان — برای حساب مالی کلی */
    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Payment>>

    /** همه پرداخت‌های نقدی مشتریان + نام مشتری (LEFT JOIN برای مشتری حذف‌شده) — بخش «پرداخت‌های نقدی» */
    @Query(
        "SELECT payments.*, customers.name AS customerName FROM payments " +
            "LEFT JOIN customers ON payments.customerId = customers.id " +
            "WHERE payments.method = 'cash' ORDER BY payments.date DESC, payments.id DESC"
    )
    fun observeAllCashWithCustomer(): Flow<List<PaymentWithCustomer>>

    @Insert
    suspend fun insert(payment: Payment): Long
}
