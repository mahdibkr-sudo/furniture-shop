package ir.furnitureshop.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.furnitureshop.app.data.dao.CheckDao
import ir.furnitureshop.app.data.dao.CustomerDao
import ir.furnitureshop.app.data.dao.ExpenseDao
import ir.furnitureshop.app.data.dao.InvoiceDao
import ir.furnitureshop.app.data.dao.PaymentDao
import ir.furnitureshop.app.data.dao.ProductDao
import ir.furnitureshop.app.data.dao.PurchaseDao
import ir.furnitureshop.app.data.dao.SupplierDao
import ir.furnitureshop.app.data.dao.LegacyDebtDao
import ir.furnitureshop.app.data.dao.SupplierPaymentDao
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Expense
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.entity.LegacyDebt
import ir.furnitureshop.app.data.entity.SupplierPayment

/**
 * Room database — local & offline only, single device.
 */
@Database(
    entities = [
        Product::class,
        Customer::class,
        Invoice::class,
        InvoiceItem::class,
        Payment::class,
        Supplier::class,
        Purchase::class,
        Check::class,
        Expense::class,
        SupplierPayment::class,
        LegacyDebt::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun supplierPaymentDao(): SupplierPaymentDao
    abstract fun checkDao(): CheckDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun legacyDebtDao(): LegacyDebtDao

    companion object {
        const val DATABASE_NAME = "furniture_shop.db"

        /** v1 -> v2: Product ساده شد؛ FK به Product از InvoiceItem حذف شد. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `invoice_items_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`invoiceId` INTEGER NOT NULL, " +
                        "`productId` INTEGER NOT NULL, " +
                        "`quantity` INTEGER NOT NULL, " +
                        "`unitPrice` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO `invoice_items_new` " +
                        "(`id`, `invoiceId`, `productId`, `quantity`, `unitPrice`) " +
                        "SELECT `id`, `invoiceId`, `productId`, `quantity`, `unitPrice` FROM `invoice_items`"
                )
                db.execSQL("DROP TABLE `invoice_items`")
                db.execSQL("ALTER TABLE `invoice_items_new` RENAME TO `invoice_items`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_invoice_items_invoiceId` " +
                        "ON `invoice_items` (`invoiceId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_invoice_items_productId` " +
                        "ON `invoice_items` (`productId`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `products_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`image` TEXT, " +
                        "`basePrice` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO `products_new` (`id`, `name`, `image`, `basePrice`) " +
                        "SELECT `id`, `name`, NULL, `price` FROM `products`"
                )
                db.execSQL("DROP TABLE `products`")
                db.execSQL("ALTER TABLE `products_new` RENAME TO `products`")
            }
        }

        /** v2 -> v3: Customer ساده شد (فقط id, name, phone). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `customers_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`phone` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO `customers_new` (`id`, `name`, `phone`) " +
                        "SELECT `id`, `name`, `phone` FROM `customers`"
                )
                db.execSQL("DROP TABLE `customers`")
                db.execSQL("ALTER TABLE `customers_new` RENAME TO `customers`")
            }
        }

        /** v3 -> v4: invoices + فیلدهای جدید، invoice_items + productName */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `invoice_items_tmp` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`invoiceId` INTEGER NOT NULL, " +
                        "`productId` INTEGER NOT NULL, " +
                        "`productName` TEXT NOT NULL DEFAULT '', " +
                        "`quantity` INTEGER NOT NULL, " +
                        "`unitPrice` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO `invoice_items_tmp` " +
                        "(`id`, `invoiceId`, `productId`, `productName`, `quantity`, `unitPrice`) " +
                        "SELECT ii.`id`, ii.`invoiceId`, ii.`productId`, " +
                        "COALESCE(p.`name`, ''), ii.`quantity`, ii.`unitPrice` " +
                        "FROM `invoice_items` ii " +
                        "LEFT JOIN `products` p ON p.`id` = ii.`productId`"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `invoices_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`customerId` INTEGER NOT NULL, " +
                        "`invoiceNumber` TEXT NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`discount` INTEGER NOT NULL, " +
                        "`fabricCustomerProvided` INTEGER NOT NULL, " +
                        "`fabricAmount` INTEGER NOT NULL, " +
                        "`totalAmount` INTEGER NOT NULL, " +
                        "`note` TEXT, " +
                        "FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO `invoices_new` " +
                        "(`id`, `customerId`, `invoiceNumber`, `date`, `discount`, " +
                        "`fabricCustomerProvided`, `fabricAmount`, `totalAmount`, `note`) " +
                        "SELECT `id`, `customerId`, 'INV-' || printf('%04d', `id`), `date`, 0, 0, 0, " +
                        "`totalAmount`, `note` FROM `invoices`"
                )
                db.execSQL("DROP TABLE `invoice_items`")
                db.execSQL("DROP TABLE `invoices`")
                db.execSQL("ALTER TABLE `invoices_new` RENAME TO `invoices`")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `invoice_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`invoiceId` INTEGER NOT NULL, " +
                        "`productId` INTEGER NOT NULL, " +
                        "`productName` TEXT NOT NULL, " +
                        "`quantity` INTEGER NOT NULL, " +
                        "`unitPrice` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO `invoice_items` " +
                        "(`id`, `invoiceId`, `productId`, `productName`, `quantity`, `unitPrice`) " +
                        "SELECT `id`, `invoiceId`, `productId`, `productName`, `quantity`, `unitPrice` " +
                        "FROM `invoice_items_tmp`"
                )
                db.execSQL("DROP TABLE `invoice_items_tmp`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_invoice_items_invoiceId` " +
                        "ON `invoice_items` (`invoiceId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_invoice_items_productId` " +
                        "ON `invoice_items` (`productId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_invoices_customerId` " +
                        "ON `invoices` (`customerId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_invoices_invoiceNumber` " +
                        "ON `invoices` (`invoiceNumber`)"
                )
            }
        }

        /** v4 -> v5: payments از invoiceId به customerId مستقل شد. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `payments_tmp` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`customerId` INTEGER NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`method` TEXT NOT NULL, " +
                        "FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO `payments_tmp` (`id`, `customerId`, `amount`, `date`, `method`) " +
                        "SELECT p.`id`, i.`customerId`, p.`amount`, p.`date`, " +
                        "COALESCE(p.`method`, 'cash') " +
                        "FROM `payments` p INNER JOIN `invoices` i ON p.`invoiceId` = i.`id`"
                )
                db.execSQL("DROP TABLE `payments`")
                db.execSQL("ALTER TABLE `payments_tmp` RENAME TO `payments`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_payments_customerId` " +
                        "ON `payments` (`customerId`)"
                )
            }
        }

        /** v5 -> v6: suppliers ساده شد + جدول جدید supplier_payments */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=OFF")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `suppliers_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`phone` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO `suppliers_new` (`id`, `name`, `phone`) " +
                        "SELECT `id`, `name`, `phone` FROM `suppliers`"
                )
                db.execSQL("DROP TABLE `suppliers`")
                db.execSQL("ALTER TABLE `suppliers_new` RENAME TO `suppliers`")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `supplier_payments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`supplierId` INTEGER NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`method` TEXT NOT NULL, " +
                        "FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_supplier_payments_supplierId` " +
                        "ON `supplier_payments` (`supplierId`)"
                )
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        /** v6 -> v7: سیستم یکپارچه Check — بازسازی جدول checks با اسکیمای جدید */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=OFF")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `checks_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`checkNumber` TEXT NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`bank` TEXT, " +
                        "`receiveDate` INTEGER NOT NULL, " +
                        "`dueDate` INTEGER, " +
                        "`receivedFromCustomerId` INTEGER, " +
                        "`currentHolder` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "FOREIGN KEY(`receivedFromCustomerId`) REFERENCES `customers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE SET NULL)"
                )
                db.execSQL(
                    "INSERT INTO `checks_new` " +
                        "(`id`, `checkNumber`, `amount`, `bank`, `receiveDate`, `dueDate`, " +
                        "`receivedFromCustomerId`, `currentHolder`, `status`) " +
                        "SELECT `id`, `number`, `amount`, `bank`, COALESCE(`issueDate`, 0), " +
                        "`dueDate`, NULL, 'shop', 'in_hand' FROM `checks`"
                )
                db.execSQL("DROP TABLE `checks`")
                db.execSQL("ALTER TABLE `checks_new` RENAME TO `checks`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_checks_checkNumber` " +
                        "ON `checks` (`checkNumber`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_checks_receivedFromCustomerId` " +
                        "ON `checks` (`receivedFromCustomerId`)"
                )
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        /** v7 -> v8: جدول جدید expenses (هزینه‌های جانبی مستقل از Supplier) */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `expenses` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`description` TEXT)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_expenses_date` " +
                        "ON `expenses` (`date`)"
                )
            }
        }

        /** v8 -> v9: پارچه — متراژ و قیمت واحد هر متر (فاکتورهای قدیمی فقط مبلغ دارند) */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `invoices` ADD COLUMN `fabricMeters` REAL NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `invoices` ADD COLUMN `fabricUnitPrice` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /** v10 -> v11: نام پارچه در فاکتور — ایمن برای داده‌های موجود */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `invoices` ADD COLUMN `fabricName` TEXT")
                } catch (e: Exception) {
                    // ستون احتمالاً از قبل وجود دارد یا خطای غیرمنتظره‌ای رخ داده.
                    // در هر صورت عملیات افزودن ستون نادیده گرفته می‌شود تا مایگریشن ادامه یابد.
                }
            }
        }

        /** v11 -> v12: مسیر تصویر رسید پرداخت نقدی تأمین‌کننده (اختیاری) */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `supplier_payments` ADD COLUMN `receiptImagePath` TEXT")
                } catch (e: Exception) {
                    // ستون احتمالاً از قبل وجود دارد یا خطای غیرمنتظره.
                }
            }
        }

        /**
         * v12 -> v13: ساخت جدول purchases (خرید جنسی از تأمین‌کننده).
         *
         * این جدول در مایگریشن‌های قبلی جا افتاده بود؛ برای دیتابیس‌های قدیمی
         * که ارتقا می‌یابند (و هنوز این جدول را ندارند) به‌صورت ایمن ساخته می‌شود.
         * `IF NOT EXISTS` تضمین می‌کند که دیتابیس‌های تازه‌نصب (که Room خودش همه جدول‌ها
         * را ساخته) دچار خطای duplicate نشوند.
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `purchases` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`supplierId` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`totalAmount` INTEGER NOT NULL, " +
                        "`note` TEXT, " +
                        "FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_purchases_supplierId` " +
                        "ON `purchases` (`supplierId`)"
                )
            }
        }

        /** v9 -> v10: جدول جدید legacy_debts (بدهی قبلی مشتری از سیستم مالی پیشین — بدون فاکتور) */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `legacy_debts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`customerId` INTEGER NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`description` TEXT, " +
                        "FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_legacy_debts_customerId` " +
                        "ON `legacy_debts` (`customerId`)"
                )
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13
                )
                .build()
    }
}
