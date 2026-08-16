package ir.furnitureshop.app.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithCustomer(
    @Embedded val invoice: Invoice,
    @ColumnInfo(name = "customerName") val customerName: String
)

@Dao
interface InvoiceDao {

    // ---- لیست ----

    @Query(
        "SELECT invoices.*, customers.name AS customerName " +
            "FROM invoices INNER JOIN customers ON invoices.customerId = customers.id " +
            "ORDER BY invoices.date DESC"
    )
    fun observeAllWithCustomer(): Flow<List<InvoiceWithCustomer>>

    /** فاکتورهای یک مشتری (برای پروفایل مشتری) */
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY date DESC")
    fun observeByCustomer(customerId: Long): Flow<List<Invoice>>

    /** همه فاکتورها (بدون JOIN) — برای حساب مالی کلی */
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun observeAll(): Flow<List<Invoice>>

    // ---- خواندن ----

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    suspend fun getItems(invoiceId: Long): List<InvoiceItem>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    fun observeItems(invoiceId: Long): Flow<List<InvoiceItem>>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    // ---- نوشتن ----

    @Insert
    suspend fun insert(invoice: Invoice): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItem>)

    /** صدور فاکتور به‌صورت اتمیک: فاکتور + همه اقلام */
    @Transaction
    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val id = insert(invoice)
        insertItems(items.map { it.copy(invoiceId = id) })
        return id
    }
}
