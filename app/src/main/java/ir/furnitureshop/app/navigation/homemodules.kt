package ir.furnitureshop.app.navigation

/**
 * داده خالص ماژول‌های صفحه اصلی — بدون وابستگی به UI تا قابل تست واحد باشد.
 *
 * صفحه اصلی فقط Navigation است؛ آیکون‌ها در لایه UI به این داده متصل می‌شوند.
 */
data class HomeModuleData(
    val title: String,
    val subtitle: String,
    val route: String
)

/**
 * دقیقاً ۷ بخش اصلی اپ.
 * (Payment مستقل از پروفایل مشتری/تأمین‌کننده ساخته نمی‌شود؛ اینجا فقط ورودی بخش‌هاست.)
 */
val HOME_MODULES: List<HomeModuleData> = listOf(
    HomeModuleData(
        title = "محصولات",
        subtitle = "مبل‌ها و میزها — قیمت پایه، عکس و جستجو",
        route = Routes.PRODUCTS
    ),
    HomeModuleData(
        title = "مشتریان",
        subtitle = "لیست، جستجو و پروفایل با سوابق خرید",
        route = Routes.CUSTOMERS
    ),
    HomeModuleData(
        title = "تأمین‌کنندگان",
        subtitle = "فروشنده مواد اولیه، رنگ‌کار، خدمات‌دهنده یا خریدار",
        route = Routes.SUPPLIERS
    ),
    HomeModuleData(
        title = "فاکتورها",
        subtitle = "صدور فاکتور فروش با تخفیف و پارچه",
        route = Routes.INVOICES
    ),
    HomeModuleData(
        title = "چک‌ها",
        subtitle = "لیست چک‌ها، جزئیات و انتقال به تأمین‌کننده",
        route = Routes.CHECKS
    ),
    HomeModuleData(
        title = "هزینه‌های جانبی",
        subtitle = "حمل، حمل‌ونقل و هزینه‌های کارگاه (مستقل از تأمین‌کننده)",
        route = Routes.EXPENSES
    ),
    HomeModuleData(
        title = "حساب مالی",
        subtitle = "وضعیت مالی کلی: فروش، دریافتی‌ها، خرید جنسی، پرداخت‌ها و هزینه‌ها",
        route = Routes.FINANCIAL_OVERVIEW
    )
)
