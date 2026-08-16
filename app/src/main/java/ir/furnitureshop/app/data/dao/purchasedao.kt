package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.furnitureshop.app.data.entity.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    /** خریدهای (جنسی) از یک تأمین‌کننده — برای پروفایل و «مبلغ خرید جنسی» */
    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY date DESC, id DESC")
    fun observeBySupplier(supplierId: Long): Flow<List<Purchase>>

    /** همه خریدها — برای حساب مالی کلی */
    @Query("SELECT * FROM purchases ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Purchase>>

    @Insert
    suspend fun insert(purchase: Purchase): Long
}
