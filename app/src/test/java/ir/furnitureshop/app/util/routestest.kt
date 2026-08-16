package ir.furnitureshop.app.util

import ir.furnitureshop.app.navigation.HOME_MODULES
import ir.furnitureshop.app.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست Routeها و ناوبری صفحه اصلی.
 *
 * تأیید می‌کند:
 * - همه route های ثابت معتبرند (غیرخالی، فقط کاراکترهای مجاز)
 * - همه builder ها پارامترها را درست جایگزین می‌کنند (بدون placeholder → ناوبری امن)
 * - Home دقیقاً ۷ ماژول ناوبری دارد با route های درست، بدون پارامتر و بدون تکرار
 * - Payment مستقل از پروفایل مشتری/تأمین‌کننده در Home وجود ندارد
 */
class RoutesTest {

    // ---- همه route های ثابت ----

    @Test
    fun `all route constants are well-formed`() {
        val routes = listOf(
            Routes.HOME,
            Routes.PRODUCTS,
            Routes.PRODUCT_EDIT,
            Routes.CUSTOMERS,
            Routes.CUSTOMER_EDIT,
            Routes.CUSTOMER_PROFILE,
            Routes.INVOICES,
            Routes.INVOICE_CREATE,
            Routes.INVOICE_DETAIL,
            Routes.PAYMENT_CREATE,
            Routes.SUPPLIERS,
            Routes.SUPPLIER_EDIT,
            Routes.SUPPLIER_PROFILE,
            Routes.SUPPLIER_PURCHASE_CREATE,
            Routes.SUPPLIER_PAYMENT_CREATE,
            Routes.CHECKS,
            Routes.CHECK_DETAIL,
            Routes.CHECK_TRANSFER,
            Routes.EXPENSES,
            Routes.EXPENSE_EDIT,
            Routes.EXPENSE_DETAIL,
            Routes.FINANCIAL_OVERVIEW
        )

        assertTrue(routes.isNotEmpty())
        routes.forEach { route ->
            assertFalse("route خالی است: $route", route.isBlank())
            assertTrue(
                "route کاراکتر غیرمجاز دارد: $route",
                route.matches(Regex("[A-Za-z0-9_?={}&]+"))
            )
        }
        // یکتایی
        assertEquals(routes.size, routes.toSet().size)
    }

    // ---- builder ها: جایگزینی کامل پارامترها (ناوبری امن و Back بدون crash) ----

    @Test
    fun `route builders replace all parameters`() {
        assertReplaced(Routes.productEdit(5), "product_edit", "productId=5")
        assertReplaced(Routes.customerEdit(3), "customer_edit", "customerId=3")
        assertReplaced(Routes.customerProfile(7), "customer_profile", "customerId=7")
        assertReplaced(Routes.invoiceDetail(2), "invoice_detail", "invoiceId=2")
        assertReplaced(Routes.paymentCreate(1), "payment_create", "customerId=1")
        assertReplaced(Routes.supplierEdit(4), "supplier_edit", "supplierId=4")
        assertReplaced(Routes.supplierProfile(9), "supplier_profile", "supplierId=9")
        assertReplaced(Routes.supplierPurchaseCreate(9), "supplier_purchase_create", "supplierId=9")
        assertReplaced(Routes.supplierPaymentCreate(9), "supplier_payment_create", "supplierId=9")
        assertReplaced(Routes.checkDetail(6), "check_detail", "checkId=6")
        assertReplaced(Routes.checkTransfer(checkId = 6), "check_transfer", "checkId=6&supplierId=-1")
        assertReplaced(Routes.checkTransfer(supplierId = 4), "check_transfer", "checkId=-1&supplierId=4")
        assertReplaced(Routes.expenseEdit(8), "expense_edit", "expenseId=8")
        assertReplaced(Routes.expenseDetail(8), "expense_detail", "expenseId=8")
    }

    private fun assertReplaced(actual: String, expectedPath: String, expectedQuery: String) {
        assertTrue("مسیر درست نیست: $actual", actual.startsWith(expectedPath))
        assertTrue("پارامتر درست نیست: $actual", actual.contains(expectedQuery))
        assertFalse(
            "placeholder جایگزین نشده (خطر crash در ناوبری): $actual",
            actual.contains("{") || actual.contains("}")
        )
    }

    // ---- صفحه اصلی: فقط ۷ ماژول ناوبری ----

    @Test
    fun `home has exactly the 7 required modules with valid routes`() {
        val expectedRoutes = listOf(
            Routes.PRODUCTS,
            Routes.CUSTOMERS,
            Routes.SUPPLIERS,
            Routes.INVOICES,
            Routes.CHECKS,
            Routes.EXPENSES,
            Routes.FINANCIAL_OVERVIEW
        )
        val expectedTitles = listOf(
            "محصولات",
            "مشتریان",
            "تأمین‌کنندگان",
            "فاکتورها",
            "چک‌ها",
            "هزینه‌های جانبی",
            "حساب مالی"
        )

        assertEquals(7, HOME_MODULES.size)
        assertEquals(expectedTitles, HOME_MODULES.map { it.title })
        assertEquals(expectedRoutes, HOME_MODULES.map { it.route })

        // route های Home بدون پارامترند (ثابت، قابل ناوبری مستقیم)
        HOME_MODULES.forEach { module ->
            assertFalse(
                "ماژول Home نباید پارامتر داشته باشد: ${module.route}",
                module.route.contains("?")
            )
        }
        // بدون تکرار
        assertEquals(HOME_MODULES.size, HOME_MODULES.map { it.route }.toSet().size)
    }

    @Test
    fun `home does not create payment outside customer or supplier profile`() {
        // هیچ ماژولی در Home به «ثبت پرداخت» مستقل اشاره نمی‌کند
        HOME_MODULES.forEach { module ->
            assertFalse(
                "Payment نباید مستقل از پروفایل در Home باشد: ${module.route}",
                module.route == Routes.PAYMENT_CREATE ||
                    module.route == Routes.SUPPLIER_PAYMENT_CREATE
            )
        }
        // و route های پرداخت مستقل، در هیچ ماژول Home نیستند
        val homeRoutes = HOME_MODULES.map { it.route }.toSet()
        assertFalse(homeRoutes.contains(Routes.PAYMENT_CREATE))
        assertFalse(homeRoutes.contains(Routes.SUPPLIER_PAYMENT_CREATE))
    }

    @Test
    fun `home modules navigate back to a static route`() {
        // تمام مقصدهای Home مسیرهای ثابت‌اند؛ با popBackStack استاندارد
        // (که همه صفحه‌ها دارند) بازگشت به Home همیشه ممکن است.
        HOME_MODULES.forEach { module ->
            assertNotEquals(Routes.HOME, module.route)
            assertFalse(module.route.contains("{"))
        }
    }
}
