package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.InvoiceDao
import ir.furnitureshop.app.data.dao.InvoiceWithCustomer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {

    fun observeAllWithCustomer(): Flow<List<InvoiceWithCustomer>> =
        invoiceDao.observeAllWithCustomer()

    /** همه فاکتورها (بدون JOIN) — برای حساب مالی کلی */
    fun observeAll(): Flow<List<Invoice>> = invoiceDao.observeAll()

    fun observeByCustomer(customerId: Long): Flow<List<Invoice>> =
        invoiceDao.observeByCustomer(customerId)

    suspend fun getById(id: Long): Invoice? = invoiceDao.getById(id)

    suspend fun getItems(invoiceId: Long): List<InvoiceItem> =
        invoiceDao.getItems(invoiceId)

    /** صدور اتمیک فاکتور + اقلام */
    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long =
        invoiceDao.createInvoice(invoice, items)

    /** شماره فاکتور بعدی: INV-0001 ، INV-0002 ، ... */
    suspend fun nextInvoiceNumber(): String {
        val next = invoiceDao.count() + 1
        return "INV-" + next.toString().padStart(4, '0')
    }
}
