package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.PaymentDao
import ir.furnitureshop.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {

    fun observeByCustomer(customerId: Long): Flow<List<Payment>> =
        paymentDao.observeByCustomer(customerId)

    /** همه پرداخت‌های مشتریان — برای حساب مالی کلی */
    fun observeAll(): Flow<List<Payment>> = paymentDao.observeAll()

    suspend fun insert(payment: Payment): Long =
        paymentDao.insert(payment)
}
