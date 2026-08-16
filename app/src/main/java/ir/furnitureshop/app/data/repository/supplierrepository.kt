package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.PurchaseDao
import ir.furnitureshop.app.data.dao.SupplierDao
import ir.furnitureshop.app.data.dao.SupplierPaymentDao
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow

class SupplierRepository(
    private val supplierDao: SupplierDao,
    private val purchaseDao: PurchaseDao,
    private val supplierPaymentDao: SupplierPaymentDao
) {

    fun observeSuppliers(query: String = ""): Flow<List<Supplier>> =
        if (query.isBlank()) supplierDao.observeAll()
        else supplierDao.observeSearch(query.trim())

    fun observeById(id: Long): Flow<Supplier?> = supplierDao.observeById(id)

    suspend fun getById(id: Long): Supplier? = supplierDao.getById(id)

    suspend fun insert(supplier: Supplier): Long = supplierDao.insert(supplier)

    suspend fun update(supplier: Supplier) = supplierDao.update(supplier)

    /** حذف فقط در صورت نبود خرید جنسی ثبت‌شده */
    suspend fun deleteIfNoPurchases(supplier: Supplier): Boolean {
        return if (supplierDao.countPurchases(supplier.id) == 0) {
            supplierDao.delete(supplier)
            true
        } else {
            false
        }
    }

    // ---- پروفایل تأمین‌کننده ----

    fun purchasesFor(supplierId: Long): Flow<List<Purchase>> =
        purchaseDao.observeBySupplier(supplierId)

    fun paymentsFor(supplierId: Long): Flow<List<SupplierPayment>> =
        supplierPaymentDao.observeBySupplier(supplierId)

    /** همه خریدها — برای حساب مالی کلی */
    fun observeAllPurchases(): Flow<List<Purchase>> = purchaseDao.observeAll()

    /** همه پرداخت‌های تأمین‌کنندگان — برای حساب مالی کلی */
    fun observeAllPayments(): Flow<List<SupplierPayment>> = supplierPaymentDao.observeAll()

    suspend fun insertPurchase(purchase: Purchase): Long =
        purchaseDao.insert(purchase)

    suspend fun insertPayment(payment: SupplierPayment): Long =
        supplierPaymentDao.insert(payment)
}
