package ir.furnitureshop.app.data.repository

import androidx.room.withTransaction
import ir.furnitureshop.app.data.dao.CheckDao
import ir.furnitureshop.app.data.dao.PaymentDao
import ir.furnitureshop.app.data.dao.SupplierPaymentDao
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.SupplierPayment
import ir.furnitureshop.app.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow

class CheckRepository(
    private val db: AppDatabase,
    private val checkDao: CheckDao,
    private val paymentDao: PaymentDao,
    private val supplierPaymentDao: SupplierPaymentDao
) {

    fun observeAllWithCustomer() = checkDao.observeAllWithCustomer()

    suspend fun getById(id: Long): Check? = checkDao.getById(id)

    fun observeByCustomer(customerId: Long): Flow<List<Check>> =
        checkDao.observeByCustomer(customerId)

    fun observeInHand(): Flow<List<Check>> =
        checkDao.observeInHand(CheckFlow.HOLDER_SHOP)

    fun observeBySupplier(supplierId: Long): Flow<List<Check>> =
        checkDao.observeByHolder(CheckFlow.supplierHolder(supplierId))

    suspend fun isDuplicateNumber(number: String): Boolean =
        CheckFlow.isDuplicateNumber(checkDao.getAllCheckNumbers(), number)

    /**
     * دریافت چک از مشتری — ATOMIC: Payment (مشتری) + Check با هم ثبت می‌شوند.
     */
    suspend fun receiveFromCustomer(
        customerId: Long,
        amount: Long,
        checkNumber: String,
        bank: String?,
        dueDate: Long?
    ): Long = db.withTransaction {
        val (payment, check) = CheckFlow.receiveFromCustomer(
            customerId = customerId,
            amount = amount,
            checkNumber = checkNumber,
            bank = bank,
            dueDate = dueDate
        )
        paymentDao.insert(payment)
        checkDao.insert(check)
    }

    /**
     * انتقال همان چک به تأمین‌کننده — ATOMIC:
     * update چک (holder/status) + Payment (تأمین‌کننده) با هم انجام می‌شوند.
     * Check جدید ساخته نمی‌شود.
     */
    suspend fun transferToSupplier(check: Check, supplierId: Long): Long = db.withTransaction {
        val (updatedCheck, payment) = CheckFlow.transferToSupplier(check, supplierId)
        checkDao.update(updatedCheck)
        supplierPaymentDao.insert(payment)
    }
}
