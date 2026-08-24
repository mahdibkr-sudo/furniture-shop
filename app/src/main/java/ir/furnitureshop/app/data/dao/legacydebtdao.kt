package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.furnitureshop.app.data.entity.LegacyDebt
import kotlinx.coroutines.flow.Flow

@Dao
interface LegacyDebtDao {

    @Query("SELECT * FROM legacy_debts WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    fun observeByCustomer(customerId: Long): Flow<List<LegacyDebt>>

    @Query("SELECT * FROM legacy_debts ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<LegacyDebt>>

    /** تعداد بدهی‌های قبلی مشتری — برای جلوگیری از حذف بی‌صدا در CASCADE */
    @Query("SELECT COUNT(*) FROM legacy_debts WHERE customerId = :customerId")
    suspend fun countByCustomer(customerId: Long): Int

    @Insert
    suspend fun insert(debt: LegacyDebt): Long
}
