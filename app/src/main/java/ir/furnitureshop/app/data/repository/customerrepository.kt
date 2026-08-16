package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.CustomerDao
import ir.furnitureshop.app.data.dao.InvoiceDao
import ir.furnitureshop.app.data.dao.PaymentDao
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao
) {

    fun observeCustomers(query: String = ""): Flow<List<Customer>> =
        if (query.isBlank()) customerDao.observeAll()
        else customerDao.observeSearch(query.trim())

    fun observeById(id: Long): Flow<Customer?> = customerDao.observeById(id)

    suspend fun getById(id: Long): Customer? = customerDao.getById(id)

    suspend fun insert(customer: Customer): Long = customerDao.insert(customer)

    suspend fun update(customer: Customer) = customerDao.update(customer)

    /** حذف فقط در صورتی که مشتری هیچ فاکتوری نداشته باشد */
    suspend fun deleteIfNoInvoices(customer: Customer): Boolean {
        return if (customerDao.countInvoices(customer.id) == 0) {
            customerDao.delete(customer)
            true
        } else {
            false
        }
    }

    // ---- پروفایل مشتری (ساختار اولیه؛ منطق کامل در تسک‌های بعدی) ----

    fun invoicesFor(customerId: Long): Flow<List<Invoice>> =
        invoiceDao.observeByCustomer(customerId)

    fun paymentsFor(customerId: Long): Flow<List<Payment>> =
        paymentDao.observeByCustomer(customerId)
}
