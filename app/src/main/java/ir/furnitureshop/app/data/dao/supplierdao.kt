package ir.furnitureshop.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ir.furnitureshop.app.data.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM suppliers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Supplier>>

    @Query(
        "SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' " +
            "OR phone LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeSearch(query: String): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun observeById(id: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): Supplier?

    /** تعداد خریدهای از تأمین‌کننده — برای جلوگیری از حذف دارای سابقه */
    @Query("SELECT COUNT(*) FROM purchases WHERE supplierId = :supplierId")
    suspend fun countPurchases(supplierId: Long): Int

    @Insert
    suspend fun insert(supplier: Supplier): Long

    @Update
    suspend fun update(supplier: Supplier)

    @Delete
    suspend fun delete(supplier: Supplier)
}
