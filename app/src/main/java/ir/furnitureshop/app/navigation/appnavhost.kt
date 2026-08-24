package ir.furnitureshop.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.furnitureshop.app.ui.screens.CashPaymentsScreen
import ir.furnitureshop.app.ui.screens.CheckDetailScreen
import ir.furnitureshop.app.ui.screens.BackupScreen
import ir.furnitureshop.app.ui.screens.CheckTransferScreen
import ir.furnitureshop.app.ui.screens.ChecksScreen
import ir.furnitureshop.app.ui.screens.CustomerEditScreen
import ir.furnitureshop.app.ui.screens.CustomerProfileScreen
import ir.furnitureshop.app.ui.screens.CustomersScreen
import ir.furnitureshop.app.ui.screens.ExpenseDetailScreen
import ir.furnitureshop.app.ui.screens.ExpenseEditScreen
import ir.furnitureshop.app.ui.screens.ExpensesScreen
import ir.furnitureshop.app.ui.screens.FinancialOverviewScreen
import ir.furnitureshop.app.ui.screens.HomeScreen
import ir.furnitureshop.app.ui.screens.InvoiceCreateScreen
import ir.furnitureshop.app.ui.screens.InvoiceDetailScreen
import ir.furnitureshop.app.ui.screens.InvoicesScreen
import ir.furnitureshop.app.ui.screens.LegacyDebtCreateScreen
import ir.furnitureshop.app.ui.screens.PaymentCreateScreen
import ir.furnitureshop.app.ui.screens.ProductEditScreen
import ir.furnitureshop.app.ui.screens.ProductsScreen
import ir.furnitureshop.app.ui.screens.SupplierEditScreen
import ir.furnitureshop.app.ui.screens.SupplierPaymentCreateScreen
import ir.furnitureshop.app.ui.screens.SupplierProfileScreen
import ir.furnitureshop.app.ui.screens.SupplierPurchaseCreateScreen
import ir.furnitureshop.app.ui.screens.SuppliersScreen

object Routes {
    const val HOME = "home"

    const val PRODUCTS = "products"
    const val PRODUCT_EDIT = "product_edit?productId={productId}"

    const val CUSTOMERS = "customers"
    const val CUSTOMER_EDIT = "customer_edit?customerId={customerId}"
    const val CUSTOMER_PROFILE = "customer_profile?customerId={customerId}"

    const val INVOICES = "invoices"
    const val INVOICE_CREATE = "invoice_create"
    const val INVOICE_DETAIL = "invoice_detail?invoiceId={invoiceId}"

    const val PAYMENT_CREATE = "payment_create?customerId={customerId}"
    const val LEGACY_DEBT_CREATE = "legacy_debt_create?customerId={customerId}"

    const val SUPPLIERS = "suppliers"
    const val SUPPLIER_EDIT = "supplier_edit?supplierId={supplierId}"
    const val SUPPLIER_PROFILE = "supplier_profile?supplierId={supplierId}"
    const val SUPPLIER_PURCHASE_CREATE = "supplier_purchase_create?supplierId={supplierId}"
    const val SUPPLIER_PAYMENT_CREATE = "supplier_payment_create?supplierId={supplierId}"

    const val CHECKS = "checks"
    const val CHECK_DETAIL = "check_detail?checkId={checkId}"
    const val CHECK_TRANSFER = "check_transfer?checkId={checkId}&supplierId={supplierId}"

    const val CASH_PAYMENTS = "cash_payments"

    const val EXPENSES = "expenses"
    const val EXPENSE_EDIT = "expense_edit?expenseId={expenseId}"
    const val EXPENSE_DETAIL = "expense_detail?expenseId={expenseId}"

    const val FINANCIAL_OVERVIEW = "financial_overview"
    const val BACKUP = "backup"

    // --- محصولات ---
    fun productEdit(productId: Long? = null): String =
        "product_edit?productId=${productId ?: -1L}"

