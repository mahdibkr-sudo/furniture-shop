package ir.furnitureshop.app

import android.app.Application
import ir.furnitureshop.app.data.local.AppDatabase
import ir.furnitureshop.app.data.local.ProductImageStore
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.ExpenseRepository
import ir.furnitureshop.app.data.repository.InvoiceRepository
import ir.furnitureshop.app.data.repository.PaymentRepository
import ir.furnitureshop.app.data.repository.ProductRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.CrashLogger

/**
 * Application class — ساده‌ترین DI دستی (بدون Hilt تا پروژه سبک بماند).
 */
class FurnitureApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // ثبت خطاهای ناگهانی در فایل محلی تا قابل ارسال/تشخیص باشند
        CrashLogger.install(this)
    }

    val database: AppDatabase by lazy {
        AppDatabase.build(this)
    }

    val productImageStore: ProductImageStore by lazy {
        ProductImageStore(this)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao())
    }

    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(
            customerDao = database.customerDao(),
            invoiceDao = database.invoiceDao(),
            paymentDao = database.paymentDao()
        )
    }

    val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(database.invoiceDao())
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(database.paymentDao())
    }

    val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(
            supplierDao = database.supplierDao(),
            purchaseDao = database.purchaseDao(),
            supplierPaymentDao = database.supplierPaymentDao()
        )
    }

    val checkRepository: CheckRepository by lazy {
        CheckRepository(
            db = database,
            checkDao = database.checkDao(),
            paymentDao = database.paymentDao(),
            supplierPaymentDao = database.supplierPaymentDao()
        )
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }
}
