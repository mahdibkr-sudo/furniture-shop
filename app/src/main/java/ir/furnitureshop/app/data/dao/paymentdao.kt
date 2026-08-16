package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.furnitureshop.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    /** پرداخت‌های یک مشتری (نقد و چک) — برای پروفایل و محاسبه تراز */
    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    fun observeByCustomer(customerId: Long): Flow<List<Payment>>

    /** همه پرداخت‌های مشتریان — برای حساب مالی کلی */
    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Payment>>

    @Insert
    suspend fun insert(payment: Payment): Long
}