    // --- مشتریان ---
    fun customerEdit(customerId: Long? = null): String =
        "customer_edit?customerId=${customerId ?: -1L}"

    fun customerProfile(customerId: Long): String =
        "customer_profile?customerId=$customerId"

    // --- فاکتور ---
    fun invoiceDetail(invoiceId: Long): String =
        "invoice_detail?invoiceId=$invoiceId"

    // --- پرداخت ---
    fun paymentCreate(customerId: Long): String =
        "payment_create?customerId=$customerId"

    // --- بدهی قبلی ---
    fun legacyDebtCreate(customerId: Long): String =
        "legacy_debt_create?customerId=$customerId"

    // --- تأمین‌کنندگان ---
    fun supplierEdit(supplierId: Long? = null): String =
        "supplier_edit?supplierId=${supplierId ?: -1L}"

    fun supplierProfile(supplierId: Long): String =
        "supplier_profile?supplierId=$supplierId"

    fun supplierPurchaseCreate(supplierId: Long): String =
        "supplier_purchase_create?supplierId=$supplierId"

    fun supplierPaymentCreate(supplierId: Long): String =
        "supplier_payment_create?supplierId=$supplierId"

    // --- چک ---
    fun checkDetail(checkId: Long): String =
        "check_detail?checkId=$checkId"

    fun checkTransfer(checkId: Long? = null, supplierId: Long? = null): String =
        "check_transfer?checkId=${checkId ?: -1L}&supplierId=${supplierId ?: -1L}"

    // --- هزینه‌های جانبی ---
    fun expenseEdit(expenseId: Long? = null): String =
        "expense_edit?expenseId=${expenseId ?: -1L}"

    fun expenseDetail(expenseId: Long): String =
        "expense_detail?expenseId=$expenseId"
}

