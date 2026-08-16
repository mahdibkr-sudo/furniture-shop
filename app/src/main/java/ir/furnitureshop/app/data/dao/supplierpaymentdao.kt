package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.furnitureshop.app.data.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierPaymentDao {

    /** پرداخت‌های انجام‌شده به یک تأمین‌کننده (نقد و چک) */
    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY date DESC, id DESC")
    fun observeBySupplier(supplierId: Long): Flow<List<SupplierPayment>>

    /** همه پرداخت‌های تأمین‌کنندگان — برای حساب مالی کلی */
    @Query("SELECT * FROM supplier_payments ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<SupplierPayment>>

    @Insert
    suspend fun insert(payment: SupplierPayment): Long
}
