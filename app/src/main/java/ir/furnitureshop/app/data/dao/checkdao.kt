package ir.furnitureshop.app.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ir.furnitureshop.app.data.entity.Check
import kotlinx.coroutines.flow.Flow

data class CheckWithCustomer(
    @Embedded val check: Check,
    @ColumnInfo(name = "customerName") val customerName: String?
)

@Dao
interface CheckDao {

    /** لیست همه چک‌ها + نام مشتری دریافت‌کننده (LEFT JOIN برای مشتری حذف‌شده) */
    @Query(
        "SELECT checks.*, customers.name AS customerName FROM checks " +
            "LEFT JOIN customers ON checks.receivedFromCustomerId = customers.id " +
            "ORDER BY checks.receiveDate DESC"
    )
    fun observeAllWithCustomer(): Flow<List<CheckWithCustomer>>

    @Query("SELECT * FROM checks WHERE id = :id")
    suspend fun getById(id: Long): Check?

    /** چک‌های دریافتی از یک مشتری (پروفایل مشتری) */
    @Query("SELECT * FROM checks WHERE receivedFromCustomerId = :customerId ORDER BY receiveDate DESC")
    fun observeByCustomer(customerId: Long): Flow<List<Check>>

    /** چک‌های در دست فروشگاه (قابل انتقال) */
    @Query("SELECT * FROM checks WHERE currentHolder = :holder ORDER BY receiveDate DESC")
    fun observeInHand(holder: String): Flow<List<Check>>

    /** چک‌های منتقل‌شده به یک تأمین‌کننده (پروفایل تأمین‌کننده) */
    @Query("SELECT * FROM checks WHERE currentHolder = :holder ORDER BY receiveDate DESC")
    fun observeByHolder(holder: String): Flow<List<Check>>

    @Query("SELECT checkNumber FROM checks")
    suspend fun getAllCheckNumbers(): List<String>

    @Insert
    suspend fun insert(check: Check): Long

    @Update
    suspend fun update(check: Check)
}