/**
 * Navigation اصلی اپ — مسیرهای جدید (پرداخت، چک، خرید و...) در تسک‌های بعدی.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        // --- محصولات ---
        composable(Routes.PRODUCTS) {
            ProductsScreen(navController)
        }
        composable(
            route = Routes.PRODUCT_EDIT,
            arguments = listOf(navArgument("productId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: -1L
            ProductEditScreen(
                navController = navController,
                productId = if (productId > 0) productId else 0L
            )
        }

        // --- مشتریان ---
        composable(Routes.CUSTOMERS) {
            CustomersScreen(navController)
        }
        composable(
            route = Routes.CUSTOMER_EDIT,
            arguments = listOf(navArgument("customerId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val customerId = entry.arguments?.getLong("customerId") ?: -1L
            CustomerEditScreen(
                navController = navController,
                customerId = if (customerId > 0) customerId else 0L
            )
        }
        composable(
            route = Routes.CUSTOMER_PROFILE,
            arguments = listOf(navArgument("customerId") {
                type = NavType.LongType
            })
        ) { entry ->
            CustomerProfileScreen(
                navController = navController,
                customerId = entry.arguments?.getLong("customerId") ?: 0L
            )
        }

        // --- فاکتور ---
        composable(Routes.INVOICES) {
            InvoicesScreen(navController)
        }
        composable(Routes.INVOICE_CREATE) {
            InvoiceCreateScreen(navController)
        }
        composable(
            route = Routes.INVOICE_DETAIL,
            arguments = listOf(navArgument("invoiceId") {
                type = NavType.LongType
            })
        ) { entry ->
            InvoiceDetailScreen(
                navController = navController,
                invoiceId = entry.arguments?.getLong("invoiceId") ?: 0L
            )
        }

        // --- پرداخت ---
        composable(
            route = Routes.PAYMENT_CREATE,
            arguments = listOf(navArgument("customerId") {
                type = NavType.LongType
            })
        ) { entry ->
            PaymentCreateScreen(
                navController = navController,
                customerId = entry.arguments?.getLong("customerId") ?: 0L
            )
        }

        // --- بدهی قبلی (سیستم مالی پیشین) ---
        composable(
            route = Routes.LEGACY_DEBT_CREATE,
            arguments = listOf(navArgument("customerId") {
                type = NavType.LongType
            })
        ) { entry ->
            LegacyDebtCreateScreen(
                navController = navController,
                customerId = entry.arguments?.getLong("customerId") ?: 0L
            )
        }

        // --- تأمین‌کنندگان ---
        composable(Routes.SUPPLIERS) {
            SuppliersScreen(navController)
        }
        composable(
            route = Routes.SUPPLIER_EDIT,
            arguments = listOf(navArgument("supplierId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val supplierId = entry.arguments?.getLong("supplierId") ?: -1L
            SupplierEditScreen(
                navController = navController,
                supplierId = if (supplierId > 0) supplierId else 0L
            )
        }
        composable(
            route = Routes.SUPPLIER_PROFILE,
            arguments = listOf(navArgument("supplierId") {
                type = NavType.LongType
            })
        ) { entry ->
            SupplierProfileScreen(
                navController = navController,
                supplierId = entry.arguments?.getLong("supplierId") ?: 0L
            )
        }
        composable(
            route = Routes.SUPPLIER_PURCHASE_CREATE,
            arguments = listOf(navArgument("supplierId") {
                type = NavType.LongType
            })
        ) { entry ->
            SupplierPurchaseCreateScreen(
                navController = navController,
                supplierId = entry.arguments?.getLong("supplierId") ?: 0L
            )
        }
        composable(
            route = Routes.SUPPLIER_PAYMENT_CREATE,
            arguments = listOf(navArgument("supplierId") {
                type = NavType.LongType
            })
        ) { entry ->
            SupplierPaymentCreateScreen(
                navController = navController,
                supplierId = entry.arguments?.getLong("supplierId") ?: 0L
            )
        }

        // --- چک ---
        composable(Routes.CHECKS) {
            ChecksScreen(navController)
        }
        composable(
            route = Routes.CHECK_DETAIL,
            arguments = listOf(navArgument("checkId") {
                type = NavType.LongType
            })
        ) { entry ->
            CheckDetailScreen(
                navController = navController,
                checkId = entry.arguments?.getLong("checkId") ?: 0L
            )
        }
        composable(
            route = Routes.CHECK_TRANSFER,
            arguments = listOf(
                navArgument("checkId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("supplierId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { entry ->
            val checkId = entry.arguments?.getLong("checkId") ?: -1L
            val supplierId = entry.arguments?.getLong("supplierId") ?: -1L
            CheckTransferScreen(
                navController = navController,
                checkId = if (checkId > 0) checkId else 0L,
                supplierId = if (supplierId > 0) supplierId else null
            )
        }

        // --- پرداخت‌های نقدی ---
        composable(Routes.CASH_PAYMENTS) {
            CashPaymentsScreen(navController)
        }

        // --- هزینه‌های جانبی ---
        composable(Routes.EXPENSES) {
            ExpensesScreen(navController)
        }
        composable(
            route = Routes.EXPENSE_EDIT,
            arguments = listOf(navArgument("expenseId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val expenseId = entry.arguments?.getLong("expenseId") ?: -1L
            ExpenseEditScreen(
                navController = navController,
                expenseId = if (expenseId > 0) expenseId else 0L
            )
        }
        composable(
            route = Routes.EXPENSE_DETAIL,
            arguments = listOf(navArgument("expenseId") {
                type = NavType.LongType
            })
        ) { entry ->
            ExpenseDetailScreen(
                navController = navController,
                expenseId = entry.arguments?.getLong("expenseId") ?: 0L
            )
        }

        // --- حساب مالی کلی ---
        composable(Routes.FINANCIAL_OVERVIEW) {
            FinancialOverviewScreen(navController)
        }

        // --- پشتیبان‌گیری ---
        composable(Routes.BACKUP) {
            BackupScreen(navController)
        }
    }
}
