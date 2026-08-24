package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ir.furnitureshop.app.data.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Customer>>

    @Query(
        "SELECT * FROM customers WHERE name LIKE '%' || :query || '%' " +
            "OR phone LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeSearch(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    /** تعداد فاکتورهای مشتری — برای جلوگیری از حذف مشتری دارای سابقه */
    @Query("SELECT COUNT(*) FROM invoices WHERE customerId = :customerId")
    suspend fun countInvoices(customerId: Long): Int

    /** تعداد پرداخت‌های مشتری (نقد و چک) — برای جلوگیری از حذف بی‌صدا در CASCADE */
    @Query("SELECT COUNT(*) FROM payments WHERE customerId = :customerId")
    suspend fun countPayments(customerId: Long): Int

    @Insert
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}
