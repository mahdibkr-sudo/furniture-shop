package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.CustomerDao
import ir.furnitureshop.app.data.dao.InvoiceDao
import ir.furnitureshop.app.data.dao.LegacyDebtDao
import ir.furnitureshop.app.data.dao.PaymentDao
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao,
    private val legacyDebtDao: LegacyDebtDao? = null
) {

    fun observeCustomers(query: String = ""): Flow<List<Customer>> =
        if (query.isBlank()) customerDao.observeAll()
        else customerDao.observeSearch(query.trim())

    fun observeById(id: Long): Flow<Customer?> = customerDao.observeById(id)

    suspend fun getById(id: Long): Customer? = customerDao.getById(id)

    suspend fun insert(customer: Customer): Long = customerDao.insert(customer)

    suspend fun update(customer: Customer) = customerDao.update(customer)

    /**
     * حذف فقط در صورتی که مشتری هیچ سابقه مالی نداشته باشد:
     * نه فاکتور، نه پرداخت (نقد/چک)، نه بدهی قبلی.
     * (FK ها CASCADE هستند — بدون این بررسی، حذف داده‌های مالی را بی‌صدا پاک می‌کرد)
     */
    suspend fun deleteIfNoInvoices(customer: Customer): Boolean {
        val id = customer.id
        val hasInvoices = customerDao.countInvoices(id) > 0
        val hasPayments = customerDao.countPayments(id) > 0
        val hasLegacyDebts = legacyDebtDao?.countByCustomer(id) ?: 0 > 0
        return if (!hasInvoices && !hasPayments && !hasLegacyDebts) {
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
